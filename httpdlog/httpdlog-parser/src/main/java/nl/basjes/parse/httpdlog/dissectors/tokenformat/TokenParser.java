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
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TokenParser implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TokenParser.class);

    // ---------------------------------------
    public static final String FORMAT_DIGIT = "[0-9]";
    public static final String FORMAT_NUMBER = FORMAT_DIGIT + "+";
    public static final String FORMAT_CLF_NUMBER = FORMAT_NUMBER + "|-";
    public static final String FORMAT_HEXDIGIT = "[0-9a-fA-F]";
    public static final String FORMAT_HEXNUMBER = FORMAT_HEXDIGIT + "+";
    public static final String FORMAT_CLF_HEXNUMBER = FORMAT_HEXNUMBER + "|-";
    public static final String FORMAT_NON_ZERO_NUMBER = "[1-9][0-9]*";
    public static final String FORMAT_CLF_NON_ZERO_NUMBER = FORMAT_NON_ZERO_NUMBER + "|-";
    public static final String FORMAT_EIGHT_BIT_DECIMAL = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    // This next regex only allows for the common form of an IPv4 address that appear in a logfile.
    public static final String FORMAT_IPV4 = "(?:" + FORMAT_EIGHT_BIT_DECIMAL + "\\.){3}" + FORMAT_EIGHT_BIT_DECIMAL;

    // From http://codepad.org/wo95tiyp
    public static final String FORMAT_IPV6 = ":?(?:" + FORMAT_HEXDIGIT + "{1,4}(?::|.)?){0,8}(?::|::)?(?:" + FORMAT_HEXDIGIT + "{1,4}(?::|.)?){0,8}";
    public static final String FORMAT_IP = FORMAT_IPV4 + "|" + FORMAT_IPV6;

    public static final String FORMAT_CLF_IP = FORMAT_IP + "|-";
    public static final String FORMAT_STRING = ".*?";
    public static final String FORMAT_NO_SPACE_STRING = "[^\\s]*";
    public static final String FIXED_STRING = "FIXED_STRING";

    // This expression "forces" a year in the range [1000-9999] :).
    public static final String FORMAT_STANDARD_TIME_US =
            "[0-3][0-9]/(?:[a-zA-Z][a-zA-Z][a-zA-Z])/[1-9][0-9][0-9][0-9]:[0-9][0-9]:[0-9][0-9]:[0-9][0-9] [\\+|\\-][0-9][0-9][0-9][0-9]";

    public static final String FORMAT_STANDARD_TIME_ISO8601 =
        "[1-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9][\\+|\\-][0-9][0-9]:[0-9][0-9]";

    public static final String FORMAT_NUMBER_DECIMAL = FORMAT_NUMBER + "\\." + FORMAT_NUMBER;
    public static final String FORMAT_NUMBER_OPTIONAL_DECIMAL = FORMAT_NUMBER + "(?:\\." + FORMAT_NUMBER + ")?";

    // ---------------------------------------

    private final String logFormatToken;
    private final List<TokenOutputField> outputFields = new ArrayList<>();
    private final String regex;
    private final int prio;
    protected String warningMessageWhenUsed;
    private final Dissector customDissector;

    // --------------------------------------------
    public TokenParser(final String nLogFormatToken,
            final String nValueName,
            final String nValueType,
            final EnumSet<Casts> nCasts,
            final String nRegex) {
        this(nLogFormatToken, nValueName, nValueType, nCasts, nRegex, 10);
    }

    public TokenParser(final String nLogFormatToken,
                       final String nValueName,
                       final String nValueType,
                       final EnumSet<Casts> nCasts,
                       final String nRegex,
                       final int nPrio) {
        this(nLogFormatToken, nValueName, nValueType, nCasts, nRegex, nPrio, null);
    }

    public TokenParser(final String nLogFormatToken,
            final String nValueName,
            final String nValueType,
            final EnumSet<Casts> nCasts,
            final String nRegex,
            final int nPrio,
           final Dissector nCustomDissector) {
        logFormatToken = nLogFormatToken;
        regex = nRegex;
        prio = nPrio;
        customDissector = nCustomDissector;
        addOutputField(nValueType, nValueName, nCasts);
    }

    public TokenParser(final String nLogFormatToken,
                       final String nRegex) {
        this(nLogFormatToken, nRegex, 0, null);
    }

    public TokenParser(final String nLogFormatToken,
                       final String nRegex,
                       final int nPrio) {
        this(nLogFormatToken, nRegex, nPrio, null);
    }

    public TokenParser(final String nLogFormatToken,
                       final String nRegex,
                       final int nPrio,
                       final Dissector nCustomDissector) {
        logFormatToken = nLogFormatToken;
        regex = nRegex;
        prio = nPrio;
        customDissector = nCustomDissector;
    }

    public TokenParser addOutputField(String type, String name, EnumSet<Casts> casts) {
        outputFields.add(new TokenOutputField(type, name, casts));
        return this;
    }

    public TokenParser addOutputField(String type, String name, EnumSet<Casts> casts, String deprecateFor) {
        outputFields.add(new TokenOutputField(type, name, casts).deprecateFor(deprecateFor));
        return this;
    }

    public TokenParser addOutputField(TokenOutputField outputField) {
        this.outputFields.add(outputField);
        return this;
    }

    public TokenParser addOutputFields(List<TokenOutputField> nOutputFields) {
        this.outputFields.addAll(nOutputFields);
        return this;
    }

    public List<TokenOutputField> getOutputFields() {
        return outputFields;
    }

    // --------------------------------------------

    public TokenParser setWarningMessageWhenUsed(String message) {
        this.warningMessageWhenUsed = message;
        return this;
    }

    public String getLogFormatToken() {
        return logFormatToken;
    }

    public String getRegex() {
        return regex;
    }

    public int getPrio() {
        return prio;
    }

    public Dissector getCustomDissector() {
        return customDissector;
    }

    // --------------------------------------------

    public Token getNextToken(final String logFormat, final int startOffset) {
        final int pos = logFormat.indexOf(logFormatToken, startOffset);
        if (pos == -1) {
            return null;
        }

        Token token = new Token(
                regex,
                pos,
                logFormatToken.length(),
                prio)
            .addOutputFields(outputFields);

        if (warningMessageWhenUsed != null) {
            token.setWarningMessageWhenUsed(warningMessageWhenUsed);
        }

        if (!addCustomDissector(token,
            outputFields.get(0).getType(),
            outputFields.get(0).getName())) {
            return null;
        }

        return token;
    }

    // --------------------------------------------

    public List<Token> getTokens(final String logFormat) {
        if (StringUtils.isBlank(logFormat)) {
            return null;
        }

        final List<Token> result = new ArrayList<>(5); // Usually a good guess

        int offset = 0;
        while (true) {
            final Token token = getNextToken(logFormat, offset);
            if (token == null) {
                break;
            }
            result.add(token);
            offset = token.getStartPos() + token.getLength();
        }
        return result;
    }

    // --------------------------------------------

    protected boolean addCustomDissector(Token token, String fieldType, String fieldName) {
        if (customDissector == null) {
            return true;
        }
        try {
            Dissector dissector = customDissector.getNewInstance();
            dissector.setInputType(fieldType);
            if (!dissector.initializeFromSettingsParameter(fieldName)) {
                LOG.error("Unable to INITIALIZE custom dissector for {}:{}", fieldType, fieldName);
                return false;
            }
            token.setCustomDissector(dissector);
        } catch (Exception e) {
            LOG.error("Unable to add custom dissector for {}:{} because of : {}", fieldType, fieldName, e.getMessage());
            return false;
        }
        return true;
    }

}
