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

package nl.basjes.parse.httpdlog.dissectors.nginxmodules;

import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_IP;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_CLF_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_HEXDIGIT;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_HEXNUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER_DECIMAL;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STANDARD_TIME_ISO8601;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STANDARD_TIME_US;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

// Implement the variables described here:
// https://nginx.org/en/docs/http/ngx_http_log_module.html#log_format
// https://nginx.org/en/docs/http/ngx_http_core_module.html#variables
public class CoreLogModule implements NginxModule {
    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // -------
        // $bytes_sent
        // number of bytes sent to a client (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$bytes_sent",
            "response.bytes", "BYTES",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $bytes_received
        // number of bytes received from a client (1.11.4)
        parsers.add(new TokenParser("$bytes_received",
            "request.bytes", "BYTES",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $connection
        // connection serial number (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$connection",
            "connection.serial_number", "NUMBER",
            STRING_OR_LONG, FORMAT_CLF_NUMBER, -1));

        // -------
        // $connection_requests
        // current number of requests made through a connection (1.3.8, 1.2.5)
        parsers.add(new TokenParser("$connection_requests",
            "connection.requestnr", "NUMBER",
            STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // $msec
        // time in seconds with a milliseconds resolution at the time of the log write
        // Example value:  1483455396.639
        parsers.add(new TokenParser("$msec",
            "request.receive.time.epoch", "TIME.EPOCH_SECOND_MILLIS",
            STRING_ONLY, "[0-9]+\\.[0-9][0-9][0-9]"));

        // -------
        // $status
        // response status
        parsers.add(new TokenParser("$status",
            "request.status.last", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $time_iso8601
        // local time in the ISO 8601 standard format (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$time_iso8601",
            "request.receive.time", "TIME.ISO8601",
            STRING_ONLY, FORMAT_STANDARD_TIME_ISO8601));

        // -------
        // $time_local
        // local time in the Common Log Format (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$time_local",
            "request.receive.time", "TIME.STAMP",
            STRING_ONLY, FORMAT_STANDARD_TIME_US));

