/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


import nl.basjes.parse.core.Casts;
import org.apache.commons.lang.StringUtils;

public class TokenParser {

    // ---------------------------------------
    public static final String FORMAT_NUMBER = "[0-9]*";
    public static final String FORMAT_CLF_NUMBER = FORMAT_NUMBER + "|-";
    public static final String FORMAT_HEXDIGIT = "[0-9a-fA-F]";
    public static final String FORMAT_HEXNUMBER = FORMAT_HEXDIGIT+"*";
    public static final String FORMAT_CLF_HEXNUMBER = FORMAT_HEXNUMBER + "|-";
    public static final String FORMAT_NON_ZERO_NUMBER = "[1-9]|[1-9][0-9]*";
    public static final String FORMAT_EIGHT_BIT_DECIMAL = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    // This next regex only allows for the common form of an IPv4 address that appear in a logfile.
    public static final String FORMAT_IPV4 = "(?:"+FORMAT_EIGHT_BIT_DECIMAL+"\\.){3}"+FORMAT_EIGHT_BIT_DECIMAL;

    // From http://codepad.org/wo95tiyp
    public static final String FORMAT_IPV6 = ":?(?:"+FORMAT_HEXDIGIT+"{1,4}(?::|.)?){0,8}(?::|::)?(?:"+FORMAT_HEXDIGIT+"{1,4}(?::|.)?){0,8}";
    public static final String FORMAT_IP = FORMAT_IPV4 + "|" + FORMAT_IPV6;

    public static final String FORMAT_CLF_IP = FORMAT_IP + "|-";
    public static final String FORMAT_STRING = ".*";
    public static final String FORMAT_NO_SPACE_STRING = "[^\\s]*";
    public static final String FIXED_STRING = "FIXED_STRING";

    // This expression "forces" a year in the range [1000-9999] :).
    public static final String FORMAT_STANDARD_TIME_US =
            "\\[[0-3][0-9]/(?:[A-Z][a-z][a-z])/[1-9][0-9][0-9][0-9]:[0-9][0-9]:[0-9][0-9]:[0-9][0-9] [\\+|\\-][0-9][0-9][0-9]0\\]";
//    public static final String FORMAT_STANDARD_TIME =
//            "\\[[0-9][0-9]/(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/20[0-9][0-9]:[0-9][0-9]"
//             +":[0-9][0-9]:[0-9][0-9] [\\+|\\-][0-9][0-9][0-9]0\\]";
    // ---------------------------------------

    private final String logFormatToken;
    private final String valueName;
    private final String valueType;
    private final EnumSet<Casts> casts;
    private final String regex;
    private final int prio;

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
        logFormatToken = nLogFormatToken;
        valueName = nValueName;
        valueType = nValueType;
        casts = nCasts;
        regex = nRegex;
        prio = nPrio;
    }

    // --------------------------------------------

    public String getLogFormatToken() {
        return logFormatToken;
    }

    public String getValueName() {
        return valueName;
    }

    public String getValueType() {
        return valueType;
    }

    public EnumSet<Casts> getCasts() {
        return casts;
    }

    public String getRegex() {
        return regex;
    }

    public int getPrio() {
        return prio;
    }

    // --------------------------------------------

    public Token getNextToken(final String logFormat, final int startOffset) {
        final int pos = logFormat.indexOf(logFormatToken, startOffset);
        if (pos == -1) {
            return null;
        }

        return new Token(
                valueName,
                valueType,
                casts,
                regex,
                pos, logFormatToken.length(),
                prio);
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

}
