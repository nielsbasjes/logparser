/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.CoreLogModule;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.GeoIPModule;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.KubernetesIngressModule;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.NginxModule;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.SslModule;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.UpstreamModule;
import nl.basjes.parse.httpdlog.dissectors.nginxmodules.VariousModule;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertMillisecondsIntoMicroseconds;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertSecondsWithMillisStringDissector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.basjes.parse.core.Casts.STRING_OR_LONG;

@SuppressWarnings({
    "PMD.LongVariable", // I like my variable names this way
    "PMD.CyclomaticComplexity", "PMD.OnlyOneReturn",
    "PMD.BeanMembersShouldSerialize", // No beans here
    "PMD.DataflowAnomalyAnalysis" // Results in a lot of mostly useless messages.
})
public class NginxHttpdLogFormatDissector extends TokenFormatDissector {

    private static final Logger LOG = LoggerFactory.getLogger(NginxHttpdLogFormatDissector.class);

    public NginxHttpdLogFormatDissector(final String logFormat) {
        super(logFormat);
        setInputType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    public NginxHttpdLogFormatDissector() {
        super();
        setInputType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    private void overrideLogFormat(String originalLogformat, String logformat) {
        LOG.debug("Specified logformat \"{}\" was mapped to {}", originalLogformat, logformat);
        super.setLogFormat(logformat);
    }

    @Override
    public void setLogFormat(final String logformat) {
        // https://nginx.org/en/docs/http/ngx_http_log_module.html#log_format
        // The configuration always includes the predefined “combined” format:

        //  log_format combined '$remote_addr - $remote_user [$time_local] '
        //                      '"$request" $status $body_bytes_sent '
        //                      '"$http_referer" "$http_user_agent"';
        switch (logformat.toLowerCase(Locale.getDefault())) {
            case "combined":
                overrideLogFormat(logformat,
                    "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"");
                break;
            default:
                super.setLogFormat(logformat);
                break;
        }
    }

    public static boolean looksLikeNginxFormat(String logFormat) {
        if (logFormat.indexOf('$') != -1) {
            return true;
        }
        switch (logFormat.toLowerCase(Locale.getDefault())) {
            case "combined":
                return true;
            default:
                return false;
        }
    }

    // --------------------------------------------

    @Override
    public String decodeExtractedValue(String tokenName, String value) {
        if (value == null || value.equals("")) {
            return value;
        }

        // In Apache logfiles a '-' means a 'not specified' / 'empty' value.
        if (value.equals("-")) {
            return null;
        }

        return value;
    }

    private static final List<NginxModule> MODULES = new ArrayList<>();
    static {
        MODULES.add(new CoreLogModule());
        MODULES.add(new UpstreamModule());
        MODULES.add(new SslModule());
        MODULES.add(new GeoIPModule());
        MODULES.add(new VariousModule());
        MODULES.add(new KubernetesIngressModule());
    }

    // --------------------------------------------
    @Override
    protected List<TokenParser> createAllTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>();
        MODULES.forEach(m -> parsers.addAll(m.getTokenParsers()));

        return parsers;
    }

    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        super.createAdditionalDissectors(parser);
        parser.addDissector(new BinaryIPDissector());
        parser.addDissector(new ConvertSecondsWithMillisStringDissector("SECOND_MILLIS",            "MILLISECONDS"));
        parser.addDissector(new ConvertSecondsWithMillisStringDissector("TIME.EPOCH_SECOND_MILLIS", "TIME.EPOCH"));
        parser.addDissector(new ConvertMillisecondsIntoMicroseconds("MILLISECONDS", "MICROSECONDS"));

        MODULES.forEach(m -> parser.addDissectors(m.getDissectors()));
    }

    public static class BinaryIPDissector extends SimpleDissector {

        private static final HashMap<String, EnumSet<Casts>> EPOCH_MILLIS_CONFIG = new HashMap<>();
        static {
            EPOCH_MILLIS_CONFIG.put("IP:", STRING_OR_LONG);
        }
        public BinaryIPDissector() {
            super("IP_BINARY", EPOCH_MILLIS_CONFIG);
        }

        private static final String CAPTURE_HEX_BYTE = "\\\\x([0-9a-fA-F][0-9a-fA-F])";
        final Pattern binaryIPPattern = Pattern.compile(
            CAPTURE_HEX_BYTE+CAPTURE_HEX_BYTE+CAPTURE_HEX_BYTE+CAPTURE_HEX_BYTE
        );

        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            Matcher matcher = binaryIPPattern.matcher(value.getString());
            if (matcher.matches()) {
                String ip =
                    String.valueOf(Utils.hexCharsToByte(matcher.group(1))) + '.' +
                    String.valueOf(Utils.hexCharsToByte(matcher.group(2))) + '.' +
                    String.valueOf(Utils.hexCharsToByte(matcher.group(3))) + '.' +
                    String.valueOf(Utils.hexCharsToByte(matcher.group(4)));
                parsable.addDissection(inputname, "IP", "", ip);
            }
        }
    }

    // This is marked as deprecated because we want to mark all uses of this as "undesirable"
    @Deprecated
    public static class NotYetImplemented extends NotImplementedTokenParser {
        private static final String FIELD_PREFIX = "nginx_parameter";
        public NotYetImplemented(final String nLogFormatToken) {
            super(nLogFormatToken, FIELD_PREFIX, 0);
        }

        public NotYetImplemented(final String nLogFormatToken, final String regex) {
            super(nLogFormatToken, FIELD_PREFIX, regex, 0);
        }

        public NotYetImplemented(final String nLogFormatToken, final String regex, final int prio) {
            super(nLogFormatToken, FIELD_PREFIX, regex, prio);
        }

        public NotYetImplemented(final String nLogFormatToken, final int prio) {
            super(nLogFormatToken, FIELD_PREFIX, "[^\" ]*", prio);
        }
    }

}