        // https://nginx.org/en/docs/http/ngx_http_core_module.html#var_bytes_sent
        // -------
        // $arg_name
        // argument name in the request line
        parsers.add(new NamedTokenParser("\\$arg_([a-z0-9\\-\\_]*)",
            "request.firstline.uri.query.", "STRING",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $is_args
        // “?” if a request line has arguments, or an empty string otherwise
        parsers.add(new TokenParser("$is_args",
            "request.firstline.uri.is_args", "STRING",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $args
        // arguments in the request line
        parsers.add(new TokenParser("$args",
            "request.firstline.uri.query", "HTTP.QUERYSTRING",
            STRING_ONLY, FORMAT_STRING));
        // -------
        // $query_string
        // same as $args
        parsers.add(new TokenParser("$query_string",
            "request.firstline.uri.query", "HTTP.QUERYSTRING",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $body_bytes_sent
        // number of bytes sent to a client, not counting the response header; this variable is compatible with
        // the “%B” parameter of the mod_log_config Apache module
        parsers.add(new TokenParser("$body_bytes_sent",
            "response.body.bytes", "BYTES",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $content_length
        // “Content-Length” request header field
        parsers.add(new TokenParser("$content_length",
            "request.header.content_length", "HTTP.HEADER",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $content_type
        // “Content-Type” request header field
        parsers.add(new TokenParser("$content_type",
            "request.header.content_type", "HTTP.HEADER",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $cookie_name
        // the name cookie
        parsers.add(new NamedTokenParser("\\$cookie_([a-z0-9\\-_]*)",
            "request.cookies.", "HTTP.COOKIE",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $document_root
        // root or alias directive’s value for the current request
        parsers.add(new TokenParser("$document_root",
            "request.firstline.document_root", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $realpath_root
        // an absolute pathname corresponding to the root or alias directive’s value for the current request,
        // with all symbolic links resolved to real paths
        parsers.add(new TokenParser("$realpath_root",
            "request.firstline.realpath_root", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $host
        // in this order of precedence: host name from the request line, or host name from the “Host” request header field,
        // or the server name matching a request
        parsers.add(new TokenParser("$host",
            "connection.server.name", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING, -1));

        // -------
        // $hostname
        // host name
        parsers.add(new TokenParser("$hostname",
            "connection.client.host", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $http_<name>
        // arbitrary request header field; the last part of a variable name is the field name converted
        // to lower case with dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$http_([a-z0-9\\-_]*)",
            "request.header.", "HTTP.HEADER",
            STRING_ONLY, FORMAT_STRING));

        parsers.add(new TokenParser("$http_user_agent",
            "request.user-agent", "HTTP.USERAGENT",
            STRING_ONLY, FORMAT_STRING, 1));

        parsers.add(new TokenParser("$http_referer",
            "request.referer", "HTTP.URI",
            STRING_ONLY, FORMAT_NO_SPACE_STRING, 1));

        // -------
        // $https
        // “on” if connection operates in SSL mode, or an empty string otherwise
        parsers.add(new TokenParser("$https",
            "connection.https", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $limit_rate
        // setting this variable enables response rate limiting; see limit_rate
        parsers.add(new TokenFormatDissector.NotImplementedTokenParser("$limit_rate",
            "nginx_parameter_not_intended_for_logging", FORMAT_NO_SPACE_STRING, 0));

        // -------
        // $nginx_version
        // nginx version
        parsers.add(new TokenParser("$nginx_version",
            "server.nginx.version", "STRING",
            STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // $pid
        // PID of the worker process
        parsers.add(new TokenParser("$pid",
            "connection.server.child.processid", "NUMBER",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $protocol
        // protocol used to communicate with the client: TCP or UDP (1.11.4)
        parsers.add(new TokenParser("$protocol",
            "connection.protocol", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $pipe
        // “p” if request was pipelined, “.” otherwise (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$pipe",
            "connection.nginx.pipe", "STRING",
            STRING_ONLY, "."));

        // -------
        // $proxy_protocol_addr
        // client address from the PROXY protocol header, or an empty string otherwise (1.5.12)
        // The PROXY protocol must be previously enabled by setting the proxy_protocol parameter
        // in the listen directive.
        parsers.add(new TokenParser("$proxy_protocol_addr",
            "connection.client.proxy.host", "IP",
            STRING_OR_LONG, FORMAT_CLF_IP));

        // $proxy_protocol_port
        // client port from the PROXY protocol header, or an empty string otherwise (1.11.4)
        parsers.add(new TokenParser("$proxy_protocol_port",
            "connection.client.proxy.port", "PORT",
            STRING_OR_LONG, FORMAT_CLF_NUMBER));

        // -------
        // $remote_addr
        // client address
        parsers.add(new TokenParser("$remote_addr",
            "connection.client.host", "IP",
            STRING_OR_LONG, FORMAT_CLF_IP));

        // -------
        // $binary_remote_addr
        // client address in a binary form, value’s length is always 4 bytes
        String formatHexByte = "\\\\x" + FORMAT_HEXDIGIT + FORMAT_HEXDIGIT;
        parsers.add(new TokenParser("$binary_remote_addr",
            "connection.client.host", "IP_BINARY",
            STRING_OR_LONG, formatHexByte + formatHexByte + formatHexByte + formatHexByte));

        // -------
        // $remote_port
        // client port
        parsers.add(new TokenParser("$remote_port",
            "connection.client.port", "PORT",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $remote_user
        // user name supplied with the Basic authentication
        parsers.add(new TokenParser("$remote_user",
            "connection.client.user", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request
        // full original request line
        parsers.add(new TokenParser("$request",
            "request.firstline", "HTTP.FIRSTLINE",
            STRING_ONLY, FORMAT_STRING, -2));

        // -------
        // $request_body
        // request body
        // The variable’s value is made available in locations processed by the proxy_pass,
        // fastcgi_pass, uwsgi_pass, and scgi_pass directives.
        parsers.add(new TokenFormatDissector.NotImplementedTokenParser("$request_body",
            "nginx_parameter_not_intended_for_logging",
            FORMAT_STRING, -1));

        // -------
        // $request_body_file
        // name of a temporary file with the request body
        // At the end of processing, the file needs to be removed. To always write the request body to a file,
        // client_body_in_file_only needs to be enabled. When the name of a temporary file is passed in a proxied request
        // or in a request to a FastCGI/uwsgi/SCGI server, passing the request body should be disabled by the
        // proxy_pass_request_body off, fastcgi_pass_request_body off, uwsgi_pass_request_body off, or
        // scgi_pass_request_body off directives, respectively.
        parsers.add(new TokenFormatDissector.NotImplementedTokenParser("$request_body_file",
            "nginx_parameter_not_intended_for_logging",
            FORMAT_STRING, -1));

        // -------
        // $request_completion
        // “OK” if a request has completed, or an empty string otherwise
        parsers.add(new TokenParser("$request_completion",
            "request.completion", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request_filename
        // file path for the current request, based on the root or alias directives, and the request URI
        parsers.add(new TokenParser("$request_filename",
            "server.filename", "FILENAME",
            STRING_ONLY, TokenParser.FORMAT_STRING));

        // -------
        // $request_length
        // request length (including request line, header, and request body) (1.3.12, 1.2.7)
        parsers.add(new TokenParser("$request_length",
            "request.bytes", "BYTES",
            STRING_OR_LONG, TokenParser.FORMAT_CLF_NUMBER));

        // -------
        // $request_method
        // request method, usually “GET” or “POST”
        parsers.add(new TokenParser("$request_method",
            "request.firstline.method", "HTTP.METHOD",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request_time
        // request processing time in seconds with a milliseconds resolution (1.3.9, 1.2.6);
        // time elapsed since the first bytes were read from the client
        parsers.add(new TokenParser("$request_time",
            "response.server.processing.time", "SECOND_MILLIS",
            STRING_ONLY, FORMAT_NUMBER_DECIMAL));

        // -------
        // $request_uri
        // full original request URI (with arguments)
        parsers.add(new TokenParser("$request_uri",
            "request.firstline.uri", "HTTP.URI",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $request_id
        // unique request identifier generated from 16 random bytes, in hexadecimal (1.11.0)
        parsers.add(new TokenParser("$request_id",
            "request.id", "STRING",
            STRING_ONLY, FORMAT_HEXNUMBER));

        // -------
        // $uri
        // current URI in request, normalized
        // The value of $uri may change during request processing, e.g. when doing internal redirects, or when using index files.
        parsers.add(new TokenParser("$uri",
            "request.firstline.uri.normalized", "HTTP.URI",
            STRING_ONLY, FORMAT_STRING));
        // -------
        // $document_uri
        // same as $uri
        parsers.add(new TokenParser("$document_uri",
            "request.firstline.uri.normalized", "HTTP.URI",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $scheme
        // request scheme, “http” or “https”
        parsers.add(new TokenParser("$scheme",
            "request.firstline.uri.protocol", "HTTP.PROTOCOL",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $sent_http_name
        // arbitrary response header field; the last part of a variable name is the field name converted to lower case with
        // dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$sent_http_([a-z0-9\\-_]*)",
            "response.header.", "HTTP.HEADER",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $sent_trailer_name
        // arbitrary field sent at the end of the response (1.13.2); the last part of a variable name is the field name
        // converted to lower case with dashes replaced by underscores
        parsers.add(new NamedTokenParser("\\$sent_trailer_([a-z0-9\\-_]*)",
            "response.trailer.", "HTTP.TRAILER",
            STRING_ONLY, FORMAT_STRING));

        // -------
        // $server_addr
        // an address of the server which accepted a request
        // Computing a value of this variable usually requires one system call. To avoid a system call, the listen
        // directives must specify addresses and use the bind parameter.
        parsers.add(new TokenParser("$server_addr",
            "connection.server.ip", "IP",
            STRING_OR_LONG, FORMAT_CLF_IP));

        // -------
        // $server_name
        // name of the server which accepted a request
        parsers.add(new TokenParser("$server_name",
            "connection.server.name", "STRING",
            STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -------
        // $server_port
        // port of the server which accepted a request
        parsers.add(new TokenParser("$server_port",
            "connection.server.port", "PORT",
            STRING_OR_LONG, FORMAT_NUMBER));

        // -------
        // $server_protocol
        // request protocol, usually “HTTP/1.0” or “HTTP/1.1”
        parsers.add(new TokenParser("$server_protocol",
            "request.firstline.protocol", "HTTP.PROTOCOL_VERSION",
            STRING_OR_LONG, FORMAT_NO_SPACE_STRING));

        // $session_time
        // session duration in seconds with a milliseconds resolution (1.11.4);
        parsers.add(new TokenParser("$session_time",
            "connection.session.time", "SECOND_MILLIS",
            STRING_ONLY, FORMAT_NUMBER_DECIMAL));

        // -------
        // $tcpinfo_rtt, $tcpinfo_rttvar, $tcpinfo_snd_cwnd, $tcpinfo_rcv_space
        // information about the client TCP connection; available on systems that support the TCP_INFO socket option
        // See http://linuxgazette.net/136/pfeiffer.html
        //      tcpi_rtt and tcpi_rttvar are the Round Trip Time (RTT), and its smoothed mean deviation maximum measured in microseconds
        // $tcpinfo_rtt
        // $tcpinfo_rttvar
        parsers.add(new TokenParser("$tcpinfo_rtt",
            "connection.tcpinfo.rtt", "MICROSECONDS",
            STRING_OR_LONG, FORMAT_NUMBER, -1));
        parsers.add(new TokenParser("$tcpinfo_rttvar",
            "connection.tcpinfo.rttvar", "MICROSECONDS",
            STRING_OR_LONG, FORMAT_NUMBER));

        // $tcpinfo_snd_cwnd
        //      tcpi_snd_cwnd is the sending congestion window.
        parsers.add(new TokenParser("$tcpinfo_snd_cwnd",
            "connection.tcpinfo.send.cwnd", "BYTES",
            STRING_OR_LONG, FORMAT_NUMBER));

        // $tcpinfo_rcv_space
        parsers.add(new TokenParser("$tcpinfo_rcv_space",
            "connection.tcpinfo.receive.space", "BYTES",
            STRING_OR_LONG, FORMAT_NUMBER));

        //   // Some explicit type overrides.
        //   // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
        //   parsers.add(new TokenParser("%{cookie}i",
        //           "request.cookies", "HTTP.COOKIES",
        //           STRING_ONLY, FORMAT_STRING, 1));
        //   parsers.add(new TokenParser("%{set-cookie}o",
        //           "response.cookies", "HTTP.SETCOOKIES",
        //           STRING_ONLY, FORMAT_STRING, 1));

        // -------
        // Fallback for all unknown variables that might appear
        parsers.add(new NamedTokenParser("\\$([a-z0-9\\-\\_]*)",
            "nginx.unknown.", "UNKNOWN_NGINX_VARIABLE",
            STRING_ONLY, FORMAT_NO_SPACE_STRING, -10)
            .setWarningMessageWhenUsed("Found unknown variable \"${}\" that was mapped to \"{}\". " +
                "It is assumed the values are text that cannot contain a whitespace."));

        return parsers;
    }
}
