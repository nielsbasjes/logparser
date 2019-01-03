/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a TokenParser where we expect to get the constructor parameter for the provided dissector as a group in
 * the regular expression. As a consequence the type is a generated string to match THIS dissector instance.
 */
public class ParameterizedTokenParser extends TokenParser {

    private final Pattern pattern;

    // --------------------------------------------

    public ParameterizedTokenParser(
            final String nLogFormatToken,
            final String nValueName,
            final String nValueType,
            final EnumSet<Casts> nCasts,
            final String nRegex,
            final int prio,
            final Dissector customDissector) {
        super(nLogFormatToken, nValueName, nValueType, nCasts, nRegex, prio, customDissector);

        // Compile the regular expression
        pattern = Pattern.compile(getLogFormatToken());
    }

    @Override
    public TokenParser addOutputField(String type, String name, EnumSet<Casts> casts) {
        if (getOutputFields().isEmpty()) {
            return super.addOutputField(type, name, casts);
        }
        throw new UnsupportedOperationException("A ParameterizedTokenParser only supports ONE outputfield.");
    }

    @Override
    public TokenParser addOutputFields(List<TokenOutputField> outputFields) {
        if (getOutputFields().isEmpty()) {
            return super.addOutputFields(outputFields);
        }
        throw new UnsupportedOperationException("A ParameterizedTokenParser only supports ONE outputfield.");
    }

    // --------------------------------------------

    @Override
    public Token getNextToken(final String logFormat, final int startOffset) {
        final Matcher matcher = pattern.matcher(logFormat.substring(startOffset));
        if (!matcher.find()) {
            return null;
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


        Token token = new Token(
            getRegex(),
            startOffset + start, end - start,
            getPrio());

        for (TokenOutputField tokenOutputField: getOutputFields()) {
            String fieldType = tokenParameterToTypeName(fieldName);
            token.addOutputField(
                tokenParameterToTypeName(fieldName),
                tokenOutputField.getName(),
                tokenOutputField.getCasts());
            addCustomDissector(token, fieldType, fieldName);
        }

        if (warningMessageWhenUsed != null) {
            token.setWarningMessageWhenUsed(warningMessageWhenUsed.replaceFirst("\\{}", fieldName));
        }

        return token;
    }

    // --------------------------------------------

    String tokenParameterToTypeName(String parameter) {
        return (
                getOutputFields().get(0).getType() +
                parameter.replaceAll("[^A-Za-z0-9]", "") +
                "_" + stringHashAsHexString(parameter)
            ).toUpperCase(Locale.ENGLISH);
    }

    private String stringHashAsHexString(String input) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            result = Hex.encodeHexString(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            // Shouldn't happen
        }
        return result;
    }

}
