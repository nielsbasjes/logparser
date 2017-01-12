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
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertMillisecondsIntoMicroseconds;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertSecondsWithMillisStringDissector;
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
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_HEXDIGIT;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_HEXNUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER_DOT_NUMBER;
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
        //                      '"$request" $status $body_bytes_sent '
        //                      '"$http_referer" "$http_user_agent"';
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

        // http://nginx.org/en/docs/http/ngx_http_log_module.html#log_format
        // FIXME: Cleanup this list. Seems to contain too much when looking at the documentation.
        //  Needed:
        //  $bytes_sent         the number of bytes sent to a client
        //  $connection        connection serial number
        //  $connection_requests        the current number of requests made through a connection (1.1.18)
        //  $msec        time in seconds with a milliseconds resolution at the time of the log write
        //  $pipe       “p” if request was pipelined, “.” otherwise
        //  $request_length        request length (including request line, header, and request body)
        //  $request_time        request processing time in seconds with a milliseconds resolution; time elapsed between the first
        //                      bytes were read from the client and the log write after the last bytes were sent to the client
        //  $status        response status
        //  $time_iso8601        local time in the ISO 8601 standard format
        //  $time_local        local time in the Common Log Format
        //  $http_*         request headers
        //  $send_http_*    response headers

        //  log_format combined '$remote_addr - $remote_user [$time_local] "$request" $status $body_bytes_sent "$http_referer" "$http_user_agent"';

