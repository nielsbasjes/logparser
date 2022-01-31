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

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.dissectors.HttpFirstLineDissector;
import nl.basjes.parse.httpdlog.dissectors.HttpFirstLineProtocolDissector;
import nl.basjes.parse.httpdlog.dissectors.HttpUriDissector;
import nl.basjes.parse.httpdlog.dissectors.ModUniqueIdDissector;
import nl.basjes.parse.httpdlog.dissectors.QueryStringFieldDissector;
import nl.basjes.parse.httpdlog.dissectors.RequestCookieListDissector;
import nl.basjes.parse.httpdlog.dissectors.ResponseSetCookieDissector;
import nl.basjes.parse.httpdlog.dissectors.ResponseSetCookieListDissector;
import nl.basjes.parse.httpdlog.dissectors.TimeStampDissector;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertCLFIntoNumber;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertNumberIntoCLF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.basjes.parse.httpdlog.Version.GIT_COMMIT_ID_DESCRIBE_SHORT;
import static nl.basjes.parse.httpdlog.Version.MAVEN_BUILD_TIMESTAMP;
import static nl.basjes.parse.httpdlog.Version.PROJECT_VERSION;

public class HttpdLoglineParser<RECORD> extends Parser<RECORD> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpdLoglineParser.class);

    // --------------------------------------------

    public HttpdLoglineParser(
            final Class<RECORD> clazz,
            final String logformat) {
        super(clazz);
        logVersion();
        setupDissectors(logformat, null);
    }

    // --------------------------------------------

    public static void logVersion(){
        String[] lines = {
            "Apache HTTPD & NGINX Access log parsing made easy",
            "For more information: https://github.com/nielsbasjes/logparser",
            "Copyright (C) 2011-2021 Niels Basjes - License Apache 2.0"
        };
        String version = getVersion();
        int width = version.length();
        for (String line: lines) {
            width = Math.max(width, line.length());
        }

        LOG.info("");
        LOG.info("/-{}-\\", padding('-', width));
        logLine(version, width);
        LOG.info("+-{}-+", padding('-', width));
        for (String line: lines) {
            logLine(line, width);
        }
        LOG.info("\\-{}-/", padding('-', width));
        LOG.info("");
    }

    private static String padding(char letter, int count) {
        StringBuilder sb = new StringBuilder(128);
        for (int i=0; i <count; i++) {
            sb.append(letter);
        }
        return sb.toString();
    }

    private static void logLine(String line, int width) {
        LOG.info("| {}{} |", line, padding(' ', width - line.length()));
    }

    // --------------------------------------------

    public static String getVersion() {
        return "LogParser " + PROJECT_VERSION +
            " (" + GIT_COMMIT_ID_DESCRIBE_SHORT + " @ " + MAVEN_BUILD_TIMESTAMP + ")";
    }

    public HttpdLoglineParser(
            final Class<RECORD> clazz,
            final String logformat,
            final String timestampFormat) {
        super(clazz);
        setupDissectors(logformat, timestampFormat);
    }

    private void setupDissectors(
            final String logformat,
            final String timestampFormat) {
        // The pieces we have to get there
        addDissector(new HttpdLogFormatDissector(logformat));
        addDissector(new TimeStampDissector("TIME.STAMP", timestampFormat));
        addDissector(new TimeStampDissector("TIME.ISO8601", "yyyy-MM-dd'T'HH:mm:ssXXX"));
        addDissector(new HttpFirstLineDissector());
        addDissector(new HttpFirstLineProtocolDissector());
        addDissector(new HttpUriDissector());
        addDissector(new QueryStringFieldDissector());
        addDissector(new RequestCookieListDissector());
        addDissector(new ResponseSetCookieListDissector());
        addDissector(new ResponseSetCookieDissector());
        addDissector(new ModUniqueIdDissector());

        // Type translators
        addDissector(new ConvertCLFIntoNumber("BYTESCLF", "BYTES"));
        addDissector(new ConvertNumberIntoCLF("BYTES", "BYTESCLF"));

        // And we define the input for this parser
        setRootType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    // --------------------------------------------

}
