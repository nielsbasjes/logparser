/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
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
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.basjes.parse.core.Casts.STRING_ONLY;

@SuppressWarnings({
        "PMD.LongVariable", // I like my variable names this way
        "PMD.CyclomaticComplexity", "PMD.OnlyOneReturn",
        "PMD.BeanMembersShouldSerialize", // No beans here
        "PMD.DataflowAnomalyAnalysis" // Results in a lot of mostly useless messages.
    })
public abstract class TokenFormatDissector extends Dissector {

    private static final Logger LOG = LoggerFactory.getLogger(TokenFormatDissector.class);

    private String       logFormat           = null;
    private ArrayList<Token>  logFormatUsedTokens = null; // Using ArrayList because it is Serializable
    private String       logFormatRegEx      = null;
    private Pattern      logFormatPattern    = null;
    private boolean      isUsable            = false;

    private List<Token>  logFormatTokens;

    private List<String> outputTypes;

    // --------------------------------------------
    public static class FixedStringTokenParser extends TokenParser {
        public FixedStringTokenParser(final String nLogFormatToken, final String nRegEx) {
            super(nLogFormatToken, nRegEx, 0);
        }

        @Override
        public Token getNextToken(String logFormat, int startOffset) {
            final int pos = logFormat.indexOf(getLogFormatToken(), startOffset);
            if (pos == -1) {
                return null;
            }

            return new FixedStringToken(
                getRegex(),
                pos,
                getLogFormatToken().length(),
                0)
                .addOutputFields(getOutputFields());
        }
    }

    public static class FixedStringToken extends Token {
        public FixedStringToken(String nRegex, int nStartPos, int nLength, int nPrio) {
            super(nRegex, nStartPos, nLength, nPrio);
        }
    }

    // --------------------------------------------

    public static class NotImplementedTokenParser extends TokenParser {

        public NotImplementedTokenParser(final String nLogFormatToken, final String fieldPrefix, int nPrio) {
            this(nLogFormatToken, fieldPrefix, ".*", nPrio);
        }

