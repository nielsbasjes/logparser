/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
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

package nl.basjes.parse.apachehttpdlog.logformat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NamedTokenParser extends TokenParser {

    private Pattern pattern;

    // --------------------------------------------

    public NamedTokenParser(final String nLogFormatToken,
            final String nValueName,
            final String nValueType,
            final String nRegex) {
        super(nLogFormatToken, nValueName, nValueType, nRegex);

        // Compile the regular expression
        pattern = Pattern.compile(getLogFormatToken());
    }

    // --------------------------------------------

    @Override
    public Token getNextToken(final String logFormat, final int startOffset) {
        final Matcher matcher = pattern.matcher(logFormat.substring(startOffset));
        if (!matcher.find()) {
            return null;
        }

        // Retrieve the name
        final String fieldname = matcher.group(1);

        // Retrieve indices of matching string
        final int start = matcher.start();
        final int end = matcher.end();
        // the end is index of the last matching character + 1

        return new Token(
                getValueName() + fieldname,
                getValueType(),
                getRegex(),
                startOffset + start, end - start);
    }

    // --------------------------------------------

}