//        http://nginx.org/en/docs/http/ngx_http_core_module.html#variables
//        $arg_name
//        argument name in the request line
//        $args
//        arguments in the request line
//            $binary_remote_addr
//        client address in a binary form, value’s length is always 4 bytes for IPv4 addresses or 16 bytes for IPv6 addresses
//        $body_bytes_sent
//        number of bytes sent to a client, not counting the response header; this variable is compatible with the “%B” parameter of the
// mod_log_config Apache module
//        $bytes_sent
//        number of bytes sent to a client (1.3.8, 1.2.5)
//        $connection
//        connection serial number (1.3.8, 1.2.5)
//        $connection_requests
//        current number of requests made through a connection (1.3.8, 1.2.5)
//        $content_length
//“Content-Length” request header field
//            $content_type
//“Content-Type” request header field
//            $cookie_name
//        the name cookie
//            $document_root
//        root or alias directive’s value for the current request
//            $document_uri
//        same as $uri
//            $host
//        in this order of precedence: host name from the request line, or host name from the “Host” request header field,
// or the server name matching a request
//        $hostname
//        host name
//        $http_name
//        arbitrary request header field; the last part of a variable name is the field name converted to lower case
// with dashes replaced by underscores
//        $https
//“on” if connection operates in SSL mode, or an empty string otherwise
//        $is_args
//“?” if a request line has arguments, or an empty string otherwise
//        $limit_rate
//        setting this variable enables response rate limiting; see limit_rate
//        $msec
//        current time in seconds with the milliseconds resolution (1.3.9, 1.2.6)
//        $nginx_version
//        nginx version
//        $pid
//        PID of the worker process
//            $pipe
//“p” if request was pipelined, “.” otherwise (1.3.12, 1.2.7)
//        $proxy_protocol_addr
//        client address from the PROXY protocol header, or an empty string otherwise (1.5.12)
//        The PROXY protocol must be previously enabled by setting the proxy_protocol parameter in the listen directive.
//
//        $proxy_protocol_port
//        client port from the PROXY protocol header, or an empty string otherwise (1.11.0)
//        The PROXY protocol must be previously enabled by setting the proxy_protocol parameter in the listen directive.
//
//        $query_string
//        same as $args
//            $realpath_root
//        an absolute pathname corresponding to the root or alias directive’s value for the current request,
// with all symbolic links resolved to real paths
//            $remote_addr
//        client address
//        $remote_port
//        client port
//        $remote_user
//        user name supplied with the Basic authentication
//            $request
//        full original request line
//        $request_body
//        request body
//        The variable’s value is made available in locations processed by the proxy_pass, fastcgi_pass, uwsgi_pass,
// and scgi_pass directives when the request body was read to a memory buffer.
//
//        $request_body_file
//        name of a temporary file with the request body
//        At the end of processing, the file needs to be removed. To always write the request body to a file,
// client_body_in_file_only needs to be enabled. When the name of a temporary file is passed in a proxied request
// or in a request to a FastCGI/uwsgi/SCGI server, passing the request body should be disabled by the proxy_pass_request_body off,
// fastcgi_pass_request_body off, uwsgi_pass_request_body off, or scgi_pass_request_body off directives, respectively.
//
//            $request_completion
//“OK” if a request has completed, or an empty string otherwise
//        $request_filename
//        file path for the current request, based on the root or alias directives, and the request URI
//            $request_id
//        unique request identifier generated from 16 random bytes, in hexadecimal (1.11.0)
//        $request_length
//        request length (including request line, header, and request body) (1.3.12, 1.2.7)
//        $request_method
//        request method, usually “GET” or “POST”
//        $request_time
//        request processing time in seconds with a milliseconds resolution (1.3.9, 1.2.6); time elapsed since
// the first bytes were read from the client
//            $request_uri
//        full original request URI (with arguments)
//        $scheme
//        request scheme, “http” or “https”
//        $sent_http_name
//        arbitrary response header field; the last part of a variable name is the field name converted to
// lower case with dashes replaced by underscores
//        $server_addr
//        an address of the server which accepted a request
//        Computing a value of this variable usually requires one system call. To avoid a system call, the listen
// directives must specify addresses and use the bind parameter.
//
//        $server_name
//        name of the server which accepted a request
//        $server_port
//        port of the server which accepted a request
//        $server_protocol
//        request protocol, usually “HTTP/1.0”, “HTTP/1.1”, or “HTTP/2.0”
//        $status
//        response status (1.3.2, 1.2.2)
//        $tcpinfo_rtt, $tcpinfo_rttvar, $tcpinfo_snd_cwnd, $tcpinfo_rcv_space
//        information about the client TCP connection; available on systems that support the TCP_INFO socket option
//            $time_iso8601
//        local time in the ISO 8601 standard format (1.3.12, 1.2.7)
//        $time_local
//        local time in the Common Log Format (1.3.12, 1.2.7)
//        $uri
//        current URI in request, normalized
//        The value of $uri may change during request processing, e.g. when doing internal redirects, or when using index files.

        // TODO: Check https://www.nginx.com/resources/admin-guide/logging-and-monitoring/
        // $upstream_connect_time – The time spent on establishing a connection with an upstream server
        // $upstream_header_time – The time between establishing a connection and receiving the first byte of the response header
        //                          from the upstream server
        // $upstream_response_time – The time between establishing a connection and receiving the last byte of the response body
        //                          from the upstream server
        // $request_time – The total time spent processing a request



        // -------
        // $bytes_sent
        // number of bytes sent to a client (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$bytes_sent",
            "response.bytes", "BYTES",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $connection
        // connection serial number (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$connection",
            "connection.serial_number", "NUMBER",
            Casts.STRING_OR_LONG, FORMAT_CLF_NUMBER, -1));

        // -------
        // $connection_requests
        // current number of requests made through a connection (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$connection_requests",
            "connection.requestnr", "NUMBER",
            Casts.STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // $msec
        // time in seconds with a milliseconds resolution at the time of the log write
        // Example value:  1483455396.639
        parsers.add(new TokenParser("$msec",
            "request.receive.time.epoch", "TIME.EPOCH_SECOND_MILLIS",
            Casts.STRING_ONLY, "[0-9]+\\.[0-9][0-9][0-9]"));

        // -------
        // $status
        // response status
        parsers.add(new TokenParser("$status",
            "request.status.last", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $time_iso8601
        // local time in the ISO 8601 standard format (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$time_iso8601",
            "request.receive.time", "TIME.ISO8601",
            Casts.STRING_ONLY, FORMAT_STANDARD_TIME_ISO8601));

        // -------
        // $time_local
        // local time in the Common Log Format (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$time_local",
            "request.receive.time", "TIME.STAMP",
            Casts.STRING_ONLY, FORMAT_STANDARD_TIME_US));

        // -------
        // Header lines sent to a client have the prefix “sent_http_”, for example, $sent_http_content_range.
        parsers.add(new NamedTokenParser("\\$sent_http_([a-z0-9\\-\\_]*)",
            "response.header.", "HTTP.HEADER",
            Casts.STRING_ONLY, FORMAT_STRING));

        // http://nginx.org/en/docs/http/ngx_http_core_module.html#var_bytes_sent
        // -------
        // $arg_name
        // argument name in the request line
        parsers.add(new NamedTokenParser("\\$arg_([a-z0-9\\-\\_]*)",
            "request.firstline.uri.query.", "STRING",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $is_args
        // “?” if a request line has arguments, or an empty string otherwise
        parsers.add(new TokenParser("$is_args",
            "request.firstline.uri.is_args", "STRING",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $args
        // arguments in the request line
        parsers.add(new TokenParser("$args",
            "request.firstline.uri.query", "HTTP.QUERYSTRING",
            Casts.STRING_ONLY, FORMAT_STRING));
        // -------
        // $query_string
        // same as $args
        parsers.add(new TokenParser("$query_string",
            "request.firstline.uri.query", "HTTP.QUERYSTRING",
            Casts.STRING_ONLY, FORMAT_STRING));


        // -------
        // $body_bytes_sent
        // number of bytes sent to a client, not counting the response header; this variable is compatible with
        // the “%B” parameter of the mod_log_config Apache module
        parsers.add(new TokenParser("$body_bytes_sent",
            "response.body.bytes", "BYTES",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

//        // the %b variant
//        parsers.add(new TokenParser("$body_bytes_sent",
//            "response.body.bytes", "BYTESCLF",
//            Casts.STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // $content_length
        // “Content-Length” request header field
        parsers.add(new TokenParser("$content_length",
                "request.header.content_length", "HTTP.HEADER",  // TODO: Use '-' or '_' ??
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $content_type
        // “Content-Type” request header field
        parsers.add(new TokenParser("$content_type",
                "request.header.content_type", "HTTP.HEADER", // TODO: Use '-' or '_' ??
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $cookie_name
        // the name cookie
        parsers.add(new NamedTokenParser("\\$cookie_([a-z0-9\\-_]*)",
                "request.cookies.", "HTTP.COOKIE",
                Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $document_root
        // root or alias directive’s value for the current request
        parsers.add(new TokenParser("$document_root",
            "request.firstline.document_root", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));
        // -------
        // $realpath_root
        // an absolute pathname corresponding to the root or alias directive’s value for the current request,
        // with all symbolic links resolved to real paths
        parsers.add(new TokenParser("$realpath_root",
            "request.firstline.realpath_root", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $host
        // in this order of precedence: host name from the request line, or host name from the “Host” request header field,
        // or the server name matching a request
        parsers.add(new TokenParser("$host",
            "connection.server.name", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING, -1));

        // -------
        // $hostname
        // host name
        parsers.add(new TokenParser("$hostname",
            "connection.client.host", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $http_<name>
        // arbitrary request header field; the last part of a variable name is the field name converted
        // to lower case with dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$http_([a-z0-9\\-_]*)",
                "request.header.", "HTTP.HEADER",
                Casts.STRING_ONLY, FORMAT_STRING));

        parsers.add(new TokenParser("$http_user_agent",
                "request.user-agent", "HTTP.USERAGENT",
                Casts.STRING_ONLY, FORMAT_STRING, 1));

        parsers.add(new TokenParser("$http_referer",
                "request.referer", "HTTP.URI",
                Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING, 1));

        // -------
        // $https
        // “on” if connection operates in SSL mode, or an empty string otherwise
        parsers.add(new TokenParser("$https",
            "connection.https", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $limit_rate
        // setting this variable enables response rate limiting; see limit_rate
        parsers.add(new NotImplementedTokenParser("$limit_rate", "nginx_parameter_not_intended_for_logging", FORMAT_NO_SPACE_STRING, 0));

        // -------
        // $nginx_version
        // nginx version
        parsers.add(new TokenParser("$nginx_version",
            "server.nginx.version", "STRING",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // $pid
        // PID of the worker process
        parsers.add(new TokenParser("$pid",
            "connection.server.child.processid", "NUMBER",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $pipe
        // “p” if request was pipelined, “.” otherwise (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$pipe",
            "connection.nginx.pipe", "STRING",
            Casts.STRING_ONLY, "\\.?"));

        // -------
        // $proxy_protocol_addr
        // client address from the PROXY protocol header, or an empty string otherwise (1.5.12)
        // The PROXY protocol must be previously enabled by setting the proxy_protocol parameter in the listen directive.
        // TODO: Check if this is correct. Need actually logged example for this.
        parsers.add(new TokenParser("$proxy_protocol_addr",
            "connection.client.proxy.host", "IP",
            Casts.STRING_OR_LONG, FORMAT_CLF_IP));

        // TODO: Consider renaming 'host' to 'ip'

        // -------
        // $remote_addr
        // client address
        parsers.add(new TokenParser("$remote_addr",
            "connection.client.host", "IP",
            Casts.STRING_OR_LONG, FORMAT_CLF_IP));

        // -------
        // $binary_remote_addr
        // client address in a binary form, value’s length is always 4 bytes
        String formatHexByte = "\\\\x" + FORMAT_HEXDIGIT + FORMAT_HEXDIGIT;
        parsers.add(new TokenParser("$binary_remote_addr",
            "connection.client.host", "IP_BINARY",
            Casts.STRING_OR_LONG, formatHexByte + formatHexByte + formatHexByte + formatHexByte));

        // -------
        // $remote_port
        // client port
        parsers.add(new TokenParser("$remote_port",
            "connection.client.port", "PORT",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $remote_user
        // user name supplied with the Basic authentication
        parsers.add(new TokenParser("$remote_user",
            "connection.client.user", "STRING",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $request
        // full original request line
        parsers.add(new TokenParser("$request",
            "request.firstline", "HTTP.FIRSTLINE",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING + " " +
            FORMAT_NO_SPACE_STRING + " " +
            FORMAT_NO_SPACE_STRING, -2));

        // -------
        // $request_body
        // request body
        // The variable’s value is made available in locations processed by the proxy_pass, fastcgi_pass, uwsgi_pass, and scgi_pass directives.
        parsers.add(new NotImplementedTokenParser("$request_body", "nginx_parameter_not_intended_for_logging", FORMAT_STRING, -1));

        // -------
        // $request_body_file
        // name of a temporary file with the request body
        // At the end of processing, the file needs to be removed. To always write the request body to a file,
        // client_body_in_file_only needs to be enabled. When the name of a temporary file is passed in a proxied request
        // or in a request to a FastCGI/uwsgi/SCGI server, passing the request body should be disabled by the
        // proxy_pass_request_body off, fastcgi_pass_request_body off, uwsgi_pass_request_body off, or
        // scgi_pass_request_body off directives, respectively.
        parsers.add(new NotImplementedTokenParser("$request_body_file", "nginx_parameter_not_intended_for_logging", FORMAT_STRING, -1));

        // -------
        // $request_completion
        // “OK” if a request has completed, or an empty string otherwise
        parsers.add(new TokenParser("$request_completion",
            "request.completion", "STRING",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request_filename
        // file path for the current request, based on the root or alias directives, and the request URI
        parsers.add(new TokenParser("$request_filename",
            "server.filename", "FILENAME",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // $request_length
        // request length (including request line, header, and request body) (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$request_length",
            "request.bytes", "BYTES",
            Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // $request_method
        // request method, usually “GET” or “POST”
        parsers.add(new TokenParser("$request_method",
            "request.firstline.method", "HTTP.METHOD",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request_time
        // request processing time in seconds with a milliseconds resolution (1.3.9, 1.2.6);
        // time elapsed since the first bytes were read from the client
        parsers.add(new TokenParser("$request_time",
            "response.server.processing.time", "SECOND_MILLIS",
            Casts.STRING_ONLY, FORMAT_NUMBER_DOT_NUMBER));

        // -------
        // $request_uri
        // full original request URI (with arguments)
        parsers.add(new TokenParser("$request_uri",
            "request.firstline.uri", "HTTP.URI",
            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request_id
        // unique request identifier generated from 16 random bytes, in hexadecimal (1.11.0)
        parsers.add(new NotYetImplemented("$request_id", FORMAT_HEXNUMBER));
//        parsers.add(new TokenParser("$request_id",
//            "request.firstline.uri", "HTTP.URI",
//            Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $uri
        // current URI in request, normalized
        // The value of $uri may change during request processing, e.g. when doing internal redirects, or when using index files.
        parsers.add(new TokenParser("$uri",
            "request.firstline.uri.normalized", "HTTP.URI",
            Casts.STRING_ONLY, FORMAT_STRING));
        // -------
        // $document_uri
        // same as $uri
        parsers.add(new TokenParser("$document_uri",
            "request.firstline.uri.normalized", "HTTP.URI",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $scheme
        // request scheme, “http” or “https”
        parsers.add(new TokenParser("$scheme",
                "request.firstline.uri.protocol", "HTTP.PROTOCOL",
                Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $sent_http_name
        // arbitrary response header field; the last part of a variable name is the field name converted to lower case with
        // dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$sent_http_([a-z0-9\\-_]*)",
            "response.header.", "HTTP.HEADER",
            Casts.STRING_ONLY, FORMAT_STRING));

        // -------
        // $server_addr
        // an address of the server which accepted a request
        // Computing a value of this variable usually requires one system call. To avoid a system call, the listen
        // directives must specify addresses and use the bind parameter.
        parsers.add(new TokenParser("$server_addr",
                "connection.server.ip", "IP",
                Casts.STRING_OR_LONG, FORMAT_CLF_IP));

        // -------
        // $server_name
        // name of the server which accepted a request
        parsers.add(new TokenParser("$server_name",
                "connection.server.name", "STRING",
                Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $server_port
        // port of the server which accepted a request
        parsers.add(new TokenParser("$server_port",
                "connection.server.port", "PORT",
                Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $server_protocol
        // request protocol, usually “HTTP/1.0” or “HTTP/1.1”
        parsers.add(new TokenParser("$server_protocol",
            "request.firstline.protocol", "HTTP.PROTOCOL_VERSION",
            Casts.STRING_OR_LONG, FORMAT_NO_SPACE_STRING));

        // -------
        // $tcpinfo_rtt, $tcpinfo_rttvar, $tcpinfo_snd_cwnd, $tcpinfo_rcv_space
        // information about the client TCP connection; available on systems that support the TCP_INFO socket option
        // See http://linuxgazette.net/136/pfeiffer.html
        //      tcpi_rtt and tcpi_rttvar are the Round Trip Time (RTT), and its smoothed mean deviation maximum measured in microseconds
        // $tcpinfo_rtt
        // $tcpinfo_rttvar
        parsers.add(new TokenParser("$tcpinfo_rtt",
            "connection.tcpinfo.rtt", "MICROSECONDS",
            Casts.STRING_OR_LONG, FORMAT_NUMBER, -1));
        parsers.add(new TokenParser("$tcpinfo_rttvar",
            "connection.tcpinfo.rttvar", "MICROSECONDS",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // $tcpinfo_snd_cwnd
        //      tcpi_snd_cwnd is the sending congestion window.
        parsers.add(new TokenParser("$tcpinfo_snd_cwnd",
            "connection.tcpinfo.send.cwnd", "BYTES",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));

        // $tcpinfo_rcv_space
        parsers.add(new TokenParser("$tcpinfo_rcv_space",
            "connection.tcpinfo.receive.space", "BYTES",
            Casts.STRING_OR_LONG, FORMAT_NUMBER));



        //   // %{c}a Underlying peer IP address of the connection (see the mod_remoteip module).
        //   parsers.add(new TokenParser("%{c}a",
        //   "connection.client.peerip", "IP",
        //   Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));


        //   // Some explicit type overrides.
        //   // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
        //   parsers.add(new TokenParser("%{cookie}i",
        //           "request.cookies", "HTTP.COOKIES",
        //           Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
        //   parsers.add(new TokenParser("%{set-cookie}o",
        //           "response.cookies", "HTTP.SETCOOKIES",
        //           Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));

        return parsers;
    }

    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        super.createAdditionalDissectors(parser);
        parser.addDissector(new BinaryIPDissector());
        parser.addDissector(new ConvertSecondsWithMillisStringDissector("SECOND_MILLIS",            "MILLISECONDS"));
        parser.addDissector(new ConvertSecondsWithMillisStringDissector("TIME.EPOCH_SECOND_MILLIS", "TIME.EPOCH"));
        parser.addDissector(new ConvertMillisecondsIntoMicroseconds("MILLISECONDS", "MICROSECONDS"));
    }

    public static class BinaryIPDissector extends SimpleDissector {

        private static HashMap<String, EnumSet<Casts>> epochMillisConfig = new HashMap<>();
        static {
            epochMillisConfig.put("IP:", Casts.STRING_OR_LONG);
        }
        public BinaryIPDissector() {
            super("IP_BINARY", epochMillisConfig);
        }

        private static final String CAPTURE_HEX_BYTE = "\\\\x([0-9a-fA-F][0-9a-fA-F])";
        Pattern binaryIPPattern = Pattern.compile(
            CAPTURE_HEX_BYTE+CAPTURE_HEX_BYTE+CAPTURE_HEX_BYTE+CAPTURE_HEX_BYTE
        );

        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            Matcher matcher = binaryIPPattern.matcher(value.getString());
            if (matcher.matches()) {
                String ip =
                    String.valueOf(Utils.hexCharsToByte(matcher.group(1))) + '.' +
                    String.valueOf(Utils.hexCharsToByte(matcher.group(2))) + '.' +
                    String.valueOf(Utils.hexCharsToByte(matcher.group(3))) + '.' +
                    String.valueOf(Utils.hexCharsToByte(matcher.group(4)));
                parsable.addDissection(inputname, "IP", "", ip);
            }
        }
    }

    // This is marked as deprecated because we want to mark all uses of this as "undesirable"
    @Deprecated
    public static class NotYetImplemented extends NotImplementedTokenParser {
        private static final String FIELD_PREFIX = "nginx_parameter";
        public NotYetImplemented(final String nLogFormatToken) {
            super(nLogFormatToken, FIELD_PREFIX, 0);
        }

        public NotYetImplemented(final String nLogFormatToken, final String regex) {
            super(nLogFormatToken, FIELD_PREFIX, regex, 0);
        }

        public NotYetImplemented(final String nLogFormatToken, final String regex, final int prio) {
            super(nLogFormatToken, FIELD_PREFIX, regex, prio);
        }

        public NotYetImplemented(final String nLogFormatToken, final int prio) {
            super(nLogFormatToken, FIELD_PREFIX, "[^\" ]*", prio);
        }
    }

}
