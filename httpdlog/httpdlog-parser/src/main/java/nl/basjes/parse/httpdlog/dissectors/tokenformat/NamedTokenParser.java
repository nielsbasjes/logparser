/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import nl.basjes.parse.core.Casts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedTokenParser extends TokenParser {

    private static final Logger LOG = LoggerFactory.getLogger(NamedTokenParser.class);
    private final Pattern pattern;

    // --------------------------------------------

    public NamedTokenParser(
            final String nLogFormatToken,
            final String nValueName,
            final String nValueType,
            final EnumSet<Casts> nCasts,
            final String nRegex) {
        this(nLogFormatToken, nValueName, nValueType, nCasts, nRegex, 0);
    }

    public NamedTokenParser(
            final String nLogFormatToken,
            final String nValueName,
            final String nValueType,
            final EnumSet<Casts> nCasts,
            final String nRegex,
            final int prio) {
        super(nLogFormatToken, nValueName, nValueType, nCasts, nRegex, prio);

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

        if (warningMessageWhenUsed != null) {
            LOG.warn(warningMessageWhenUsed);
        }

        String fieldName = "";
        if (matcher.groupCount() > 0) {
            // Retrieve the name
            fieldName = matcher.group(1);
        }

        // Retrieve indices of matching string
        final int start = matcher.start();
        final int end = matcher.end();
        // the end is index of the last matching character + 1

        return new Token(
                getValueName() + fieldName,
                getValueType(),
                getCasts(),
                getRegex(),
                startOffset + start, end - start,
                getPrio());
    }

    // --------------------------------------------

}
