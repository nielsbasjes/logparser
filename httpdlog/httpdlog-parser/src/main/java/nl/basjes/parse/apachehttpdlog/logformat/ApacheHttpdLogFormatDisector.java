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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@SuppressWarnings({
//        "PMD.LongVariable", // I like my variable names this way
//        "PMD.CyclomaticComplexity", "PMD.OnlyOneReturn", "PMD.BeanMembersShouldSerialize", // No beans here
//        "PMD.DataflowAnomalyAnalysis", // Results in a lot of mostly useless messages.
//})
public final class ApacheHttpdLogFormatDisector extends Disector {

    private static final Logger LOG = LoggerFactory.getLogger(ApacheHttpdLogFormatDisector.class);

    private String       logFormat          = null;
    private List<String> logFormatNames     = null;
    private List<String> logFormatTypes     = null;
    private String       logFormatRegEx     = null;
    private Pattern      logFormatPattern   = null;
    private boolean      isUsable           = false;

    private List<Token> logFormatTokens;

    private String[] outputTypes;

    private static final String FIXED_STRING_TYPE = "NONE";
    
    // --------------------------------------------

    public ApacheHttpdLogFormatDisector(final String logFormat) throws ParseException {
        isUsable = false; // Make sure we classify as unusable while we're busy
        setLogFormat(logFormat);
    }

    public ApacheHttpdLogFormatDisector(){
        isUsable = false; // Make sure we classify as unusable while we're busy
    }

    @Override
    protected void initializeNewInstance(Disector newInstance) {
        if (newInstance instanceof ApacheHttpdLogFormatDisector) {
            try {
                ((ApacheHttpdLogFormatDisector)newInstance).setLogFormat(logFormat);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            LOG.error("============================== WTF == " + newInstance.getClass().getCanonicalName());
        }
    }

    public void setLogFormat(final String logformat) throws ParseException {
        this.logFormat = logformat;
        // LogFormat "%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"" combined
        // LogFormat "%h %l %u %t "%r" %>s %b" common
        // LogFormat "%{Referer}i -> %U" referer
        // LogFormat "%{User-agent}i" agent

        // Now we disassemble the format into parts
        logFormatTokens = parseApacheLogFileDefinition(this.logFormat);

        List<String> outputTypesList = new ArrayList<String>(20);

        for (final Token token : logFormatTokens) {
            String type = token.getType();
            if (FIXED_STRING_TYPE.equals(type)) {
                continue;
            }

            outputTypesList.add(token.getType() + ':' + token.getName());
        }

        outputTypes = outputTypesList.toArray(new String[0]);
    }

    // --------------------------------------------

    private Map<String, Set<String>> expectedDisections = new HashMap<String, Set<String>>();

    @Override
    public void prepareForDisect(String inputname, String outputname) {
        Set<String> currentSet = expectedDisections.get(inputname);
        if (currentSet == null) {
            currentSet = new HashSet<String>();
        }
        currentSet.add(outputname);
        expectedDisections.put(inputname, currentSet);
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() throws InvalidDisectorException {
        // At this point we have all the tokens and now we construct the
        // complete regex and the list to use when extracting
        // We build the regexp so that it only extracts the needed parts.

        // TODO: Make it possible for multiple input subtrees
        // We enforce here that there can be only one.
        if (expectedDisections.size() > 1){
            throw new InvalidDisectorException();
        }

        Set<String> requestedOutput;
        if (expectedDisections.size() == 0) { // Special edge case if there is NOTHING requested from this disector
            requestedOutput = new HashSet<String>();
        } else {
            // For now: only one possible
            requestedOutput = expectedDisections.get(expectedDisections.keySet().iterator().next());
        }

        // Allocated buffer is a bit bigger than needed
        final StringBuilder regex = new StringBuilder(logFormatTokens.size() * 16);

        logFormatNames = new ArrayList<String>(20);
        logFormatTypes = new ArrayList<String>(20);

        regex.append('^'); // Link to start of the line
        for (final Token token : logFormatTokens) {
            if (TokenParser.FIXED_STRING.equals(token.getType())) {
                // Only insert the fixed part
                regex.append(Pattern.quote(token.getRegex()));
            } else if (requestedOutput.contains(token.getName())) {
                logFormatNames.add(token.getName());
                logFormatTypes.add(token.getType());
                regex.append("(").append(token.getRegex()).append(")");
            } else {
                regex.append("(?:").append(token.getRegex()).append(")");
            }

        }
        regex.append('$'); // Link to end of the line

        logFormatRegEx = regex.toString();
//        System.out.println("APACHE LOG REGEX: "+logFormatRegEx);

        // Now we compile this expression ONLY ONCE!
        logFormatPattern = Pattern.compile(logFormatRegEx);

        isUsable = true; // Ready!
    }

    // --------------------------------------------

    private static final String INPUT_TYPE = "APACHELOGLINE";
    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public String[] getPossibleOutput() {
        return outputTypes;
    }

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) throws DisectionFailure {
        if (!isUsable) {
            throw new DisectionFailure("Disector in unusable state");
        }

        final ParsedField line = parsable.getParsableField(INPUT_TYPE, inputname);

        // Now we create a matcher for this line
        final Matcher matcher = logFormatPattern.matcher(line.getValue());

        // Is it all as expected?
        final boolean matches = matcher.find();

        if (matches) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String matchedStr = matcher.group(i);
                final String matchedName = logFormatNames.get(i - 1);
                final String matchedType = logFormatTypes.get(i - 1);

                // In Apache logfiles a '-' means empty value.
                if (matchedStr.equals("-")){
                    matchedStr="";
                }
                parsable.addDisection(inputname, matchedType, matchedName, matchedStr);
            }
        } else {
            throw new DisectionFailure("The input line :\n"+line.getValue()+"\n" +
                                       "does not match the specified apache log format RegEx:\n" + logFormatRegEx);
        }

    }

    // --------------------------------------------

    public static String makeHeaderNamesLowercaseInLogFormat(String logformat) {
        // In vim I would simply do: %s@{\([^}]*\)}@{\L\1\E@g
        // But such an expression is not (yet) possible in Java
        StringBuffer sb = new StringBuffer(logformat.length());
        Pattern p = Pattern.compile("\\{([^\\}]*)\\}");
        Matcher m = p.matcher(logformat);
        while (m.find()) {
            m.appendReplacement(sb, '{'+m.group(1).toLowerCase()+'}');
        }
        m.appendTail(sb);

        return sb.toString();
    }

    // --------------------------------------------
    @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops",
            "PMD.LongVariable", "PMD.ExcessiveMethodLength",
            "PMD.DataflowAnomalyAnalysis", "PMD.NcssMethodCount",
            "PMD.NPathComplexity" })
    private List<Token> parseApacheLogFileDefinition(final String apacheLogFormat) throws ParseException {

        // Add all available parsers
        final List<TokenParser> tokenParsers = createAllTokenParsers();
        final List<Token> tokens = new ArrayList<Token>(50);

        // We first change all the references to headers to lowercase
        // because we must handle these as "case insensitive"
        String cleanedApacheLogFormat = makeHeaderNamesLowercaseInLogFormat(apacheLogFormat);
        
        // Now we let all tokens figure out if they are present in here
        for (TokenParser tokenParser : tokenParsers) {
            List<Token> newTokens = tokenParser.getTokens(cleanedApacheLogFormat);
            if (newTokens != null) {
                tokens.addAll(newTokens);
            }
        }

        // We now have a full list of all matched tokens
        // ---------------------------------------
        // We sort them by position of the token in the format specifier
        Collections.sort(tokens, new TokenSorterByStartPos());
        
        // First we take out the duplicates with a lower prio(=relevance score)
        final List<Token> kickTokens = new ArrayList<Token>(50);
        Token prevToken = null;
        for (Token token : tokens) {
            if (prevToken==null){
                prevToken=token;
                continue;
            }

            if (prevToken.getStartPos() == token.getStartPos()) {
                if (prevToken.getPrio() < token.getPrio()) {
                    kickTokens.add(prevToken);
                } else {
                    kickTokens.add(token);
                    continue;
                }
            }
            prevToken=token;

        }

        tokens.removeAll(kickTokens);

        final List<Token> allTokens = new ArrayList<Token>(50);
        // We now look for the holes and add "FIXED STRING" tokens
        int tokenBegin = 0;
        int tokenEnd = 0;
        for (Token token : tokens) {
            tokenBegin = token.getStartPos();
            // Space between the begin of the next token and the end of the previous token?
            if (tokenBegin - tokenEnd > 0) {
                String separator = cleanedApacheLogFormat.substring(tokenEnd, tokenBegin);
                Token fixedStringToken = new Token(TokenParser.FIXED_STRING, FIXED_STRING_TYPE,
                        separator, tokenBegin, tokenBegin - tokenEnd);
                allTokens.add(fixedStringToken);
            }
            allTokens.add(token);
            tokenEnd = tokenBegin + token.getLength();
        }

        int apacheLogFormatLength = cleanedApacheLogFormat.length();
        if (tokenEnd < apacheLogFormatLength) {
            String separator = cleanedApacheLogFormat.substring(tokenEnd);
            Token fixedStringToken = new Token(TokenParser.FIXED_STRING, FIXED_STRING_TYPE,
                    separator, tokenEnd, cleanedApacheLogFormat.length() - tokenEnd);
            allTokens.add(fixedStringToken);
        }

        return allTokens;
    }

    // --------------------------------------------
    private List<TokenParser> createAllTokenParsers() {
        List<TokenParser> parsers = new ArrayList<TokenParser>(60);

        // Quote from
        // http://httpd.apache.org/docs/2.2/mod/mod_log_config.html#logformat
        // Format String Description
        // -------
        // %% The percent sign
        parsers.add(new TokenParser("%%", TokenParser.FIXED_STRING, TokenParser.FIXED_STRING, "%%"));

        // -------
        // %a Remote IP-address
        parsers.add(new TokenParser("%a", "connection.client.ip", "IP", TokenParser.FORMAT_CLF_IP));

        // -------
        // %A Local IP-address
        parsers.add(new TokenParser("%A", "connection.server.ip", "IP", TokenParser.FORMAT_CLF_IP));

        // -------
        // %B Size of response in bytes, excluding HTTP headers.
        parsers.add(new TokenParser("%B", "response.body.bytes", "BYTES", TokenParser.FORMAT_NUMBER));

        // -------
        // %b Size of response in bytes, excluding HTTP headers. In CLF format,
        // i.e. a '-' rather than a 0 when no bytes are sent.
        parsers.add(new TokenParser("%b", "response.body.bytesclf", "BYTES", TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // %{Foobar}C The contents of cookie Foobar in the request sent to the server.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}C", "request.cookies.", "HTTP.COOKIE", TokenParser.FORMAT_STRING));

        // -------
        // %D The time taken to serve the request, in microseconds.
        parsers.add(new TokenParser("%D", "server.process.time", "MICROSECONDS", TokenParser.FORMAT_NUMBER));

        // -------
        // %{FOOBAR}e The contents of the environment variable FOOBAR
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}e", "server.environment.", "VARIABLE",
                TokenParser.FORMAT_STRING));

        // -------
        // %f Filename
        parsers.add(new TokenParser("%f", "server.filename", "FILENAME", TokenParser.FORMAT_STRING));
        // -------

        // %h Remote host
        parsers.add(new TokenParser("%h", "connection.client.host", "IP", TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %H The request protocol
        parsers.add(new TokenParser("%H", "request.protocol", "PROTOCOL", TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %{Foobar}i The contents of Foobar: header line(s) in the request sent
        // to the server. Changes made by other modules (e.g. mod_headers)
        // affect this.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}i", "request.header.",
                                         "HTTP.HEADER", TokenParser.FORMAT_STRING));

        // -------
        // %k Number of keepalive requests handled on this connection.
        // Interesting if KeepAlive is being used, so that, for example,
        // a '1' means the first keepalive request after the initial one, '
        // 2' the second, etc...;
        // otherwise this is always 0 (indicating the initial request).
        // Available in versions 2.2.11 and later.
        parsers.add(new TokenParser("%k", "connection.keepalivecount", "NUMBER", TokenParser.FORMAT_NUMBER));

        // -------
        // %l Remote logname (from identd, if supplied). This will return a dash
        // unless mod_ident is present and IdentityCheck is set On.
        parsers.add(new TokenParser("%l", "connection.client.logname", "NUMBER", TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // %m The request method
        parsers.add(new TokenParser("%m", "request.method", "HTTP.METHOD", TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // %{Foobar}n The contents of note Foobar from another module.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}n",
                "server.module_note.", "STRING", TokenParser.FORMAT_STRING));

        // -------
        // %{Foobar}o The contents of Foobar: header line(s) in the response.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-]*)\\}o",
                "response.header.", "HTTP.HEADER", TokenParser.FORMAT_STRING));

        // -------
        // %p The canonical port of the server serving the request
        parsers.add(new TokenParser("%p", "request.server.port.canonical", "PORT", TokenParser.FORMAT_NUMBER));

        // -------
        // %{format}p The canonical port of the server serving the request or
        // the server's actual port or the client's actual port. Valid formats
        // are canonical, local, or remote.
        parsers.add(new TokenParser("%{canonical}p", "connection.server.port.canonical", "PORT", TokenParser.FORMAT_NUMBER));
        parsers.add(new TokenParser("%{local}p", "connection.server.port", "PORT", TokenParser.FORMAT_NUMBER));
        parsers.add(new TokenParser("%{remote}p", "connection.client.port", "PORT", TokenParser.FORMAT_NUMBER));

        // -------
        // %P The process ID of the child that serviced the request.
        parsers.add(new TokenParser("%P", "connection.server.child.processid", "NUMBER", TokenParser.FORMAT_NUMBER));

        // -------
        // %{format}P The process ID or thread id of the child that serviced the
        // request. Valid formats are pid, tid, and hextid. hextid requires
        // APR 1.2.0 or higher.
        parsers.add(new TokenParser("%{pid}P", "connection.server.child.processid",
                                    "NUMBER", TokenParser.FORMAT_NUMBER));
        parsers.add(new TokenParser("%{tid}P", "connection.server.child.threadid",
                                    "NUMBER", TokenParser.FORMAT_NUMBER));
        parsers.add(new TokenParser("%{hextid}P", "connection.server.child.hexthreadid",
                                    "NUMBER", TokenParser.FORMAT_CLF_HEXNUMBER));

        // -------
        // %q The query string (prepended with a ? if a query string exists,
        // otherwise an empty string)
        parsers.add(new TokenParser("%q", "request.querystring", "HTTP.QUERYSTRING", TokenParser.FORMAT_STRING));

        // -------
        // %r First line of request
        parsers.add(new TokenParser("%r", "request.firstline", "HTTP.FIRSTLINE", TokenParser.FORMAT_STRING));

        // -------
        // %R The handler generating the response (if any).
        parsers.add(new TokenParser("%R", "request.handler", "STRING", TokenParser.FORMAT_STRING));

        // -------
        // %s Status. For requests that got internally redirected, this is the
        // status of the *original* request --- %>s for the last.
        parsers.add(new TokenParser("%s", "request.status.original", "STRING", TokenParser.FORMAT_NO_SPACE_STRING));
        parsers.add(new TokenParser("%>s", "request.status.last", "STRING", TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %t Time the request was received (standard english format)
        parsers.add(new TokenParser("%t", "request.receive.time", "TIME.STAMP", TokenParser.FORMAT_STANDARD_TIME_US));
        // %{format}t The time, in the form given by format, which should be in
        // strftime(3) format. (potentially localized)
        // FIXME: Implement %{format}t "should be in strftime(3) format. (potentially localized)"

        // -------
        // %T The time taken to serve the request, in seconds.
        parsers.add(new TokenParser("%T", "response.server.processing.time", "SECONDS", TokenParser.FORMAT_NUMBER));

        // -------
        // %u Remote user (from auth; may be bogus if return status (%s) is 401)
        parsers.add(new TokenParser("%u", "connection.client.user", "STRING", TokenParser.FORMAT_STRING));

        // -------
        // %U The URL path requested, not including any query string.
        parsers.add(new TokenParser("%U", "request.urlpath", "URI", TokenParser.FORMAT_STRING));

        // -------
        // %v The canonical ServerName of the server serving the request.
        parsers.add(new TokenParser("%v", "connection.server.name.canonical", "STRING", TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %V The server name according to the UseCanonicalName setting.
        parsers.add(new TokenParser("%V", "connection.server.name", "STRING", TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %X Connection status when response is completed:
        // X = connection aborted before the response completed.
        // + = connection may be kept alive after the response is sent.
        // - = connection will be closed after the response is sent.
        // (This directive was %c in late versions of Apache 1.3, but this
        // conflicted with the historical ssl %{var}c syntax.)
        parsers.add(new TokenParser("%X", "response.connection.status", "HTTP.CONNECTSTATUS", TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %I Bytes received, including request and headers, cannot be zero. You
        // need to enable mod_logio to use this.
        parsers.add(new TokenParser("%I", "request.bytes", "BYTES", TokenParser.FORMAT_NON_ZERO_NUMBER));

        // -------
        // %O Bytes sent, including headers, cannot be zero. You need to enable
        // mod_logio to use this.
        parsers.add(new TokenParser("%O", "response.bytes", "BYTES", TokenParser.FORMAT_NON_ZERO_NUMBER));
        // -------

        // Some explicit type overrides.
        // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
        parsers.add(new TokenParser("%{cookie}i"    , "request.cookies" ,   "HTTP.COOKIES"   , TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("%{set-cookie}o", "response.cookies",   "HTTP.SETCOOKIES", TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("%{user-agent}i", "request.user-agent", "HTTP.USERAGENT",  TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("%{referer}i",    "request.referer",    "HTTP.URI",        TokenParser.FORMAT_STRING, 1));

        return parsers;
    }
}
