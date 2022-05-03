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
import nl.basjes.parse.httpdlog.dissectors.HttpFirstLineDissector;
import nl.basjes.parse.httpdlog.dissectors.StrfTimeStampDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.ParameterizedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenOutputField;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_HEXNUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_IP;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STANDARD_TIME_US;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

@SuppressWarnings({
    "PMD.LongVariable", // I like my variable names this way
    "PMD.CyclomaticComplexity", "PMD.OnlyOneReturn",
    "PMD.BeanMembersShouldSerialize", // No beans here
    "PMD.DataflowAnomalyAnalysis" // Results in a lot of mostly useless messages.
    })
public class ApacheHttpdLogFormatDissector extends TokenFormatDissector {

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
        LOG.debug("Specified logformat \"{}\" was mapped to {}", originalLogformat, logformat);
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

    public static boolean looksLikeApacheFormat(String logFormat) {
        if (logFormat.indexOf('%') != -1) {
            return true;
        }
        switch (logFormat.toLowerCase(Locale.getDefault())) {
            case "common":
            case "combined":
            case "combinedio":
            case "referer":
            case "agent":
                return true;
            default:
                return false;
        }
    }

    // --------------------------------------------

    protected String makeHeaderNamesLowercaseInLogFormat(String logformat) {
        // In vim I would simply do: %s@{\([^}]*\)}@{\L\1\E@g
        // But such an expression is not (yet) possible in Java
        StringBuffer sb = new StringBuffer(logformat.length());

        // All patterns that have a 'name' (note we do NOT do it to %{...}t )
        Pattern p = Pattern.compile("%\\{([^}]*)}([^t])");
        Matcher m = p.matcher(logformat);
        while (m.find()) {
            m.appendReplacement(sb, "%{"+m.group(1).toLowerCase()+'}'+m.group(2));
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
        // NOTE: The %{...}t time format does NOT get the automatic '[' ']' around it.
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

        if (tokenName.equals("request.firstline")       ||  // %r         First line of request.
            tokenName.equals("request.user-agent")      ||  // %{User-Agent}i The contents of the User-Agent request header.
            tokenName.equals("request.user-agent.last") ||  // %{User-Agent}i The contents of the User-Agent request header.
            tokenName.startsWith("request.header.")     ||  // %{Foobar}i The contents of Foobar: request header line(s).
            tokenName.startsWith("response.header."))   {   // %{Foobar}o The contents of Foobar: response header line(s).
            return Utils.decodeApacheHTTPDLogValue(value);
        }

        return value;
    }

    // --------------------------------------------
    @Override
    protected List<TokenParser> createAllTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // Quote from
        // http://httpd.apache.org/docs/current/mod/mod_log_config.html#logformat
        // Format String Description
        // -------
        // %% The percent sign
        parsers.add(new FixedStringTokenParser("%%", "%"));

        // -------
        // %a Remote IP-address
        parsers.addAll(createFirstAndLastTokenParsers("%a",
                "connection.client.ip", "IP",
                STRING_ONLY, FORMAT_CLF_IP));

        // %{c}a Underlying peer IP address of the connection (see the mod_remoteip module).
        parsers.addAll(createFirstAndLastTokenParsers("%{c}a",
                "connection.client.peerip", "IP",
                STRING_ONLY, FORMAT_CLF_IP));

        // -------
        // %A Local IP-address
        parsers.addAll(createFirstAndLastTokenParsers("%A",
                "connection.server.ip", "IP",
                STRING_ONLY, FORMAT_CLF_IP));

        // -------
        // %B Size of response in bytes, excluding HTTP headers.
        parsers.addAll(createFirstAndLastTokenParsers("%B",
                "response.body.bytes", "BYTES",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %b Size of response in bytes, excluding HTTP headers. In CLF format,
        // i.e. a '-' rather than a 0 when no bytes are sent.
        parsers.addAll(createFirstAndLastTokenParsers("%b",
            "response.body.bytes", "BYTESCLF",
            STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // Additional support for the deprecated old output
        addExtraOutput(parsers, "%b",
            new TokenOutputField("BYTES", "response.body.bytesclf", STRING_OR_LONG)
            .deprecateFor("BYTESCLF:response.body.bytes"));

        // -------
        // %{Foobar}C The contents of cookie Foobar in the request sent to the server.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}C",
                "request.cookies.", "HTTP.COOKIE",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %{FOOBAR}e The contents of the environment variable FOOBAR
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}e", "server.environment.", "VARIABLE",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %f Filename
        parsers.addAll(createFirstAndLastTokenParsers("%f",
                "server.filename", "FILENAME",
                STRING_ONLY, FORMAT_STRING));
        // -------

        // %h Remote host
        parsers.addAll(createFirstAndLastTokenParsers("%h",
                "connection.client.host", "IP",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %H The request protocol
        parsers.addAll(createFirstAndLastTokenParsers("%H",
                "request.protocol", "PROTOCOL",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %{Foobar}i The contents of Foobar: header line(s) in the request sent
        // to the server. Changes made by other modules (e.g. mod_headers)
        // affect this.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}i",
                "request.header.", "HTTP.HEADER",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %{VARNAME}^ti The contents of VARNAME: trailer line(s) in the request sent to the server.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}\\^ti",
                "request.trailer.", "HTTP.TRAILER",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %k Number of keepalive requests handled on this connection.
        // Interesting if KeepAlive is being used, so that, for example,
        // a '1' means the first keepalive request after the initial one, '
        // 2' the second, etc...;
        // otherwise this is always 0 (indicating the initial request).
        // Available in versions 2.2.11 and later.
        parsers.addAll(createFirstAndLastTokenParsers("%k",
                "connection.keepalivecount", "NUMBER",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %l Remote logname (from identd, if supplied). This will return a dash
        // unless mod_ident is present and IdentityCheck is set On.
        parsers.addAll(createFirstAndLastTokenParsers("%l",
                "connection.client.logname", "NUMBER",
                STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // %L The request log ID from the error log (or '-' if nothing has been logged to the error log for this request).
        // Look for the matching error log line to see what request caused what error.
        parsers.addAll(createFirstAndLastTokenParsers("%L",
                "request.errorlogid", "STRING",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %m The request method
        parsers.addAll(createFirstAndLastTokenParsers("%m",
                "request.method", "HTTP.METHOD",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %{Foobar}n The contents of note Foobar from another module.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}n",
                "server.module_note.", "STRING",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %{Foobar}o The contents of Foobar: header line(s) in the response.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-]*)\\}o",
                "response.header.", "HTTP.HEADER",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %{VARNAME}^to The contents of VARNAME: trailer line(s) in the response sent from the server.
        parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}\\^to",
                "response.trailer.", "HTTP.TRAILER",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %p The canonical port of the server serving the request
        parsers.addAll(createFirstAndLastTokenParsers("%p",
                "request.server.port.canonical", "PORT",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %{format}p The canonical port of the server serving the request or
        // the server's actual port or the client's actual port. Valid formats
        // are canonical, local, or remote.
        parsers.addAll(createFirstAndLastTokenParsers("%{canonical}p",
                "connection.server.port.canonical", "PORT",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{local}p",
                "connection.server.port", "PORT",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{remote}p",
                "connection.client.port", "PORT",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %P The process ID of the child that serviced the request.
        parsers.addAll(createFirstAndLastTokenParsers("%P",
                "connection.server.child.processid", "NUMBER",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %{format}P The process ID or thread id of the child that serviced the
        // request. Valid formats are pid, tid, and hextid. hextid requires
        // APR 1.2.0 or higher.
        parsers.addAll(createFirstAndLastTokenParsers("%{pid}P",
                "connection.server.child.processid", "NUMBER",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{tid}P",
                "connection.server.child.threadid", "NUMBER",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{hextid}P",
                "connection.server.child.hexthreadid", "NUMBER",
                STRING_OR_LONG, FORMAT_CLF_HEXNUMBER));

        // -------
        // %q The query string (prepended with a ? if a query string exists,
        // otherwise an empty string)
        parsers.addAll(createFirstAndLastTokenParsers("%q",
                "request.querystring", "HTTP.QUERYSTRING",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %r First line of request
        parsers.addAll(createFirstAndLastTokenParsers("%r",
                "request.firstline", "HTTP.FIRSTLINE",
                STRING_ONLY, HttpFirstLineDissector.FIRSTLINE_REGEX));

        // -------
        // %R The handler generating the response (if any).
        parsers.addAll(createFirstAndLastTokenParsers("%R",
                "request.handler", "STRING",
                STRING_ONLY, FORMAT_STRING));

        // -------
        // %s Status. For requests that got internally redirected, this is the
        // status of the *original* request --- %>s for the last.
        parsers.addAll(createFirstAndLastTokenParsers("%s",
            "request.status", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING, 0));

        // -------
        // %t Time the request was received (standard english format)
        parsers.addAll(createFirstAndLastTokenParsers("%t",
                "request.receive.time", "TIME.STAMP",
                STRING_ONLY, FORMAT_STANDARD_TIME_US));

        // %{format}t The time, in the form given by format, which should be in
        // strftime(3) format. (potentially localized)
        parsers.add(new ParameterizedTokenParser("\\%\\{([^\\}]*%[^\\}]*)\\}t",
            "request.receive.time", "TIME.STRFTIME_",
            STRING_ONLY, FORMAT_STRING,
            -1, new StrfTimeStampDissector())
            .setWarningMessageWhenUsed("Only some parts of localized timestamps are supported")
        );

        // If the format starts with begin: (default) the time is taken at the beginning of the request processing.
        // If it starts with end: it is the time when the log entry gets written, close to the end of the request processing.

        parsers.add(new ParameterizedTokenParser("\\%\\{begin:([^\\}]*%[^\\}]*)\\}t",
            "request.receive.time.begin", "TIME.STRFTIME_",
            STRING_ONLY, FORMAT_STRING,
            0, new StrfTimeStampDissector())
            .setWarningMessageWhenUsed("Only some parts of localized timestamps are supported")
        );

        parsers.add(new ParameterizedTokenParser("\\%\\{end:([^\\}]*%[^\\}]*)\\}t",
            "request.receive.time.end", "TIME.STRFTIME_",
            STRING_ONLY, FORMAT_STRING,
            0, new StrfTimeStampDissector())
            .setWarningMessageWhenUsed("Only some parts of localized timestamps are supported")
        );

        // In addition to the formats supported by strftime(3), the following format tokens are supported:
        // sec number of seconds since the Epoch
        // msec number of milliseconds since the Epoch
        // usec number of microseconds since the Epoch
        // msec_frac millisecond fraction
        // usec_frac microsecond fraction
        // These tokens can not be combined with each other or strftime(3) formatting in the same format string.
        // You can use multiple %{format}t tokens instead.

        // sec
        parsers.addAll(createFirstAndLastTokenParsers("%{sec}t",
            "request.receive.time.sec", "TIME.SECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{begin:sec}t",
            "request.receive.time.begin.sec", "TIME.SECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{end:sec}t",
            "request.receive.time.end.sec", "TIME.SECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));

        // msec
        parsers.addAll(createFirstAndLastTokenParsers("%{msec}t",
                "request.receive.time.msec", "TIME.EPOCH",
                STRING_OR_LONG, FORMAT_NUMBER));

        // Additional support for the deprecated old output
        addExtraOutput(parsers, "%{msec}t",
            new TokenOutputField("TIME.EPOCH", "request.receive.time.begin.msec", STRING_OR_LONG)
                .deprecateFor("TIME.EPOCH:request.receive.time.msec"));

        parsers.addAll(createFirstAndLastTokenParsers("%{begin:msec}t",
                "request.receive.time.begin.msec", "TIME.EPOCH",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{end:msec}t",
                "request.receive.time.end.msec", "TIME.EPOCH",
                STRING_OR_LONG, FORMAT_NUMBER));

        // usec
        parsers.addAll(createFirstAndLastTokenParsers("%{usec}t",
                "request.receive.time.usec", "TIME.EPOCH.USEC",
                STRING_OR_LONG, FORMAT_NUMBER));

        // Additional support for the deprecated old output
        addExtraOutput(parsers, "%{usec}t",
            new TokenOutputField("TIME.EPOCH.USEC", "request.receive.time.begin.usec", STRING_OR_LONG)
                .deprecateFor("TIME.EPOCH.USEC:request.receive.time.usec"));

        parsers.addAll(createFirstAndLastTokenParsers("%{begin:usec}t",
                "request.receive.time.begin.usec", "TIME.EPOCH.USEC",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{end:usec}t",
                "request.receive.time.end.usec", "TIME.EPOCH.USEC",
                STRING_OR_LONG, FORMAT_NUMBER));

        // msec_frac
        parsers.addAll(createFirstAndLastTokenParsers("%{msec_frac}t",
                "request.receive.time.msec_frac", "TIME.EPOCH",
                STRING_OR_LONG, FORMAT_NUMBER));

        addExtraOutput(parsers, "%{msec_frac}t",
            new TokenOutputField("TIME.EPOCH", "request.receive.time.begin.msec_frac", STRING_OR_LONG)
                .deprecateFor("TIME.EPOCH:request.receive.time.msec_frac"));

        parsers.addAll(createFirstAndLastTokenParsers("%{begin:msec_frac}t",
                "request.receive.time.begin.msec_frac", "TIME.EPOCH",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{end:msec_frac}t",
                "request.receive.time.end.msec_frac", "TIME.EPOCH",
                STRING_OR_LONG, FORMAT_NUMBER));

        // usec_frac
        parsers.addAll(createFirstAndLastTokenParsers("%{usec_frac}t",
                "request.receive.time.usec_frac", "TIME.EPOCH.USEC_FRAC",
                STRING_OR_LONG, FORMAT_NUMBER));

        // Additional support for the deprecated old output
        addExtraOutput(parsers, "%{usec_frac}t",
            new TokenOutputField("TIME.EPOCH.USEC_FRAC", "request.receive.time.begin.usec_frac", STRING_OR_LONG)
                .deprecateFor("TIME.EPOCH.USEC_FRAC:request.receive.time.usec_frac"));

        parsers.addAll(createFirstAndLastTokenParsers("%{begin:usec_frac}t",
                "request.receive.time.begin.usec_frac", "TIME.EPOCH.USEC_FRAC",
                STRING_OR_LONG, FORMAT_NUMBER));

        parsers.addAll(createFirstAndLastTokenParsers("%{end:usec_frac}t",
                "request.receive.time.end.usec_frac", "TIME.EPOCH.USEC_FRAC",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %T The time taken to serve the request, in seconds.
        parsers.addAll(createFirstAndLastTokenParsers("%T",
                "response.server.processing.time", "SECONDS",
                STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %D The time taken to serve the request, in microseconds.
        parsers.addAll(createFirstAndLastTokenParsers("%D",
            "response.server.processing.time", "MICROSECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));

        // Additional support for the deprecated old output
        addExtraOutput(parsers, "%D",
            new TokenOutputField("MICROSECONDS", "server.process.time", STRING_OR_LONG)
            .deprecateFor("MICROSECONDS:response.server.processing.time"));

        // -------
        // %{UNIT}T The time taken to serve the request, in a time unit given by UNIT.
        // Valid units are ms for milliseconds, us for microseconds, and s for seconds.
        // Using s gives the same result as %T without any format;
        // Using us gives the same result as %D.
        parsers.addAll(createFirstAndLastTokenParsers("%{us}T",
            "response.server.processing.time", "MICROSECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));
        parsers.addAll(createFirstAndLastTokenParsers("%{ms}T",
            "response.server.processing.time", "MILLISECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));
        parsers.addAll(createFirstAndLastTokenParsers("%{s}T",
            "response.server.processing.time", "SECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // %u Remote user (from auth; may be bogus if return status (%s) is 401)
        parsers.addAll(createFirstAndLastTokenParsers("%u",
                "connection.client.user", "STRING",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %U The URL path requested, not including any query string.
        parsers.addAll(createFirstAndLastTokenParsers("%U",
                "request.urlpath", "URI",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %v The canonical ServerName of the server serving the request.
        parsers.addAll(createFirstAndLastTokenParsers("%v",
                "connection.server.name.canonical", "STRING",
                STRING_ONLY,  FORMAT_NO_SPACE_STRING));

        // -------
        // %V The server name according to the UseCanonicalName setting.
        parsers.addAll(createFirstAndLastTokenParsers("%V",
                "connection.server.name", "STRING",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %X Connection status when response is completed:
        // X = connection aborted before the response completed.
        // + = connection may be kept alive after the response is sent.
        // - = connection will be closed after the response is sent.
        // (This directive was %c in late versions of Apache 1.3, but this
        // conflicted with the historical ssl %{var}c syntax.)
        parsers.addAll(createFirstAndLastTokenParsers("%X",
                "response.connection.status", "HTTP.CONNECTSTATUS",
                STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // %I Bytes received, including request and headers, cannot be zero. You
        // need to enable mod_logio to use this.
        // NOTE: In reality this CAN ben 0 (in case of HTTP 408 error code)
        parsers.addAll(createFirstAndLastTokenParsers("%I",
                "request.bytes", "BYTES",
                STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // %O Bytes sent, including headers, cannot be zero. You need to enable
        // mod_logio to use this.
        // NOTE: In reality this CAN ben 0 (in case of HTTP 408 error code)
        parsers.addAll(createFirstAndLastTokenParsers("%O",
                "response.bytes", "BYTES",
                STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // %S Bytes transferred (received and sent), including request and headers, cannot be zero.
        // This is the combination of %I and %O. You need to enable mod_logio to use this.
        parsers.addAll(createFirstAndLastTokenParsers("%S",
                "total.bytes", "BYTES",
                STRING_OR_LONG, TokenParser.FORMAT_NON_ZERO_NUMBER));

        // Some explicit type overrides.
        // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
        parsers.addAll(createFirstAndLastTokenParsers("%{cookie}i",
                "request.cookies",    "HTTP.COOKIES",
                STRING_ONLY, FORMAT_STRING, 1));
        parsers.addAll(createFirstAndLastTokenParsers("%{set-cookie}o",
                "response.cookies",   "HTTP.SETCOOKIES",
                STRING_ONLY, FORMAT_STRING, 1));
        parsers.addAll(createFirstAndLastTokenParsers("%{user-agent}i",
                "request.user-agent", "HTTP.USERAGENT",
                STRING_ONLY, FORMAT_STRING, 1));
        parsers.addAll(createFirstAndLastTokenParsers("%{referer}i",
                "request.referer",    "HTTP.URI",
                STRING_ONLY, FORMAT_STRING, 1));

        return parsers;
    }

    private void addExtraOutput(List<TokenParser> parsers,
                                    final String nLogFormatToken,
                                     TokenOutputField tokenOutputField) {
        for (TokenParser tokenParser:parsers) {
            if (tokenParser.getLogFormatToken().equals(nLogFormatToken)){
                tokenParser.addOutputField(tokenOutputField);
                return;
            }
        }
    }

    private List<TokenParser> createFirstAndLastTokenParsers(
        final String nLogFormatToken,
        final String nValueName,
        final String nValueType,
        final EnumSet<Casts> nCasts,
        final String nRegex) {
        return createFirstAndLastTokenParsers(nLogFormatToken, nValueName, nValueType, nCasts, nRegex, 0);
    }

    private List<TokenParser> createFirstAndLastTokenParsers(
        final String nLogFormatToken,
        final String nValueName,
        final String nValueType,
        final EnumSet<Casts> nCasts,
        final String nRegex,
        final int nPrio) {
        List<TokenParser> parsers = new ArrayList<>(3);

        // Quote from http://httpd.apache.org/docs/current/mod/mod_log_config.html#modifiers
        //      The modifiers "<" and ">" can be used for requests that have been internally redirected to
        //      choose whether the original or final (respectively) request should be consulted.
        //      By default, the % directives %s, %U, %T, %D, and %r look at the original request
        //      while all others look at the final request.
        //      So for example, %>s can be used to record the final status of the request and %<u can be used
        //      to record the original authenticated user on a request that is internally redirected to an
        //      unauthenticated resource.

        switch(nLogFormatToken) {
            case "%s":
            case "%U":
            case "%T":
            case "%{us}T":
            case "%{ms}T":
            case "%{s}T":
            case "%D":
            case "%r":
                // Quote: By default, the % directives %s, %U, %T, %D, and %r look at the original request
                // So '%X' is the same value as '%<X' hence we output BOTH
                parsers.add(new TokenParser(nLogFormatToken, nRegex, nPrio)
                    .addOutputField(nValueType, nValueName,          nCasts)
                    .addOutputField(nValueType, nValueName + ".original", nCasts)
                );

                break;
            default:
                // Quote: By default, ... all others look at the final request.
                // So '%X' is the same value as '%>X' hence we output BOTH
                parsers.add(new TokenParser(nLogFormatToken, nRegex, nPrio)
                    .addOutputField(nValueType, nValueName,           nCasts)
                    .addOutputField(nValueType, nValueName + ".last", nCasts)
                );
                break;
        }

        parsers.add(new TokenParser(nLogFormatToken.replaceFirst("%", "%<"), nRegex, nPrio)
            .addOutputField(nValueType, nValueName + ".original", nCasts)
        );

        parsers.add(new TokenParser(nLogFormatToken.replaceFirst("%", "%>"), nRegex, nPrio)
            .addOutputField(nValueType, nValueName + ".last", nCasts)
        );

        return parsers;
    }


}
