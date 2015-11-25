/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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
package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.httpdlog.dissectors.HttpFirstLineDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({
    "PMD.LongVariable", // I like my variable names this way
    "PMD.CyclomaticComplexity", "PMD.OnlyOneReturn",
    "PMD.BeanMembersShouldSerialize", // No beans here
    "PMD.DataflowAnomalyAnalysis" // Results in a lot of mostly useless messages.
    })
public final class ApacheHttpdLogFormatDissector extends TokenFormatDissector {

    private static final Logger LOG = LoggerFactory.getLogger(ApacheHttpdLogFormatDissector.class);

    public ApacheHttpdLogFormatDissector(final String logFormat) {
        super(logFormat);
        setInputType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    public ApacheHttpdLogFormatDissector() {
        super();
        setInputType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    private void overrideLogFormat(String originalLogformat, String logformat){
        LOG.debug("Specified logformat \"" + originalLogformat + "\" was mapped to " + logformat);
        super.setLogFormat(logformat);
    }

    @Override
    public void setLogFormat(final String logformat) {
        // Commonly used logformats as documented in the manuals of the Apache Httpd
        // LogFormat "%h %l %u %t \"%r\" %>s %b" common
        // LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined
        // LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %I %O" combinedio
        // LogFormat "%{Referer}i -> %U" referer
        // LogFormat "%{User-agent}i" agent

        switch (logformat.toLowerCase(Locale.getDefault())) {
            case "common":
                overrideLogFormat(logformat, "%h %l %u %t \"%r\" %>s %b");
                break;
            case "combined":
                overrideLogFormat(logformat, "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"");
                break;
            case "combinedio":
                overrideLogFormat(logformat, "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %I %O");
                break;
            case "referer":
                overrideLogFormat(logformat, "%{Referer}i -> %U");
                break;
            case "agent":
                overrideLogFormat(logformat, "%{User-agent}i");
                break;
            default:
                super.setLogFormat(logformat);
                break;
        }
    }

    // --------------------------------------------

    protected String makeHeaderNamesLowercaseInLogFormat(String logformat) {
        // In vim I would simply do: %s@{\([^}]*\)}@{\L\1\E@g
        // But such an expression is not (yet) possible in Java
        StringBuffer sb = new StringBuffer(logformat.length());
        Pattern p = Pattern.compile("%\\{([^\\}]*)\\}");
        Matcher m = p.matcher(logformat);
        while (m.find()) {
            m.appendReplacement(sb, "%{"+m.group(1).toLowerCase()+'}');
        }
        m.appendTail(sb);

        return sb.toString();
    }

    protected String removeModifiersFromLogformat(String tokenLogFormat) {
        // Modifiers
        // Particular items can be restricted to print only for responses with specific HTTP status codes
        // by placing a comma-separated list of status codes immediately following the "%".
        // The status code list may be preceded by a "!" to indicate negation.
        //
        // %400,501{User-agent}i     Logs User-agent on 400 errors and 501 errors only.
        //                           For other status codes, the literal string "-" will be logged.
        // %!200,304,302{Referer}i   Logs Referer on all requests that do not return one of the three
        //                           specified codes, "-" otherwise.

        return tokenLogFormat.replaceAll("%!?[0-9]{3}(?:,[0-9]{3})*", "%");
    }

    protected String fixTimestampFormat(String tokenLogFormat) {
        // The %t is mapped to the actual time format surrounded by '[' ']'
        // We generate the [] around it and in the rest of the parsing
        // work with the clean format.
        // This is mainly needed to ensure reuse in conjunction with Nginx parsing.
        return tokenLogFormat.replaceAll("%t", "[%t]");

    }

    @Override
    protected String cleanupLogFormat(String tokenLogFormat) {
        String result = removeModifiersFromLogformat(tokenLogFormat);
        result =  makeHeaderNamesLowercaseInLogFormat(result);
        result = fixTimestampFormat(result);
        return result;
    }

    @Override
    public String decodeExtractedValue(String tokenName, String value) {
        if (value == null || value.equals("")) {
            return value;
        }

        // In Apache logfiles a '-' means a 'not specified' / 'empty' value.
        if (value.equals("-")){
            return null;
        }

        // http://httpd.apache.org/docs/current/mod/mod_log_config.html#formats
        // Format Notes
        // For security reasons, starting with version 2.0.46, non-printable and other special characters
        // in %r, %i and %o are escaped using \xhh sequences, where hh stands for the hexadecimal representation of
        // the raw byte. Exceptions from this rule are " and \, which are escaped by prepending a backslash, and
        // all whitespace characters, which are written in their C-style notation (\n, \t, etc).
        // In versions prior to 2.0.46, no escaping was performed on these strings so you had to be quite careful
        // when dealing with raw log files.

        if (value.equals("request.firstline")   ||  // %r         First line of request.
            value.startsWith("request.header.") ||  // %{Foobar}i The contents of Foobar: request header line(s).
            value.startsWith("response.header.")) { // %{Foobar}o The contents of Foobar: response header line(s).
            return Utils.decodeApacheHTTPDLogValue(value);
        }

        return value;
    }

    // --------------------------------------------
    @Override
    protected List<TokenParser> createAllTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // Quote from
        // http://httpd.apache.org/docs/2.2/mod/mod_log_config.html#logformat
        // Format String Description
        // -------
        // %% The percent sign
        parsers.add(new FixedStringTokenParser("%%", "%"));

        // -------
        // %a Remote IP-address
        parsers.add(new TokenParser("%a",
                "connection.client.ip", "IP",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));

        // %{c}a Underlying peer IP address of the connection (see the mod_remoteip module).
        parsers.add(new TokenParser("%{c}a",
                "connection.client.peerip", "IP",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));

        // -------
        // %A Local IP-address
        parsers.add(new TokenParser("%A",
                "connection.server.ip", "IP",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));

        // -------
        // %B Size of response in bytes, excluding HTTP headers.
        parsers.add(new TokenParser("%B",
                "response.body.bytes", "BYTES",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %b Size of response in bytes, excluding HTTP headers. In CLF format,
        // i.e. a '-' rather than a 0 when no bytes are sent.
        parsers.add(new TokenParser("%b",
                "response.body.bytesclf", "BYTES",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // %{Foobar}C The contents of cookie Foobar in the request sent to the server.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}C",
                "request.cookies.", "HTTP.COOKIE",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %D The time taken to serve the request, in microseconds.
        parsers.add(new TokenParser("%D",
                "server.process.time", "MICROSECONDS",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %{FOOBAR}e The contents of the environment variable FOOBAR
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}e", "server.environment.", "VARIABLE",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %f Filename
        parsers.add(new TokenParser("%f",
                "server.filename", "FILENAME",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));
        // -------

        // %h Remote host
        parsers.add(new TokenParser("%h",
                "connection.client.host", "IP",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %H The request protocol
        parsers.add(new TokenParser("%H",
                "request.protocol", "PROTOCOL",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %{Foobar}i The contents of Foobar: header line(s) in the request sent
        // to the server. Changes made by other modules (e.g. mod_headers)
        // affect this.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}i",
                "request.header.", "HTTP.HEADER",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %k Number of keepalive requests handled on this connection.
        // Interesting if KeepAlive is being used, so that, for example,
        // a '1' means the first keepalive request after the initial one, '
        // 2' the second, etc...;
        // otherwise this is always 0 (indicating the initial request).
        // Available in versions 2.2.11 and later.
        parsers.add(new TokenParser("%k",
                "connection.keepalivecount", "NUMBER",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %l Remote logname (from identd, if supplied). This will return a dash
        // unless mod_ident is present and IdentityCheck is set On.
        parsers.add(new TokenParser("%l",
                "connection.client.logname", "NUMBER",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // %L The request log ID from the error log (or '-' if nothing has been logged to the error log for this request).
        // Look for the matching error log line to see what request caused what error.
        parsers.add(new TokenParser("%L",
                "request.errorlogid", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %m The request method
        parsers.add(new TokenParser("%m",
                "request.method", "HTTP.METHOD",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %{Foobar}n The contents of note Foobar from another module.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}n",
                "server.module_note.", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %{Foobar}o The contents of Foobar: header line(s) in the response.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-]*)\\}o",
                "response.header.", "HTTP.HEADER",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %p The canonical port of the server serving the request
        parsers.add(new TokenParser("%p",
                "request.server.port.canonical", "PORT",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %{format}p The canonical port of the server serving the request or
        // the server's actual port or the client's actual port. Valid formats
        // are canonical, local, or remote.
        parsers.add(new TokenParser("%{canonical}p",
                "connection.server.port.canonical", "PORT",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{local}p",
                "connection.server.port", "PORT",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{remote}p",
                "connection.client.port", "PORT",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %P The process ID of the child that serviced the request.
        parsers.add(new TokenParser("%P",
                "connection.server.child.processid", "NUMBER",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %{format}P The process ID or thread id of the child that serviced the
        // request. Valid formats are pid, tid, and hextid. hextid requires
        // APR 1.2.0 or higher.
        parsers.add(new TokenParser("%{pid}P",
                "connection.server.child.processid", "NUMBER",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{tid}P",
                "connection.server.child.threadid", "NUMBER",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{hextid}P",
                "connection.server.child.hexthreadid", "NUMBER",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_HEXNUMBER));

        // -------
        // %q The query string (prepended with a ? if a query string exists,
        // otherwise an empty string)
        parsers.add(new TokenParser("%q",
                "request.querystring", "HTTP.QUERYSTRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %r First line of request
        parsers.add(new TokenParser("%r",
                "request.firstline", "HTTP.FIRSTLINE",
                Casts.STRING_ONLY, HttpFirstLineDissector.FIRSTLINE_REGEX));

        // -------
        // %R The handler generating the response (if any).
        parsers.add(new TokenParser("%R",
                "request.handler", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %s Status. For requests that got internally redirected, this is the
        // status of the *original* request --- %>s for the last.
        parsers.add(new TokenParser("%s",
                "request.status.original", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        parsers.add(new TokenParser("%>s",
                "request.status.last", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %t Time the request was received (standard english format)
        parsers.add(new TokenParser("%t",
                "request.receive.time", "TIME.STAMP",
                Casts.STRING_ONLY, TokenParser.FORMAT_STANDARD_TIME_US));

        // %{format}t The time, in the form given by format, which should be in
        // strftime(3) format. (potentially localized)
        // FIXME: Implement %{format}t "should be in strftime(3) format. (potentially localized)"
        // If the format starts with begin: (default) the time is taken at the beginning of the request processing.
        // If it starts with end: it is the time when the log entry gets written, close to the end of the request processing.
        // In addition to the formats supported by strftime(3), the following format tokens are supported:
        // sec number of seconds since the Epoch
        // msec number of milliseconds since the Epoch
        // usec number of microseconds since the Epoch
        // msec_frac millisecond fraction
        // usec_frac microsecond fraction
        // These tokens can not be combined with each other or strftime(3) formatting in the same format string.
        // You can use multiple %{format}t tokens instead.

        // msec
        parsers.add(new TokenParser("%{msec}t",
                "request.receive.time.begin.msec", "TIME.EPOCH",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{begin:msec}t",
                "request.receive.time.begin.msec", "TIME.EPOCH",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{end:msec}t",
                "request.receive.time.end.msec", "TIME.EPOCH",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // usec
        parsers.add(new TokenParser("%{usec}t",
                "request.receive.time.begin.usec", "TIME.EPOCH.USEC",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{begin:usec}t",
                "request.receive.time.begin.usec", "TIME.EPOCH.USEC",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{end:usec}t",
                "request.receive.time.end.usec", "TIME.EPOCH.USEC",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // msec_frac
        parsers.add(new TokenParser("%{msec_frac}t",
                "request.receive.time.begin.msec_frac", "TIME.EPOCH",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{begin:msec_frac}t",
                "request.receive.time.begin.msec_frac", "TIME.EPOCH",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{end:msec_frac}t",
                "request.receive.time.end.msec_frac", "TIME.EPOCH",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // usec_frac
        parsers.add(new TokenParser("%{usec_frac}t",
                "request.receive.time.begin.usec_frac", "TIME.EPOCH.USEC_FRAC",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{begin:usec_frac}t",
                "request.receive.time.begin.usec_frac", "TIME.EPOCH.USEC_FRAC",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        parsers.add(new TokenParser("%{end:usec_frac}t",
                "request.receive.time.end.usec_frac", "TIME.EPOCH.USEC_FRAC",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // This next parser is created to deliberately cause an error when it is used !!!
        parsers.add(new NamedTokenParser("\\%\\{([^\\}]*)\\}t", "", "", null,
                " ])========== %{format}t is not fully supported ==========[( ", -1)
        );

        // -------
        // %T The time taken to serve the request, in seconds.
        parsers.add(new TokenParser("%T",
                "response.server.processing.time", "SECONDS",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

        // -------
        // %u Remote user (from auth; may be bogus if return status (%s) is 401)
        parsers.add(new TokenParser("%u",
                "connection.client.user", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // %U The URL path requested, not including any query string.
        parsers.add(new TokenParser("%U",
                "request.urlpath", "URI",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %v The canonical ServerName of the server serving the request.
        parsers.add(new TokenParser("%v",
                "connection.server.name.canonical", "STRING",
                Casts.STRING_ONLY,  TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %V The server name according to the UseCanonicalName setting.
        parsers.add(new TokenParser("%V",
                "connection.server.name", "STRING",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %X Connection status when response is completed:
        // X = connection aborted before the response completed.
        // + = connection may be kept alive after the response is sent.
        // - = connection will be closed after the response is sent.
        // (This directive was %c in late versions of Apache 1.3, but this
        // conflicted with the historical ssl %{var}c syntax.)
        parsers.add(new TokenParser("%X",
                "response.connection.status", "HTTP.CONNECTSTATUS",
                Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

        // -------
        // %I Bytes received, including request and headers, cannot be zero. You
        // need to enable mod_logio to use this.
        parsers.add(new TokenParser("%I",
                "request.bytes", "BYTES",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NON_ZERO_NUMBER));

        // -------
        // %O Bytes sent, including headers, cannot be zero. You need to enable
        // mod_logio to use this.
        parsers.add(new TokenParser("%O",
                "response.bytes", "BYTES",
                Casts.STRING_OR_LONG, TokenParser.FORMAT_NON_ZERO_NUMBER));

        // -------
        // %S Bytes transferred (received and sent), including request and headers, cannot be zero.
        // This is the combination of %I and %O. You need to enable mod_logio to use this.
        // TODO: Implement %S. I have not been able to test this one yet.
//        parsers.add(new TokenParser("%S",
//                "total.bytes", "BYTES",
//                Casts.STRING_OR_LONG, TokenParser.FORMAT_NON_ZERO_NUMBER));

        // -------
        // %{VARNAME}^ti The contents of VARNAME: trailer line(s) in the request sent to the server.
        // TODO: Implement %{VARNAME}^ti

        // -------
        // %{VARNAME}^to The contents of VARNAME: trailer line(s) in the response sent from the server.
        // TODO: Implement %{VARNAME}^to

        // Some explicit type overrides.
        // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
        parsers.add(new TokenParser("%{cookie}i"    ,
                "request.cookies" ,   "HTTP.COOKIES"   ,
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("%{set-cookie}o",
                "response.cookies",   "HTTP.SETCOOKIES",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("%{user-agent}i",
                "request.user-agent", "HTTP.USERAGENT",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("%{referer}i",
                "request.referer",    "HTTP.URI",
                Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));

        return parsers;
    }
}
