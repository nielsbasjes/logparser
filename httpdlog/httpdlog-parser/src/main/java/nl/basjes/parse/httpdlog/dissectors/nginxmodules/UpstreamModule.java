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

package nl.basjes.parse.httpdlog.dissectors.nginxmodules;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER_DOT_NUMBER;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

// Implement the tokens described here:
// http://nginx.org/en/docs/http/ngx_http_upstream_module.html#variables
// http://nginx.org/en/docs/stream/ngx_stream_upstream_module.html#variables
public class UpstreamModule implements NginxModule {

    private static final String PREFIX = "nginxmodule.upstream";

    private String upstreamListOf(String regex) {
        return regex + "(?: *, *" + regex + "(?: *: *" + regex + ")?)*";
    }

    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // $upstream_addr
        // keeps the IP address and port, or the path to the UNIX-domain socket of the upstream server.
        // If several servers were contacted during request processing, their addresses are separated by commas,
        // e.g. “192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock”.
        //
        // If an internal redirect from one server group to another happens, initiated by “X-Accel-Redirect”
        // or error_page, then the server addresses from different groups are separated by colons,
        // e.g. “192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock : 192.168.10.1:80, 192.168.10.2:80”.
        //
        // If a server cannot be selected, the variable keeps the name of the server group.
        parsers.add(new TokenParser("$upstream_addr",
            PREFIX + ".addr", "UPSTREAM_ADDR_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NO_SPACE_STRING)));

        // $upstream_bytes_received
        // number of bytes received from an upstream server.
        // Values from several connections are separated by commas and colons like addresses
        // in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_bytes_received",
            PREFIX + ".bytes.received", "UPSTREAM_BYTES_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER)));

        // $upstream_bytes_sent
        // number of bytes sent to an upstream server.
        // Values from several connections are separated by commas and colons like addresses
        // in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_bytes_sent",
            PREFIX + ".bytes.sent", "UPSTREAM_BYTES_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER)));

        // $upstream_cache_status
        // keeps the status of accessing a response cache.
        // The status can be either “MISS”, “BYPASS”, “EXPIRED”, “STALE”, “UPDATING”, “REVALIDATED”, or “HIT”.
        parsers.add(new TokenParser("$upstream_cache_status",
            PREFIX + ".cache.status", "UPSTREAM_CACHE_STATUS",
            Casts.STRING_ONLY, "(?:MISS|BYPASS|EXPIRED|STALE|UPDATING|REVALIDATED|HIT)"));

        // $upstream_connect_time
        // keeps time spent on establishing a connection with the upstream server (1.9.1);
        // the time is kept in seconds with millisecond resolution. In case of SSL, includes time spent on handshake.
        // Times of several connections are separated by commas and colons like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_connect_time",
            PREFIX + ".connect.time", "UPSTREAM_SECOND_MILLIS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER_DOT_NUMBER)));

        // $upstream_cookie_name
        // cookie with the specified name sent by the upstream server in the “Set-Cookie” response header field (1.7.1).
        // Only the cookies from the response of the last server are saved.
        parsers.add(new NamedTokenParser("\\$upstream_cookie_([a-z0-9\\-_]*)",
            PREFIX + ".response.cookies.", "HTTP.COOKIE",
            Casts.STRING_ONLY, FORMAT_STRING));

        // $upstream_header_time
        // keeps time spent on receiving the response header from the upstream server (1.7.10);
        // the time is kept in seconds with millisecond resolution.
        // Times of several responses are separated by commas and colons like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_header_time",
            PREFIX + ".header.time", "UPSTREAM_SECOND_MILLIS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER_DOT_NUMBER)));

        // $upstream_http_name
        // keep server response header fields. For example, the “Server” response header field is available through
        // the $upstream_http_server variable. The rules of converting header field names to variable names are
        // the same as for the variables that start with the “$http_” prefix.
        // Only the header fields from the response of the last server are saved.
        parsers.add(new NamedTokenParser("\\$upstream_http_([a-z0-9\\-_]*)",
            PREFIX + ".header.", "HTTP.HEADER",
            Casts.STRING_ONLY, FORMAT_STRING));

        // $upstream_queue_time
        // keeps time the request spent in the upstream queue (1.13.9);
        // the time is kept in seconds with millisecond resolution.
        // Times of several responses are separated by commas and colons like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_queue_time",
            PREFIX + ".queue.time", "UPSTREAM_SECOND_MILLIS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER_DOT_NUMBER)));

        // $upstream_response_length
        // keeps the length of the response obtained from the upstream server (0.7.27);
        // the length is kept in bytes.
        // Lengths of several responses are separated by commas and colons like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_response_length",
            PREFIX + ".response.length", "UPSTREAM_BYTES_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER)));

        // $upstream_response_time
        // keeps time spent on receiving the response from the upstream server;
        // the time is kept in seconds with millisecond resolution.
        // Times of several responses are separated by commas and colons like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_response_time",
            PREFIX + ".response.time", "UPSTREAM_SECOND_MILLIS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER_DOT_NUMBER)));

        // $upstream_status
        // keeps status code of the response obtained from the upstream server.
        // Status codes of several responses are separated by commas and colons like addresses in the $upstream_addr
        // variable. If a server cannot be selected, the variable keeps the 502 (Bad Gateway) status code.
        parsers.add(new TokenParser("$upstream_status",
            PREFIX + ".status", "UPSTREAM_STATUS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NO_SPACE_STRING)));

        // $upstream_trailer_name
        // keeps fields from the end of the response obtained from the upstream server (1.13.10).
        parsers.add(new NamedTokenParser("\\$upstream_trailer_([a-z0-9\\-_]*)",
            PREFIX + ".trailer.", "HTTP.TRAILER",
            Casts.STRING_ONLY, FORMAT_STRING));

        // $upstream_first_byte_time
        // time to receive the first byte of data (1.11.4); the time is kept in seconds with millisecond resolution.
        // Times of several connections are separated by commas like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_first_byte_time",
            PREFIX + ".first_byte.time", "UPSTREAM_SECOND_MILLIS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER_DOT_NUMBER)));

        // $upstream_session_time
        // session duration in seconds with millisecond resolution (1.11.4).
        // Times of several connections are separated by commas like addresses in the $upstream_addr variable.
        parsers.add(new TokenParser("$upstream_session_time",
            PREFIX + ".session.time", "UPSTREAM_SECOND_MILLIS_LIST",
            Casts.STRING_ONLY, upstreamListOf(FORMAT_NUMBER_DOT_NUMBER)));

        return parsers;
    }

    @Override
    public List<Dissector> getDissectors() {
        List<Dissector> dissectors = new ArrayList<>();

        dissectors.add(new UpstreamListDissector(
            "UPSTREAM_ADDR_LIST",
            "UPSTREAM_ADDR",            Casts.STRING_ONLY,
            "UPSTREAM_ADDR",            Casts.STRING_ONLY));

        dissectors.add(new UpstreamListDissector(
            "UPSTREAM_BYTES_LIST",
            "BYTES",           Casts.STRING_OR_LONG,
            "BYTES",           Casts.STRING_OR_LONG));

        dissectors.add(new UpstreamListDissector(
            "UPSTREAM_SECOND_MILLIS_LIST",
            "SECOND_MILLIS",   Casts.STRING_OR_LONG_OR_DOUBLE,
            "SECOND_MILLIS",   Casts.STRING_OR_LONG_OR_DOUBLE));

        dissectors.add(new UpstreamListDissector(
            "UPSTREAM_STATUS_LIST",
            "UPSTREAM_STATUS",          Casts.STRING_ONLY,
            "UPSTREAM_STATUS",          Casts.STRING_ONLY));

        return dissectors;
    }
}