        public NotImplementedTokenParser(final String nLogFormatToken, final String fieldPrefix, final String regEx, int nPrio) {
            super(nLogFormatToken,
                fieldPrefix + "_" + nLogFormatToken.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_]", "_"),
                "NOT_IMPLEMENTED",
                STRING_ONLY,
                regEx,
                nPrio);
        }
    }

    public TokenFormatDissector(final String logFormat) {
        setLogFormat(logFormat);
    }

    public TokenFormatDissector() {
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        setLogFormat(logFormat);
        return true; // Everything went right
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        if (newInstance instanceof TokenFormatDissector) {
            ((TokenFormatDissector)newInstance).setLogFormat(logFormat);
        } else {
            LOG.error("============================== WTF == {}", newInstance.getClass().getCanonicalName());
        }
    }

    public void setLogFormat(final String logformat) {
        this.logFormat = logformat;

        // Now we disassemble the format into parts
        logFormatTokens = parseTokenLogFileDefinition(this.logFormat);

        outputTypes = new ArrayList<>();

        for (final Token token : logFormatTokens) {
            if (token instanceof FixedStringToken) {
                continue;
            }

            List<TokenOutputField> outputFields = token.getOutputFields();
            if (!outputFields.isEmpty()) {
                for (TokenOutputField tokenOutputField: outputFields) {
                    outputTypes.add(tokenOutputField.getType() + ':' + tokenOutputField.getName());
                }
            }
        }
    }

    public String getLogFormat() {
        return logFormat;
    }

    @SuppressWarnings("unused") // Useful for debugging purposes
    public String getLogFormatRegEx() {
        return logFormatRegEx;
    }

    // --------------------------------------------

    private final Set<String> requestedFields = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputName, final String outputName) {
        requestedFields.add(outputName);
        for (Token token: logFormatTokens) {
            for (TokenOutputField tokenOutputField: token.getOutputFields()) {
                if (outputName.equals(tokenOutputField.getName())) {
                    tokenOutputField.wasUsed();
                    return tokenOutputField.getCasts();
                }
            }
        }
        return STRING_ONLY;
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // At this point we have all the tokens and now we construct the
        // complete regex and the list to use when extracting
        // We build the regexp so that it only extracts the needed parts.

        // Allocated buffer is a bit bigger than needed
        final StringBuilder regex = new StringBuilder(logFormatTokens.size() * 16);

        logFormatUsedTokens = new ArrayList<>();

        regex.append('^'); // Link to start of the line
        for (final Token token : logFormatTokens) {
            token.tokenWasUsed();
            if (token instanceof FixedStringToken) {
                // Only insert the fixed part
                regex.append(Pattern.quote(token.getRegex()));
            } else if (token.canProduceADesiredFieldName(requestedFields)) {
                logFormatUsedTokens.add(token);
                regex.append("(").append(token.getRegex()).append(")");
            } else {
                regex.append("(?:").append(token.getRegex()).append(")");
            }

        }
        regex.append('$'); // Link to end of the line

        logFormatRegEx = regex.toString();
        LOG.debug("Source logformat : {}", logFormat);
        LOG.debug("Used regex       : {}", logFormatRegEx);

        // Now we compile this expression ONLY ONCE!
        logFormatPattern = Pattern.compile(logFormatRegEx);

        isUsable = true; // Ready!
    }

    // --------------------------------------------

    @Override
    public void setInputType(String newInputType) {
        this.inputType = newInputType;
    }

    private String inputType = null;

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public List<String> getPossibleOutput() {
        return outputTypes;
    }

    /**
     *
     * @param tokenName Name of the token that was found
     * @param value The actual value as it is present in the logline
     * @return The cleaned/decoded/interpreted version of the value.
     */
    public abstract String decodeExtractedValue(String tokenName, String value);

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        if (!isUsable) {
            throw new DissectionFailure("Dissector in unusable state");
        }

        final ParsedField line = parsable.getParsableField(inputType, inputname);

        // Now we create a matcher for this line
        final Matcher matcher = logFormatPattern.matcher(line.getValue().getString());

        // Is it all as expected?
        final boolean matches = matcher.find();

        if (matches) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String matchedStr = matcher.group(i);
                Token token = logFormatUsedTokens.get(i-1);
                for (TokenOutputField tokenOutputField: token.getOutputFields()) {
                    final String matchedName = tokenOutputField.getName();
                    final String matchedType = tokenOutputField.getType();

                    parsable.addDissection(inputname, matchedType, matchedName,
                        decodeExtractedValue(matchedName, matchedStr));
                }
            }
        } else {
            throw new DissectionFailure("The input line does not match the specified log format." +
                    "Line     : " + line.getValue() + "\n" +
                    "LogFormat: " + logFormat       + "\n" +
                    "RegEx    : " + logFormatRegEx);
        }

    }

    // --------------------------------------------

    /**
     * This should be overridden if there is a need to cleanup the
     * actual logformat before parsing.
     * @param tokenLogFormat the 'dirty' logformat
     * @return the cleaned version of the tokenLogFormat.
     */
    protected String cleanupLogFormat(String tokenLogFormat){
        return tokenLogFormat;
    }

    // --------------------------------------------
    @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops",
            "PMD.LongVariable", "PMD.ExcessiveMethodLength",
            "PMD.DataflowAnomalyAnalysis", "PMD.NcssMethodCount",
            "PMD.NPathComplexity" })
    private List<Token> parseTokenLogFileDefinition(final String tokenLogFormat) {

        // Add all available parsers
        final List<TokenParser> tokenParsers = createAllTokenParsers();
        final List<Token> tokens = new ArrayList<>(50);

        // We first change all the references to headers to lowercase
        // because we must handle these as "case insensitive"
        String cleanedTokenLogFormat = cleanupLogFormat(tokenLogFormat);

        // Now we let all tokens figure out if they are present in here
        for (TokenParser tokenParser : tokenParsers) {
            List<Token> newTokens = tokenParser.getTokens(cleanedTokenLogFormat);
            if (newTokens != null) {
                tokens.addAll(newTokens);
            }
        }

        // We now have a full list of all matched tokens
        // ---------------------------------------
        // We sort them by position of the token in the format specifier
        tokens.sort(new TokenSorterByStartPos());

        // First we take out the duplicates with a lower prio(=relevance score)
        final List<Token> kickTokens = new ArrayList<>(50);
        Token prevToken = null;
        for (Token token : tokens) {
            if (prevToken==null){
                prevToken=token;
                continue;
            }

            if (prevToken.getStartPos() == token.getStartPos()) {
                if (prevToken.getLength() == token.getLength()) {
                    if (prevToken.getPrio() < token.getPrio()) {
                        kickTokens.add(prevToken);
                    } else {
                        kickTokens.add(token);
                    }
                } else {
                    if (prevToken.getLength() < token.getLength()) {
                        kickTokens.add(prevToken);
                    } else {
                        kickTokens.add(token);
                    }
                }
            } else {
                // Sometimes we find that a part of a token matches another token aswell.
                // Example: %{%H}t    Custom Timeformat (only the hour) also matches the protocol token.
                // So we kick them of they overlap
                if (prevToken.getStartPos() + prevToken.getLength() > token.getStartPos()) {
                    kickTokens.add(token);
                    continue;
                }
            }
            prevToken=token;

        }

        tokens.removeAll(kickTokens);

        final List<Token> allTokens = new ArrayList<>(50);
        // We now look for the holes and add "FIXED STRING" tokens
        int tokenBegin;
        int tokenEnd = 0;
        for (Token token : tokens) {
            tokenBegin = token.getStartPos();
            // Space between the begin of the next token and the end of the previous token?
            if (tokenBegin - tokenEnd > 0) {
                String separator = cleanedTokenLogFormat.substring(tokenEnd, tokenBegin);
                Token fixedStringToken = new FixedStringToken(separator, tokenBegin, tokenBegin - tokenEnd, 0);
                allTokens.add(fixedStringToken);
            }
            allTokens.add(token);
            tokenEnd = tokenBegin + token.getLength();
        }

        int logFormatLength = cleanedTokenLogFormat.length();
        if (tokenEnd < logFormatLength) {
            String separator = cleanedTokenLogFormat.substring(tokenEnd);
            Token fixedStringToken = new FixedStringToken(separator, tokenEnd, cleanedTokenLogFormat.length() - tokenEnd, 0);
            allTokens.add(fixedStringToken);
        }

        return allTokens;
    }


    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        for (Token token: logFormatTokens) {
            parser.addDissector(token.getCustomDissector());
        }
    }

    // --------------------------------------------
    protected abstract List<TokenParser> createAllTokenParsers();
}
