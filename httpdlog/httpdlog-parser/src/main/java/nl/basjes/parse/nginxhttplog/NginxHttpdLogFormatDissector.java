/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

package nl.basjes.parse.nginxhttplog;

import nl.basjes.parse.Utils;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.dissectors.tokenformat.TokenFormatDissector;
import nl.basjes.parse.dissectors.tokenformat.TokenParser;
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
public final class NginxHttpdLogFormatDissector extends TokenFormatDissector {

  private static final Logger LOG = LoggerFactory.getLogger(NginxHttpdLogFormatDissector.class);

  public static final String INPUT_TYPE = "NGINXLOGLINE";

  public NginxHttpdLogFormatDissector(final String logFormat) {
    super(logFormat);
    setInputType(INPUT_TYPE);
  }

  public NginxHttpdLogFormatDissector() {
    super();
    setInputType(INPUT_TYPE);
  }

  private void overrideLogFormat(String originalLogformat, String logformat) {
    LOG.debug("Specified logformat \"" + originalLogformat + "\" was mapped to " + logformat);
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
        overrideLogFormat(logformat, "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"");
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

//        // http://httpd.apache.org/docs/current/mod/mod_log_config.html#formats
//        // Format Notes
//        // For security reasons, starting with version 2.0.46, non-printable and other special characters
//        // in %r, %i and %o are escaped using \xhh sequences, where hh stands for the hexadecimal representation of
//        // the raw byte. Exceptions from this rule are " and \, which are escaped by prepending a backslash, and
//        // all whitespace characters, which are written in their C-style notation (\n, \t, etc).
//        // In versions prior to 2.0.46, no escaping was performed on these strings so you had to be quite careful
//        // when dealing with raw log files.
//
//        if (value.equals("request.firstline")   ||  // %r         First line of request.
//            value.startsWith("request.header.") ||  // %{Foobar}i The contents of Foobar: request header line(s).
//            value.startsWith("response.header.")) { // %{Foobar}o The contents of Foobar: response header line(s).
//            return Utils.decodeApacheHTTPDLogValue(value);
//        }

    return value;
  }

  // --------------------------------------------
  @Override
  protected List<TokenParser> createAllTokenParsers() {
    List<TokenParser> parsers = new ArrayList<>(60);

//      http://nginx.org/en/docs/http/ngx_http_log_module.html#log_format


    // -------
//      $bytes_sent
//      the number of bytes sent to a client
    parsers.add(new FixedStringTokenParser("$bytes_sent")); // TODO: Implement $bytes_sent token
    // -------
//      $connection
    parsers.add(new FixedStringTokenParser("$connection")); // TODO: Implement $connection token
//      connection serial number

    // -------
//      $connection_requests
    parsers.add(new FixedStringTokenParser("$connection_requests")); // TODO: Implement $connection_requests token
//      the current number of requests made through a connection (1.1.18)

    // -------
//      $msec
    parsers.add(new FixedStringTokenParser("$msec")); // TODO: Implement $msec token
//      time in seconds with a milliseconds resolution at the time of the log write

    // -------
//      $pipe
    parsers.add(new FixedStringTokenParser("$pipe")); // TODO: Implement $pipe token
//      “p” if request was pipelined, “.” otherwise

    // -------
//      $request_length
    parsers.add(new FixedStringTokenParser("$request_length")); // TODO: Implement $request_length token
//      request length (including request line, header, and request body)

    // -------
//      $request_time
    parsers.add(new FixedStringTokenParser("$request_time")); // TODO: Implement $request_time token
//      request processing time in seconds with a milliseconds resolution; time elapsed between the first bytes were read from the client and the log write after the last bytes were sent to the client

    // -------
//      $status
    parsers.add(new FixedStringTokenParser("$status")); // TODO: Implement $status token
//      response status

    // -------
//      $time_iso8601
    parsers.add(new FixedStringTokenParser("$time_iso8601")); // TODO: Implement $time_iso8601 token
//      local time in the ISO 8601 standard format

    // -------
//      $time_local
    parsers.add(new FixedStringTokenParser("$time_local")); // TODO: Implement $time_local token
//      local time in the Common Log Format

    // -------
//      Header lines sent to a client have the prefix “sent_http_”, for example, $sent_http_content_range.

//      http://nginx.org/en/docs/http/ngx_http_core_module.html#var_bytes_sent
    // -------
//      $arg_name
    parsers.add(new FixedStringTokenParser("$arg_name")); // TODO: Implement $arg_name token
//      argument name in the request line

    // -------
//      $args
    parsers.add(new FixedStringTokenParser("$args")); // TODO: Implement $args token
//      arguments in the request line
    // -------
//      $query_string
    parsers.add(new FixedStringTokenParser("$query_string")); // TODO: Implement $query_string token
//      same as $args

    // -------
//      $binary_remote_addr
    parsers.add(new FixedStringTokenParser("$binary_remote_addr")); // TODO: Implement $binary_remote_addr token
//      client address in a binary form, value’s length is always 4 bytes

    // -------
//      $body_bytes_sent
    parsers.add(new FixedStringTokenParser("$body_bytes_sent")); // TODO: Implement $body_bytes_sent token
//      number of bytes sent to a client, not counting the response header; this variable is compatible with the “%B” parameter of the mod_log_config Apache module

    // -------
//      $bytes_sent
    parsers.add(new FixedStringTokenParser("$bytes_sent")); // TODO: Implement $bytes_sent token
//      number of bytes sent to a client (1.3.8, 1.2.5)

    // -------
//      $connection
    parsers.add(new FixedStringTokenParser("$connection")); // TODO: Implement $connection token
//      connection serial number (1.3.8, 1.2.5)

    // -------
//      $connection_requests
    parsers.add(new FixedStringTokenParser("$connection_requests")); // TODO: Implement $connection_requests token
//      current number of requests made through a connection (1.3.8, 1.2.5)

    // -------
//      $content_length
//      “Content-Length” request header field
    parsers.add(new TokenParser("\\%\\{([a-z0-9\\-_]*)\\}i",
            "request.header.content_length", "HTTP.HEADER",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

    // -------
//      $content_type
//      “Content-Type” request header field
    parsers.add(new TokenParser("$content_type",
            "request.cookies.content-type", "STRING",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

    // -------
//      $cookie_name
//      the name cookie
    parsers.add(new NamedTokenParser("$cookie_([a-z0-9\\-_]*)",
            "request.header.", "HTTP.HEADER",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING));

    // -------
//      $document_root
    parsers.add(new FixedStringTokenParser("$document_root")); // TODO: Implement $document_root token
//      root or alias directive’s value for the current request

    // -------
//      $host
    parsers.add(new FixedStringTokenParser("$host")); // TODO: Implement $host token
//      in this order of precedence: host name from the request line, or host name from the “Host” request header field, or the server name matching a request

    // -------
//      $hostname
    parsers.add(new FixedStringTokenParser("$hostname")); // TODO: Implement $hostname token
//      host name

    // -------
//      $http_name
//      arbitrary request header field; the last part of a variable name is the field name converted to lower case with dashes replaced by underscores
    parsers.add(new NamedTokenParser("\\%\\{([a-z0-9\\-_]*)\\}i",
            "request.header.", "HTTP.HEADER",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING));



    // -------
//      $https
    parsers.add(new FixedStringTokenParser("$https")); // TODO: Implement $https token
//      “on” if connection operates in SSL mode, or an empty string otherwise


    // -------
//      $is_args
    parsers.add(new FixedStringTokenParser("$is_args")); // TODO: Implement $is_args token
//      “?” if a request line has arguments, or an empty string otherwise


    // -------
//      $limit_rate
    parsers.add(new FixedStringTokenParser("$limit_rate")); // TODO: Implement $limit_rate token
//      setting this variable enables response rate limiting; see limit_rate

    // -------
//      $msec
    parsers.add(new FixedStringTokenParser("$msec")); // TODO: Implement $msec token
//      current time in seconds with the milliseconds resolution (1.3.9, 1.2.6)

    // -------
//      $nginx_version
    parsers.add(new FixedStringTokenParser("$nginx_version")); // TODO: Implement $nginx_version token
//      nginx version

    // -------
//      $pid
    parsers.add(new FixedStringTokenParser("$pid")); // TODO: Implement $pid token
//      PID of the worker process

    // -------
//      $pipe
    parsers.add(new FixedStringTokenParser("$pipe")); // TODO: Implement $pipe token
//      “p” if request was pipelined, “.” otherwise (1.3.12, 1.2.7)

    // -------
//      $proxy_protocol_addr
    parsers.add(new FixedStringTokenParser("$proxy_protocol_addr")); // TODO: Implement $proxy_protocol_addr token
//      client address from the PROXY protocol header, or an empty string otherwise (1.5.12)
//      The PROXY protocol must be previously enabled by setting the proxy_protocol parameter in the listen directive.


    // -------
//      $realpath_root
    parsers.add(new FixedStringTokenParser("$realpath_root")); // TODO: Implement $realpath_root token
//      an absolute pathname corresponding to the root or alias directive’s value for the current request, with all symbolic links resolved to real paths

    // -------
//      $remote_addr
    parsers.add(new FixedStringTokenParser("$remote_addr")); // TODO: Implement $remote_addr token
//      client address
//    parsers.add(new TokenParser("%a",
//            "connection.client.ip", "$remote_addr",
//            Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));

    // -------
//      $remote_port
    parsers.add(new FixedStringTokenParser("$remote_port")); // TODO: Implement $remote_port token
//      client port

    // -------
//      $remote_user
    parsers.add(new FixedStringTokenParser("$remote_user")); // TODO: Implement $remote_user token
//      user name supplied with the Basic authentication

    //TODO: Add basic authentication parsing to Apache too!!

    // -------
//      $request
    parsers.add(new FixedStringTokenParser("$request")); // TODO: Implement $request token
//      full original request line
    parsers.add(new TokenParser("%r",
            "request.firstline", "HTTP.FIRSTLINE",
            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
            TokenParser.FORMAT_NO_SPACE_STRING + " " +
            TokenParser.FORMAT_NO_SPACE_STRING));


    // -------
//      $request_body
    parsers.add(new FixedStringTokenParser("$request_body")); // TODO: Implement $request_body token
//      request body
//      The variable’s value is made available in locations processed by the proxy_pass, fastcgi_pass, uwsgi_pass, and scgi_pass directives.


    // -------
//      $request_body_file
    parsers.add(new FixedStringTokenParser("$request_body_file")); // TODO: Implement $request_body_file token
//      name of a temporary file with the request body
//      At the end of processing, the file needs to be removed. To always write the request body to a file, client_body_in_file_only needs to be enabled. When the name of a temporary file is passed in a proxied request or in a request to a FastCGI/uwsgi/SCGI server, passing the request body should be disabled by the proxy_pass_request_body off, fastcgi_pass_request_body off, uwsgi_pass_request_body off, or scgi_pass_request_body off directives, respectively.


    // -------
//      $request_completion
    parsers.add(new FixedStringTokenParser("$request_completion")); // TODO: Implement $request_completion token
//      “OK” if a request has completed, or an empty string otherwise

    // -------
//      $request_filename
    parsers.add(new FixedStringTokenParser("$request_filename")); // TODO: Implement $request_filename token
//      file path for the current request, based on the root or alias directives, and the request URI

    // -------
//      $request_length
    parsers.add(new FixedStringTokenParser("$request_length")); // TODO: Implement $request_length token
//      request length (including request line, header, and request body) (1.3.12, 1.2.7)

    // -------
//      $request_method
    parsers.add(new FixedStringTokenParser("$request_method")); // TODO: Implement $request_method token
//      request method, usually “GET” or “POST”
//    parsers.add(new TokenParser("%r",
//            "request.firstline", "HTTP.FIRSTLINE",
//            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING));
//    result.add("HTTP.METHOD:method");

    // -------
//      $request_time
    parsers.add(new FixedStringTokenParser("$request_time")); // TODO: Implement $request_time token
//      request processing time in seconds with a milliseconds resolution (1.3.9, 1.2.6); time elapsed since the first bytes were read from the client

    // -------
//      $request_uri
    parsers.add(new FixedStringTokenParser("$request_uri")); // TODO: Implement $request_uri token
//      full original request URI (with arguments)
//    parsers.add(new TokenParser("%r",
//            "request.firstline", "HTTP.FIRSTLINE",
//            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING));
//    result.add("HTTP.URI:uri");


    // -------
//      $scheme
//      request scheme, “http” or “https”

    parsers.add(new TokenParser("$scheme",
            "request.firstline.uri.protocol", "HTTP.PROTOCOL",
            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

    // -------
//      $sent_http_name
    parsers.add(new FixedStringTokenParser("$sent_http_name")); // TODO: Implement $sent_http_name token
//      arbitrary response header field; the last part of a variable name is the field name converted to lower case with dashes replaced by underscores


    // -------
//      $server_addr
//      an address of the server which accepted a request
//      Computing a value of this variable usually requires one system call. To avoid a system call, the listen directives must specify addresses and use the bind parameter.
    parsers.add(new TokenParser("$server_addr",
            "connection.server.ip", "IP",
            Casts.STRING_OR_LONG, TokenParser.FORMAT_CLF_IP));

    // -------
//      $server_name
//      name of the server which accepted a request
    parsers.add(new TokenParser("$server_name",
            "connection.server.name", "STRING",
            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

    // -------
//      $server_port
//      port of the server which accepted a request
    parsers.add(new TokenParser("$server_port",
            "connection.server.port", "PORT",
            Casts.STRING_OR_LONG, TokenParser.FORMAT_NUMBER));

    // -------
//      $server_protocol
    parsers.add(new FixedStringTokenParser("$server_protocol")); // TODO: Implement $server_protocol token
//      request protocol, usually “HTTP/1.0” or “HTTP/1.1”
//    parsers.add(new TokenParser("%r",
//            "request.firstline", "HTTP.FIRSTLINE",
//            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING + " " +
//            TokenParser.FORMAT_NO_SPACE_STRING));
//    result.add("HTTP.URI:uri");
//    result.add("HTTP.PROTOCOL:protocol");
//    result.add("HTTP.PROTOCOL.VERSION:protocol.version");

    // -------
//      $status
    parsers.add(new FixedStringTokenParser("$status")); // TODO: Implement $status token
//      response status (1.3.2, 1.2.2)
    parsers.add(new TokenParser("%s",
            "request.status.original", "STRING",
            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING));

    // -------
//      $tcpinfo_rtt, $tcpinfo_rttvar, $tcpinfo_snd_cwnd, $tcpinfo_rcv_space
//      information about the client TCP connection; available on systems that support the TCP_INFO socket option
//      $tcpinfo_rtt
    parsers.add(new FixedStringTokenParser("$tcpinfo_rtt")); // TODO: Implement $tcpinfo_rtt token
//      $tcpinfo_rttvar
    parsers.add(new FixedStringTokenParser("$tcpinfo_rttvar")); // TODO: Implement $tcpinfo_rttvar token
//      $tcpinfo_snd_cwnd
    parsers.add(new FixedStringTokenParser("$tcpinfo_snd_cwnd")); // TODO: Implement $tcpinfo_snd_cwnd token
//      $tcpinfo_rcv_space
    parsers.add(new FixedStringTokenParser("$tcpinfo_rcv_space")); // TODO: Implement $tcpinfo_rcv_space token

    // -------
//      $time_iso8601
//      local time in the ISO 8601 standard format (1.3.12, 1.2.7)
    parsers.add(new TokenParser("%t",
            "request.receive.time", "TIME.STAMP",
            Casts.STRING_ONLY, TokenParser.FORMAT_STANDARD_TIME_US));

    // -------
//      $time_local
    parsers.add(new FixedStringTokenParser("$time_local")); // TODO: Implement $time_local token
//      local time in the Common Log Format (1.3.12, 1.2.7)


    // -------
//      $uri
    parsers.add(new FixedStringTokenParser("$uri")); // TODO: Implement $uri token
//      current URI in request, normalized
//      The value of $uri may change during request processing, e.g. when doing internal redirects, or when using index files.
    // -------
//      $document_uri
    parsers.add(new FixedStringTokenParser("$document_uri")); // TODO: Implement $document_uri token
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



    // Some explicit type overrides.
    // The '1' at the end indicates this is more important than the default TokenParser (which has an implicit 0).
    parsers.add(new TokenParser("%{cookie}i",
            "request.cookies", "HTTP.COOKIES",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
    parsers.add(new TokenParser("%{set-cookie}o",
            "response.cookies", "HTTP.SETCOOKIES",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
    parsers.add(new TokenParser("%{user-agent}i",
            "request.user-agent", "HTTP.USERAGENT",
            Casts.STRING_ONLY, TokenParser.FORMAT_STRING, 1));
    parsers.add(new TokenParser("%{referer}i",
            "request.referer", "HTTP.URI",
            Casts.STRING_ONLY, TokenParser.FORMAT_NO_SPACE_STRING, 1));

    return parsers;
  }
}
