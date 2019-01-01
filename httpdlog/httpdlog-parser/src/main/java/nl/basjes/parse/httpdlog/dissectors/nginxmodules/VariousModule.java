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

import nl.basjes.parse.httpdlog.dissectors.tokenformat.NamedTokenParser;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NUMBER_OPTIONAL_DECIMAL;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

// Many nginx modules only have a small number of variables:
public class VariousModule implements NginxModule {

    private static final String PREFIX = "nginxmodule";

    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_secure_link_module.html#var_secure_link

        // $secure_link
        // The status of a link check. The specific value depends on the selected operation mode.
        parsers.add(new TokenParser("$secure_link", PREFIX + ".secure_link.status", "STRING", STRING_ONLY, FORMAT_STRING));
        // $secure_link_expires
        // The lifetime of a link passed in a request; intended to be used only in the secure_link_md5 directive.
        // --- NOT FOR LOGGING ---

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_session_log_module.html#var_session_log_id

        // $session_log_id
        // current session ID;
        parsers.add(new TokenParser("$session_log_id", PREFIX + ".session_log.id", "STRING", STRING_ONLY, FORMAT_STRING));
        // NOT FOR LOGGING      $ session_log_binary_id         current session ID in binary form (16 bytes).

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_slice_module.html#var_slice_range
        // $slice_range
        // the current slice range in HTTP byte range format, for example, bytes=0-1048575.
        parsers.add(new TokenParser("$slice_range", PREFIX + ".slice_range", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_proxy_module.html#var_proxy_add_x_forwarded_for

        // $proxy_host
        // name and port of a proxied server as specified in the proxy_pass directive;
        parsers.add(new TokenParser("$proxy_host", PREFIX + ".proxy.host", "STRING", STRING_ONLY, FORMAT_NO_SPACE_STRING));
        // $proxy_port
        // port of a proxied server as specified in the proxy_pass directive, or the protocol’s default port;
        parsers.add(new TokenParser("$proxy_port", PREFIX + ".proxy.port", "STRING", STRING_ONLY, FORMAT_NO_SPACE_STRING));
        // $proxy_add_x_forwarded_for
        // the “X-Forwarded-For” client request header field with the $remote_addr variable appended to it, separated by a comma.
        // If the “X-Forwarded-For” field is not present in the client request header,
        // the $proxy_add_x_forwarded_for variable is equal to the $remote_addr variable.
        parsers.add(new TokenParser("$proxy_add_x_forwarded_for",
            PREFIX + ".proxy.add_x_forwarded_for", "STRING", STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_userid_module.html#var_uid_got

        // $uid_got
        // The cookie name and received client identifier.
        parsers.add(new TokenParser("$uid_got", PREFIX + ".userid.uid_got", "STRING", STRING_ONLY, FORMAT_STRING));
        // $uid_reset
        // If the variable is set to a non-empty string that is not “0”, the client identifiers are reset.
        // The special value “log” additionally leads to the output of messages about the reset identifiers to the error_log.
        parsers.add(new TokenParser("$uid_reset", PREFIX + ".userid.uid_reset", "STRING", STRING_ONLY, FORMAT_STRING));
        // $uid_set
        // The cookie name and sent client identifier.
        parsers.add(new TokenParser("$uid_set", PREFIX + ".userid.uid_set", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_browser_module.html#var_msie

        // $modern_browser
        // equals the value set by the modern_browser_value directive, if a browser was identified as modern;
        parsers.add(new TokenParser("$modern_browser", PREFIX + ".browser.modern", "STRING", STRING_ONLY, FORMAT_STRING));
        // $ancient_browser
        // equals the value set by the ancient_browser_value directive, if a browser was identified as ancient;
        parsers.add(new TokenParser("$ancient_browser", PREFIX + ".browser.ancient", "STRING", STRING_ONLY, FORMAT_STRING));
        // $msie
        // equals “1” if a browser was identified as MSIE of any version.
        parsers.add(new TokenParser("$msie", PREFIX + ".browser.msie", "STRING", STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_stub_status_module.html#var_connections_active

        // $connections_active
        // The current number of active client connections including Waiting connections.
        parsers.add(new TokenParser("$connections_active", PREFIX + ".stub_status.connections.active", "STRING", STRING_ONLY, FORMAT_STRING));
        // $connections_reading
        // The current number of connections where nginx is reading the request header.
        parsers.add(new TokenParser("$connections_reading", PREFIX + ".stub_status.connections.reading", "STRING", STRING_ONLY, FORMAT_STRING));
        // $connections_writing
        // The current number of connections where nginx is writing the response back to the client.
        parsers.add(new TokenParser("$connections_writing", PREFIX + ".stub_status.connections.writing", "STRING", STRING_ONLY, FORMAT_STRING));
        // $connections_waiting
        // The current number of idle client connections waiting for a request.
        parsers.add(new TokenParser("$connections_waiting", PREFIX + ".stub_status.connections.waiting", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_ssi_module.html#var_date_gmt
        // NOTE: These two are STRING because the actual format is unknown (it is configurable)

        // $date_local
        // current time in the local time zone. The format is set by the config command with the timefmt parameter.
        parsers.add(new TokenParser("$date_local", PREFIX + ".date.local", "STRING", STRING_ONLY, FORMAT_STRING));

        // $date_gmt
        // current time in GMT. The format is set by the config command with the timefmt parameter.
        parsers.add(new TokenParser("$date_gmt", PREFIX + ".date.gmt", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // $fastcgi_script_name

        // request URI or, if a URI ends with a slash, request URI with an index file name configured by the fastcgi_index
        // directive appended to it.
        parsers.add(new TokenParser("$fastcgi_script_name", PREFIX + ".fastcgi.script_name", "STRING", STRING_ONLY, FORMAT_STRING));

        // $fastcgi_path_info
        // the value of the second capture set by the fastcgi_split_path_info directive. This variable can be used to set the PATH_INFO parameter.
        parsers.add(new TokenParser("$fastcgi_path_info", PREFIX + ".fastcgi.path_info", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_gzip_module.html#var_gzip_ratio

        // $gzip_ratio
        // achieved compression ratio, computed as the ratio between the original and compressed response sizes.
        parsers.add(new TokenParser("$gzip_ratio", PREFIX + ".gzip.ratio", "STRING", STRING_ONLY, FORMAT_NUMBER_OPTIONAL_DECIMAL));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_spdy_module.html#var_spdy

        // $spdy
        // SPDY protocol version for SPDY connections, or an empty string otherwise;
        parsers.add(new TokenParser("$spdy", PREFIX + ".spdy.version", "STRING", STRING_ONLY, FORMAT_STRING));
        // $spdy_request_priority
        // request priority for SPDY connections, or an empty string otherwise.
        parsers.add(new TokenParser("$spdy_request_priority", PREFIX + ".spdy.request_priority", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_v2_module.html#var_http2
        // $http2
        // negotiated protocol identifier: “h2” for HTTP/2 over TLS, “h2c” for HTTP/2 over cleartext TCP, or an empty string otherwise.
        parsers.add(new TokenParser("$http2", PREFIX + ".http2.negotiated_protocol", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_referer_module.html#var_invalid_referer
        // $invalid_referer
        // Empty string, if the “Referer” request header field value is considered valid, otherwise “1”.
        parsers.add(new TokenParser("$invalid_referer", PREFIX + ".referer.invalid", "STRING", STRING_ONLY, "1?"));

        // -----------------------------------------------------------------------------------------------------

        // http://nginx.org/en/docs/http/ngx_http_auth_jwt_module.html#var_jwt_claim_
        // $jwt_header_name
        // returns the value of a specified JOSE header
        parsers.add(new NamedTokenParser("\\$jwt_header_([a-z0-9\\-_]*)",
            PREFIX + ".jwt.header.", "STRING",
            STRING_ONLY, FORMAT_STRING));

        // $jwt_claim_name
        // returns the value of a specified JWT claim
        parsers.add(new NamedTokenParser("\\$jwt_claim_([a-z0-9\\-_]*)",
            PREFIX + ".jwt.claim.", "STRING",
            STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_memcached_module.html#var_memcached_key

        // $memcached_key
        // Defines a key for obtaining response from a memcached server.
        parsers.add(new TokenParser("$memcached_key", PREFIX + ".memcached.key", "STRING", STRING_ONLY, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------
        // http://nginx.org/en/docs/http/ngx_http_realip_module.html#var_realip_remote_addr
        // http://nginx.org/en/docs/stream/ngx_stream_realip_module.html#var_realip_remote_addr

        // $realip_remote_addr
        // keeps the original client address
        parsers.add(new TokenParser("$realip_remote_addr", PREFIX + ".realip.remote_addr", "IP", STRING_ONLY, FORMAT_STRING));

        // $realip_remote_port
        // keeps the original client port
        parsers.add(new TokenParser("$realip_remote_port", PREFIX + ".realip.remote_port", "PORT", STRING_OR_LONG, FORMAT_STRING));

        // -----------------------------------------------------------------------------------------------------

        return parsers;
    }
}
