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
package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_IP;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STANDARD_TIME_ISO8601;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STANDARD_TIME_US;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

@SuppressWarnings({
        "PMD.LongVariable", // I like my variable names this way
        "PMD.CyclomaticComplexity", "PMD.OnlyOneReturn",
        "PMD.BeanMembersShouldSerialize", // No beans here
        "PMD.DataflowAnomalyAnalysis" // Results in a lot of mostly useless messages.
    })
public final class NginxHttpdLogFormatDissector extends TokenFormatDissector {

    private static final Logger LOG = LoggerFactory.getLogger(NginxHttpdLogFormatDissector.class);

    public NginxHttpdLogFormatDissector(final String logFormat) {
        super(logFormat);
        setInputType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    public NginxHttpdLogFormatDissector() {
        super();
        setInputType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    private void overrideLogFormat(String originalLogformat, String logformat) {
        LOG.debug("Specified logformat \"{}\" was mapped to {}", originalLogformat, logformat);
        super.setLogFormat(logformat);
    }

    @Override
    public void setLogFormat(final String logformat) {
        // http://nginx.org/en/docs/http/ngx_http_log_module.html#log_format
        // The configuration always includes the predefined “combined” format:

        //  log_format combined '$remote_addr - $remote_user [$time_local] '
        //              '"$request" $status $body_bytes_sent '
        //              '"$http_referer" "$http_user_agent"';
        switch (logformat.toLowerCase(Locale.getDefault())) {
            case "combined":
                overrideLogFormat(logformat,
                    "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"");
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
        Pattern p = Pattern.compile("\\{([^\\}]*)\\}");
        Matcher m = p.matcher(logformat);
        while (m.find()) {
            m.appendReplacement(sb, '{' + m.group(1).toLowerCase() + '}');
        }
        m.appendTail(sb);

        return sb.toString();
    }


    @Override
    protected String cleanupLogFormat(String tokenLogFormat) {
        return makeHeaderNamesLowercaseInLogFormat(
                tokenLogFormat
        );
    }


    @Override
    public String decodeExtractedValue(String tokenName, String value) {
        if (value == null || value.equals("")) {
            return value;
        }

        // In Apache logfiles a '-' means a 'not specified' / 'empty' value.
        if (value.equals("-")) {
            return null;
        }

        return value;
    }

    // --------------------------------------------
    @Override
    protected List<TokenParser> createAllTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

//      http://nginx.org/en/docs/http/ngx_http_log_module.html#log_format

        // FIXME: !!!!!!!!!!! CHECK FOR DUPLICATE RULES !!!!!!!!!!!!!

        // -------
//      $bytes_sent
//      number of bytes sent to a client (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$bytes_sent",
            "response.bytes", "BYTES",
            Casts.STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
//      $connection
//      connection serial number (1.3.8, 1.2.5)
        parsers.add(new IgnoreUnknownTokenParser("$connection", -1)); // TODO: Implement $connection token

        // -------
//      $connection_requests
//      current number of requests made through a connection (1.3.8, 1.2.5)
        parsers.add(new IgnoreUnknownTokenParser("$connection_requests")); // TODO: Implement $connection_requests token

        // -------
//      $msec
//      time in seconds with a milliseconds resolution at the time of the log write
        // Example value:  1483455396.639
        parsers.add(new TokenParser("$msec",
            "epoch", "TIME.EPOCH_SECOND_MILLIS",
            Casts.STRING_ONLY, "[0-9]+\\.[0-9][0-9][0-9]",
            0, new EpochSecondsWithMillisDissector()));

        // -------
//      $pipe
//      “p” if request was pipelined, “.” otherwise
        parsers.add(new IgnoreUnknownTokenParser("$pipe")); // TODO: Implement $pipe token

        // -------
//      $request_length
//      request length (including request line, header, and request body)
        parsers.add(new IgnoreUnknownTokenParser("$request_length")); // TODO: Implement $request_length token

        // -------
//      $request_time
//      request processing time in seconds with a milliseconds resolution; time elapsed between the first bytes were
//      read from the client and the log write after the last bytes were sent to the client
        parsers.add(new IgnoreUnknownTokenParser("$request_time")); // TODO: Implement $request_time token

        // -------
//      $status
//      response status
        parsers.add(new TokenParser("$status",
            "request.status.original", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
//      $time_iso8601
//      local time in the ISO 8601 standard format (1.3.12, 1.2.7)
        // FIXME: Test this fixed format !!
        parsers.add(new TokenParser("$time_iso8601",
            "request.receive.time", "TIME.ISO8601",
            Casts.STRING_ONLY, FORMAT_STANDARD_TIME_ISO8601));

        // -------
//      $time_local
//      local time in the Common Log Format (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$time_local",
            "request.receive.time", "TIME.STAMP",
            Casts.STRING_ONLY, FORMAT_STANDARD_TIME_US));

        // -------
//      Header lines sent to a client have the prefix “sent_http_”, for example, $sent_http_content_range.
        parsers.add(new NamedTokenParser("\\$sent_http_([a-z0-9\\-\\_]*)",
            "response.header.", "HTTP.HEADER",
            Casts.STRING_ONLY, FORMAT_STRING));

//      http://nginx.org/en/docs/http/ngx_http_core_module.html#var_bytes_sent
        // -------
//      $arg_name
//      argument name in the request line
        parsers.add(new NamedTokenParser("\\$arg_([a-z0-9\\-\\_]*)",
            "request.firstline.uri.query.", "STRING",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $args
//      arguments in the request line
        parsers.add(new TokenParser("$args",
            "request.firstline.uri.query", "HTTP.QUERYSTRING",
            Casts.STRING_ONLY, FORMAT_STRING));
        // -------
//      $query_string
//      same as $args
        parsers.add(new TokenParser("$query_string",
            "request.firstline.uri.query", "HTTP.QUERYSTRING",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $binary_remote_addr
//      client address in a binary form, value’s length is always 4 bytes
        parsers.add(new IgnoreUnknownTokenParser("$binary_remote_addr")); // TODO: Implement $binary_remote_addr token

        // -------
//      $body_bytes_sent
//      number of bytes sent to a client, not counting the response header; this variable is compatible with
//      the “%B” parameter of the mod_log_config Apache module
        parsers.add(new TokenParser("$body_bytes_sent",
            "response.body.bytes", "BYTES",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // -------
//      $content_length
//      “Content-Length” request header field
        parsers.add(new TokenParser("$content_length",
                "request.header.content_length", "HTTP.HEADER",  // TODO: Use '-' or '_' ??
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $content_type
//      “Content-Type” request header field
        parsers.add(new TokenParser("$content_type",
                "request.header.content_type", "HTTP.HEADER", // TODO: Use '-' or '_' ??
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $cookie_name
//      the name cookie
        parsers.add(new NamedTokenParser("\\$cookie_([a-z0-9\\-_]*)",
                "request.cookies.", "HTTP.COOKIE",
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $document_root
        parsers.add(new IgnoreUnknownTokenParser("$document_root")); // TODO: Implement $document_root token
//      root or alias directive’s value for the current request

        // -------
//      $host
//      in this order of precedence: host name from the request line, or host name from the “Host” request header field,
//      or the server name matching a request
        parsers.add(new IgnoreUnknownTokenParser("$host", -1)); // TODO: Implement $host token

        // -------
//      $hostname
//      host name
        parsers.add(new IgnoreUnknownTokenParser("$hostname")); // TODO: Implement $hostname token

        // -------
//      $http_<name>
//      arbitrary request header field; the last part of a variable name is the field name converted to lower case with dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$http_([a-z0-9\\-_]*)",
                "request.header.", "HTTP.HEADER",
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $https
//      “on” if connection operates in SSL mode, or an empty string otherwise
        parsers.add(new IgnoreUnknownTokenParser("$https")); // TODO: Implement $https token

        // -------
//      $is_args
//      “?” if a request line has arguments, or an empty string otherwise
        parsers.add(new IgnoreUnknownTokenParser("$is_args")); // TODO: Implement $is_args token

        // -------
//      $limit_rate
//      setting this variable enables response rate limiting; see limit_rate
        parsers.add(new IgnoreUnknownTokenParser("$limit_rate")); // TODO: Implement $limit_rate token

        // -------
//      $msec
//      current time in seconds with the milliseconds resolution (1.3.9, 1.2.6)
        parsers.add(new IgnoreUnknownTokenParser("$msec")); // TODO: Implement $msec token

        // -------
//      $nginx_version
//      nginx version
        parsers.add(new IgnoreUnknownTokenParser("$nginx_version")); // TODO: Implement $nginx_version token

        // -------
//      $pid
//      PID of the worker process
        parsers.add(new IgnoreUnknownTokenParser("$pid")); // TODO: Implement $pid token

        // -------
//      $pipe
//      “p” if request was pipelined, “.” otherwise (1.3.12, 1.2.7)
        parsers.add(new IgnoreUnknownTokenParser("$pipe")); // TODO: Implement $pipe token

        // -------
//      $proxy_protocol_addr
//      client address from the PROXY protocol header, or an empty string otherwise (1.5.12)
//      The PROXY protocol must be previously enabled by setting the proxy_protocol parameter in the listen directive.
        parsers.add(new IgnoreUnknownTokenParser("$proxy_protocol_addr")); // TODO: Implement $proxy_protocol_addr token


        // -------
//      $realpath_root
//      an absolute pathname corresponding to the root or alias directive’s value for the current request,
//      with all symbolic links resolved to real paths
        parsers.add(new IgnoreUnknownTokenParser("$realpath_root")); // TODO: Implement $realpath_root token

        // -------
//      $remote_addr
//      client address
        parsers.add(new TokenParser("$remote_addr",
            "connection.client.ip", "IP",
            Casts.STRING_OR_LONG, FORMAT_CLF_IP));

        // -------
//      $remote_port
//      client port
        parsers.add(new IgnoreUnknownTokenParser("$remote_port")); // TODO: Implement $remote_port token

        // -------
//      $remote_user
//      user name supplied with the Basic authentication
        parsers.add(new TokenParser("$remote_user",
            "connection.client.user", "STRING",
            Casts.STRING_ONLY, FORMAT_STRING));

        //TODO: Add basic authentication parsing to Apache too!!

        // -------
//      $request
//      full original request line
        parsers.add(new TokenParser("$request",
            "request.firstline", "HTTP.FIRSTLINE",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING + " " +
            FORMAT_NO_SPACE_STRING + " " +
            FORMAT_NO_SPACE_STRING, -2));

        // -------
//      $request_body
//      request body
//      The variable’s value is made available in locations processed by the proxy_pass, fastcgi_pass, uwsgi_pass, and scgi_pass directives.
        parsers.add(new IgnoreUnknownTokenParser("$request_body", -1)); // TODO: Implement $request_body token

        // -------
//      $request_body_file
//      name of a temporary file with the request body
//      At the end of processing, the file needs to be removed. To always write the request body to a file,
//      client_body_in_file_only needs to be enabled. When the name of a temporary file is passed in a proxied request
//      or in a request to a FastCGI/uwsgi/SCGI server, passing the request body should be disabled by the
//      proxy_pass_request_body off, fastcgi_pass_request_body off, uwsgi_pass_request_body off, or
//      scgi_pass_request_body off directives, respectively.
        parsers.add(new IgnoreUnknownTokenParser("$request_body_file")); // TODO: Implement $request_body_file token

        // -------
//      $request_completion
//      “OK” if a request has completed, or an empty string otherwise
        parsers.add(new IgnoreUnknownTokenParser("$request_completion")); // TODO: Implement $request_completion token

        // -------
//      $request_filename
//      file path for the current request, based on the root or alias directives, and the request URI
        parsers.add(new IgnoreUnknownTokenParser("$request_filename")); // TODO: Implement $request_filename token

        // -------
//      $request_length
//      request length (including request line, header, and request body) (1.3.12, 1.2.7)
        parsers.add(new IgnoreUnknownTokenParser("$request_length")); // TODO: Implement $request_length token

        // -------
//      $request_method
//      request method, usually “GET” or “POST”
//    parsers.add(new TokenParser("%r",
//            "request.firstline", "HTTP.FIRSTLINE",
//            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING));
//    result.add("HTTP.METHOD:method");
        parsers.add(new IgnoreUnknownTokenParser("$request_method")); // TODO: Implement $request_method token

        // -------
//      $request_time
//      request processing time in seconds with a milliseconds resolution (1.3.9, 1.2.6); time elapsed since the first bytes were read from the client
        parsers.add(new IgnoreUnknownTokenParser("$request_time")); // TODO: Implement $request_time token

        // -------
//      $request_uri
//      full original request URI (with arguments)
//    parsers.add(new TokenParser("%r",
//            "request.firstline", "HTTP.FIRSTLINE",
//            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING));
//    result.add("HTTP.URI:uri");
        parsers.add(new IgnoreUnknownTokenParser("$request_uri")); // TODO: Implement $request_uri token


        // -------
//      $scheme
//      request scheme, “http” or “https”
        parsers.add(new TokenParser("$scheme",
                "request.firstline.uri.protocol", "HTTP.PROTOCOL",
                Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
//      $sent_http_name
//      arbitrary response header field; the last part of a variable name is the field name converted to lower case with
//      dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$sent_http_([a-z0-9\\-_]*)",
            "response.header.", "HTTP.HEADER",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
//      $server_addr
//      an address of the server which accepted a request
//      Computing a value of this variable usually requires one system call. To avoid a system call, the listen
//      directives must specify addresses and use the bind parameter.
        parsers.add(new TokenParser("$server_addr",
                "connection.server.ip", "IP",
                Casts.STRING_OR_LONG, FORMAT_CLF_IP));

        // -------
//      $server_name
//      name of the server which accepted a request
        parsers.add(new TokenParser("$server_name",
                "connection.server.name", "STRING",
                Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
//      $server_port
//      port of the server which accepted a request
        parsers.add(new TokenParser("$server_port",
                "connection.server.port", "PORT",
                Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // -------
//      $server_protocol
//      request protocol, usually “HTTP/1.0” or “HTTP/1.1”
        parsers.add(new TokenParser("$server_protocol",
            "protocol", "HTTP.PROTOCOL_VERSION",
            Casts.STRING_OR_LONG, FORMAT_NO_SPACE_STRING));

        // -------
//      $tcpinfo_rtt, $tcpinfo_rttvar, $tcpinfo_snd_cwnd, $tcpinfo_rcv_space
//      information about the client TCP connection; available on systems that support the TCP_INFO socket option
//      $tcpinfo_rtt
        parsers.add(new IgnoreUnknownTokenParser("$tcpinfo_rtt", -1)); // TODO: Implement $tcpinfo_rtt token
//      $tcpinfo_rttvar
        parsers.add(new IgnoreUnknownTokenParser("$tcpinfo_rttvar")); // TODO: Implement $tcpinfo_rttvar token
//      $tcpinfo_snd_cwnd
        parsers.add(new IgnoreUnknownTokenParser("$tcpinfo_snd_cwnd")); // TODO: Implement $tcpinfo_snd_cwnd token
//      $tcpinfo_rcv_space
        parsers.add(new IgnoreUnknownTokenParser("$tcpinfo_rcv_space")); // TODO: Implement $tcpinfo_rcv_space token


        // -------
//      $uri
        parsers.add(new IgnoreUnknownTokenParser("$uri")); // TODO: Implement $uri token
//      current URI in request, normalized
//      The value of $uri may change during request processing, e.g. when doing internal redirects, or when using index files.
        // -------
//      $document_uri
        parsers.add(new IgnoreUnknownTokenParser("$document_uri")); // TODO: Implement $document_uri token
//      same as $uri

//    parsers.add(new TokenParser("%r",
//            "request.firstline", "HTTP.FIRSTLINE",
//            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING));
//    result.add("HTTP.URI:uri");


//        // %{c}a Underlying peer IP address of the connection (see the mod_remoteip module).
//        parsers.add(new TokenParser("%{c}a",
//        "connection.client.peerip", "IP",
//        Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));


//        // Some explicit type overrides.
//        // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
//        parsers.add(new TokenParser("%{cookie}i",
//                "request.cookies", "HTTP.COOKIES",
//                Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
//        parsers.add(new TokenParser("%{set-cookie}o",
//                "response.cookies", "HTTP.SETCOOKIES",
//                Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
        parsers.add(new TokenParser("$http_user_agent",
                "request.user-agent", "HTTP.USERAGENT",
                Casts.STRING_ONLY, FORMAT_STRING, 1));
        parsers.add(new TokenParser("$http_referer",
                "request.referer", "HTTP.URI",
                Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING, 1));

        return parsers;
    }

    public static class EpochSecondsWithMillisDissector extends SimpleDissector {

        private static HashMap<String, EnumSet<Casts>> epochMillisConfig = new HashMap<>();
        static {
            epochMillisConfig.put("TIME.EPOCH:", Casts.STRING_OR_LONG);
        }
        public EpochSecondsWithMillisDissector() {
            super("TIME.EPOCH_SECOND_MILLIS", epochMillisConfig);
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            final ParsedField field = parsable.getParsableField(getInputType(), inputname);
            String fieldValue = field.getValue().getString();
            if (fieldValue == null || fieldValue.isEmpty()) {
                return; // Nothing to do here
            }
            String epochStrings[] = fieldValue.split("\\.",2);
            Long seconds =  Long.parseLong(epochStrings[0]);
            Long milliseconds =  Long.parseLong(epochStrings[1]);
            Long epoch = seconds * 1000 + milliseconds;

            parsable.addDissection(inputname, "TIME.EPOCH", "", epoch);
        }
    }

}
