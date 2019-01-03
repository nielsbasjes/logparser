/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

package nl.basjes.parse.httpdlog.nginxmodules;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;

// This test simply checks which of the fields known in the Nginx documentation have been implemented.
public class NginxAllFieldsTest {
    // All variables mentioned on https://nginx.org/en/docs/varindex.html

    @Test
    public void ensureAllFieldsAreHandled() {
        checkVariable("$arg_");                        // ngx_http_core_module
        checkVariable("$args");                        // ngx_http_core_module
        checkVariable("$binary_remote_addr");          // ngx_http_core_module
        checkVariable("$binary_remote_addr");          // ngx_stream_core_module
        checkVariable("$body_bytes_sent");             // ngx_http_core_module
        checkVariable("$bytes_received");              // ngx_stream_core_module
        checkVariable("$bytes_sent");                  // ngx_http_core_module
        checkVariable("$bytes_sent");                  // ngx_http_log_module
        checkVariable("$bytes_sent");                  // ngx_stream_core_module
        checkVariable("$connection");                  // ngx_http_core_module
        checkVariable("$connection");                  // ngx_http_log_module
        checkVariable("$connection");                  // ngx_stream_core_module
        checkVariable("$connection_requests");         // ngx_http_core_module
        checkVariable("$connection_requests");         // ngx_http_log_module
        checkVariable("$content_length");              // ngx_http_core_module
        checkVariable("$content_type");                // ngx_http_core_module
        checkVariable("$cookie_");                     // ngx_http_core_module
        checkVariable("$document_root");               // ngx_http_core_module
        checkVariable("$document_uri");                // ngx_http_core_module
        checkVariable("$host");                        // ngx_http_core_module
        checkVariable("$hostname");                    // ngx_http_core_module
        checkVariable("$hostname");                    // ngx_stream_core_module
        checkVariable("$http_");                       // ngx_http_core_module
        checkVariable("$https");                       // ngx_http_core_module
        checkVariable("$is_args");                     // ngx_http_core_module
        checkVariable("$limit_rate");                  // ngx_http_core_module
        checkVariable("$msec");                        // ngx_http_core_module
        checkVariable("$msec");                        // ngx_http_log_module
        checkVariable("$msec");                        // ngx_stream_core_module
        checkVariable("$nginx_version");               // ngx_http_core_module
        checkVariable("$nginx_version");               // ngx_stream_core_module
        checkVariable("$pid");                         // ngx_http_core_module
        checkVariable("$pid");                         // ngx_stream_core_module
        checkVariable("$pipe");                        // ngx_http_core_module
        checkVariable("$pipe");                        // ngx_http_log_module
        checkVariable("$protocol");                    // ngx_stream_core_module

        checkVariable("$proxy_protocol_addr");         // ngx_http_core_module
        checkVariable("$proxy_protocol_addr");         // ngx_stream_core_module
        checkVariable("$proxy_protocol_port");         // ngx_http_core_module
        checkVariable("$proxy_protocol_port");         // ngx_stream_core_module
        checkVariable("$query_string");                // ngx_http_core_module
        checkVariable("$realpath_root");               // ngx_http_core_module
        checkVariable("$remote_addr");                 // ngx_http_core_module
        checkVariable("$remote_addr");                 // ngx_stream_core_module
        checkVariable("$remote_port");                 // ngx_http_core_module
        checkVariable("$remote_port");                 // ngx_stream_core_module
        checkVariable("$remote_user");                 // ngx_http_core_module
        checkVariable("$request");                     // ngx_http_core_module
        checkVariable("$request_body");                // ngx_http_core_module
        checkVariable("$request_body_file");           // ngx_http_core_module
        checkVariable("$request_completion");          // ngx_http_core_module
        checkVariable("$request_filename");            // ngx_http_core_module
        checkVariable("$request_id");                  // ngx_http_core_module
        checkVariable("$request_length");              // ngx_http_core_module
        checkVariable("$request_length");              // ngx_http_log_module
        checkVariable("$request_method");              // ngx_http_core_module
        checkVariable("$request_time");                // ngx_http_core_module
        checkVariable("$request_time");                // ngx_http_log_module
        checkVariable("$request_uri");                 // ngx_http_core_module
        checkVariable("$scheme");                      // ngx_http_core_module
        checkVariable("$sent_http_somename");                  // ngx_http_core_module
        checkVariable("$sent_trailer_somename");               // ngx_http_core_module
        checkVariable("$server_addr");                 // ngx_http_core_module
        checkVariable("$server_addr");                 // ngx_stream_core_module
        checkVariable("$server_name");                 // ngx_http_core_module
        checkVariable("$server_port");                 // ngx_http_core_module
        checkVariable("$server_port");                 // ngx_stream_core_module
        checkVariable("$server_protocol");             // ngx_http_core_module
        checkVariable("$session_time");                // ngx_stream_core_module
        checkVariable("$status");                      // ngx_http_core_module
        checkVariable("$status");                      // ngx_http_log_module
        checkVariable("$status");                      // ngx_stream_core_module
        checkVariable("$tcpinfo_rtt");                 // ngx_http_core_module
        checkVariable("$tcpinfo_rttvar");              // ngx_http_core_module
        checkVariable("$tcpinfo_snd_cwnd");            // ngx_http_core_module
        checkVariable("$tcpinfo_rcv_space");           // ngx_http_core_module
        checkVariable("$time_iso8601");                // ngx_http_core_module
        checkVariable("$time_iso8601");                // ngx_http_log_module
        checkVariable("$time_iso8601");                // ngx_stream_core_module
        checkVariable("$time_local");                  // ngx_http_core_module
        checkVariable("$time_local");                  // ngx_http_log_module
        checkVariable("$time_local");                  // ngx_stream_core_module

        checkVariable("$secure_link");                 // ngx_http_secure_link_module
// NOT FOR LOGGING: checkVariable("$secure_link_expires");         // ngx_http_secure_link_module

// NOT FOR LOGGING: checkVariable("$session_log_binary_id");       // ngx_http_session_log_module
        checkVariable("$session_log_id");              // ngx_http_session_log_module

        checkVariable("$slice_range");                 // ngx_http_slice_module

        checkVariable("$proxy_add_x_forwarded_for");   // ngx_http_proxy_module
        checkVariable("$proxy_host");                  // ngx_http_proxy_module
        checkVariable("$proxy_port");                  // ngx_http_proxy_module

        checkVariable("$ssl_cipher");                  // ngx_http_ssl_module
        checkVariable("$ssl_cipher");                  // ngx_stream_ssl_module
        checkVariable("$ssl_ciphers");                 // ngx_http_ssl_module
        checkVariable("$ssl_ciphers");                 // ngx_stream_ssl_module
        checkVariable("$ssl_client_cert");             // ngx_http_ssl_module
        checkVariable("$ssl_client_cert");             // ngx_stream_ssl_module
        checkVariable("$ssl_client_escaped_cert");     // ngx_http_ssl_module
        checkVariable("$ssl_client_fingerprint");      // ngx_http_ssl_module
        checkVariable("$ssl_client_fingerprint");      // ngx_stream_ssl_module
        checkVariable("$ssl_client_i_dn");             // ngx_http_ssl_module
        checkVariable("$ssl_client_i_dn");             // ngx_stream_ssl_module
        checkVariable("$ssl_client_i_dn_legacy");      // ngx_http_ssl_module
        checkVariable("$ssl_client_raw_cert");         // ngx_http_ssl_module
        checkVariable("$ssl_client_raw_cert");         // ngx_stream_ssl_module
        checkVariable("$ssl_client_s_dn");             // ngx_http_ssl_module
        checkVariable("$ssl_client_s_dn");             // ngx_stream_ssl_module
        checkVariable("$ssl_client_s_dn_legacy");      // ngx_http_ssl_module
        checkVariable("$ssl_client_serial");           // ngx_http_ssl_module
        checkVariable("$ssl_client_serial");           // ngx_stream_ssl_module
        checkVariable("$ssl_client_v_end");            // ngx_http_ssl_module
        checkVariable("$ssl_client_v_end");            // ngx_stream_ssl_module
        checkVariable("$ssl_client_v_remain");         // ngx_http_ssl_module
        checkVariable("$ssl_client_v_remain");         // ngx_stream_ssl_module
        checkVariable("$ssl_client_v_start");          // ngx_http_ssl_module
        checkVariable("$ssl_client_v_start");          // ngx_stream_ssl_module
        checkVariable("$ssl_client_verify");           // ngx_http_ssl_module
        checkVariable("$ssl_client_verify");           // ngx_stream_ssl_module
        checkVariable("$ssl_curves");                  // ngx_http_ssl_module
        checkVariable("$ssl_curves");                  // ngx_stream_ssl_module
        checkVariable("$ssl_early_data");              // ngx_http_ssl_module
        checkVariable("$ssl_preread_alpn_protocols");  // ngx_stream_ssl_preread_module
        checkVariable("$ssl_preread_protocol");        // ngx_stream_ssl_preread_module
        checkVariable("$ssl_preread_server_name");     // ngx_stream_ssl_preread_module
        checkVariable("$ssl_protocol");                // ngx_http_ssl_module
        checkVariable("$ssl_protocol");                // ngx_stream_ssl_module
        checkVariable("$ssl_server_name");             // ngx_http_ssl_module
        checkVariable("$ssl_server_name");             // ngx_stream_ssl_module
        checkVariable("$ssl_session_id");              // ngx_http_ssl_module
        checkVariable("$ssl_session_id");              // ngx_stream_ssl_module
        checkVariable("$ssl_session_reused");          // ngx_http_ssl_module
        checkVariable("$ssl_session_reused");          // ngx_stream_ssl_module

        checkVariable("$upstream_addr");               // ngx_http_upstream_module
        checkVariable("$upstream_addr");               // ngx_stream_upstream_module
        checkVariable("$upstream_bytes_received");     // ngx_http_upstream_module
        checkVariable("$upstream_bytes_received");     // ngx_stream_upstream_module
        checkVariable("$upstream_bytes_sent");         // ngx_http_upstream_module
        checkVariable("$upstream_bytes_sent");         // ngx_stream_upstream_module
        checkVariable("$upstream_cache_status");       // ngx_http_upstream_module
        checkVariable("$upstream_connect_time");       // ngx_http_upstream_module
        checkVariable("$upstream_connect_time");       // ngx_stream_upstream_module
        checkVariable("$upstream_cookie_");            // ngx_http_upstream_module
        checkVariable("$upstream_first_byte_time");    // ngx_stream_upstream_module
        checkVariable("$upstream_header_time");        // ngx_http_upstream_module
        checkVariable("$upstream_http_");              // ngx_http_upstream_module
        checkVariable("$upstream_queue_time");         // ngx_http_upstream_module
        checkVariable("$upstream_response_length");    // ngx_http_upstream_module
        checkVariable("$upstream_response_time");      // ngx_http_upstream_module
        checkVariable("$upstream_session_time");       // ngx_stream_upstream_module
        checkVariable("$upstream_status");             // ngx_http_upstream_module
        checkVariable("$upstream_trailer_");           // ngx_http_upstream_module
        checkVariable("$uri");                         // ngx_http_core_module

        checkVariable("$uid_got");                     // ngx_http_userid_module
        checkVariable("$uid_reset");                   // ngx_http_userid_module
        checkVariable("$uid_set");                     // ngx_http_userid_module

        checkVariable("$ancient_browser");             // ngx_http_browser_module
        checkVariable("$modern_browser");              // ngx_http_browser_module
        checkVariable("$msie");                        // ngx_http_browser_module

        checkVariable("$connections_active");          // ngx_http_stub_status_module
        checkVariable("$connections_reading");         // ngx_http_stub_status_module
        checkVariable("$connections_waiting");         // ngx_http_stub_status_module
        checkVariable("$connections_writing");         // ngx_http_stub_status_module

        checkVariable("$date_gmt");                    // ngx_http_ssi_module
        checkVariable("$date_local");                  // ngx_http_ssi_module

        checkVariable("$fastcgi_path_info");           // ngx_http_fastcgi_module
        checkVariable("$fastcgi_script_name");         // ngx_http_fastcgi_module

        checkVariable("$geoip_area_code");             // ngx_http_geoip_module
        checkVariable("$geoip_area_code");             // ngx_stream_geoip_module
        checkVariable("$geoip_city");                  // ngx_http_geoip_module
        checkVariable("$geoip_city");                  // ngx_stream_geoip_module
        checkVariable("$geoip_city_continent_code");   // ngx_http_geoip_module
        checkVariable("$geoip_city_continent_code");   // ngx_stream_geoip_module
        checkVariable("$geoip_city_country_code");     // ngx_http_geoip_module
        checkVariable("$geoip_city_country_code");     // ngx_stream_geoip_module
        checkVariable("$geoip_city_country_code3");    // ngx_http_geoip_module
        checkVariable("$geoip_city_country_code3");    // ngx_stream_geoip_module
        checkVariable("$geoip_city_country_name");     // ngx_http_geoip_module
        checkVariable("$geoip_city_country_name");     // ngx_stream_geoip_module
        checkVariable("$geoip_country_code");          // ngx_http_geoip_module
        checkVariable("$geoip_country_code");          // ngx_stream_geoip_module
        checkVariable("$geoip_country_code3");         // ngx_http_geoip_module
        checkVariable("$geoip_country_code3");         // ngx_stream_geoip_module
        checkVariable("$geoip_country_name");          // ngx_http_geoip_module
        checkVariable("$geoip_country_name");          // ngx_stream_geoip_module
        checkVariable("$geoip_dma_code");              // ngx_http_geoip_module
        checkVariable("$geoip_dma_code");              // ngx_stream_geoip_module
        checkVariable("$geoip_latitude");              // ngx_http_geoip_module
        checkVariable("$geoip_latitude");              // ngx_stream_geoip_module
        checkVariable("$geoip_longitude");             // ngx_http_geoip_module
        checkVariable("$geoip_longitude");             // ngx_stream_geoip_module
        checkVariable("$geoip_org");                   // ngx_http_geoip_module
        checkVariable("$geoip_org");                   // ngx_stream_geoip_module
        checkVariable("$geoip_postal_code");           // ngx_http_geoip_module
        checkVariable("$geoip_postal_code");           // ngx_stream_geoip_module
        checkVariable("$geoip_region");                // ngx_http_geoip_module
        checkVariable("$geoip_region");                // ngx_stream_geoip_module
        checkVariable("$geoip_region_name");           // ngx_http_geoip_module
        checkVariable("$geoip_region_name");           // ngx_stream_geoip_module

        checkVariable("$gzip_ratio");                  // ngx_http_gzip_module

        checkVariable("$spdy");                        // ngx_http_spdy_module
        checkVariable("$spdy_request_priority");       // ngx_http_spdy_module
        checkVariable("$http2");                       // ngx_http_v2_module

        checkVariable("$invalid_referer");             // ngx_http_referer_module

        checkVariable("$jwt_claim_foobar");            // ngx_http_auth_jwt_module
        checkVariable("$jwt_header_foobar");           // ngx_http_auth_jwt_module

        checkVariable("$memcached_key");               // ngx_http_memcached_module

        checkVariable("$realip_remote_addr");          // ngx_http_realip_module
        checkVariable("$realip_remote_addr");          // ngx_stream_realip_module
        checkVariable("$realip_remote_port");          // ngx_http_realip_module
        checkVariable("$realip_remote_port");          // ngx_stream_realip_module

        // Everything that is mentioned on
        // https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/log-format/
        checkVariable("$proxy_protocol_addr");         // remote address if proxy protocol is enabled
        checkVariable("$remote_addr");                 // remote address if proxy protocol is disabled (default)
        checkVariable("$the_real_ip");                 // the source IP address of the client
        checkVariable("$remote_user");                 // user name supplied with the Basic authentication
        checkVariable("$time_local");                  // local time in the Common Log Format
        checkVariable("$request");                     // full original request line
        checkVariable("$status");                      // response status
        checkVariable("$body_bytes_sent");             // number of bytes sent to a client, not counting the response header
        checkVariable("$http_referer");                // value of the Referer header
        checkVariable("$http_user_agent");             // value of User-Agent header
        checkVariable("$request_length");              // request length (including request line, header, and request body)
        checkVariable("$request_time");                // time elapsed since the first bytes were read from the client
        checkVariable("$proxy_upstream_name");         // name of the upstream. The format is upstream-<namespace>-<service name>-<service port>
        checkVariable("$upstream_addr");               // the IP address and port (or the path to the domain socket) of the upstream server.
        checkVariable("$upstream_response_length");    // the length of the response obtained from the upstream server
        checkVariable("$upstream_response_time");      // time spent on receiving the response from the upstream server
        checkVariable("$upstream_status");             // status code of the response obtained from the upstream server
        checkVariable("$req_id");                      // the randomly generated ID of the request
        // Additionals
        checkVariable("$namespace");                   // namespace of the ingress
        checkVariable("$ingress_name");                // name of the ingress
        checkVariable("$service_name");                // name of the service
        checkVariable("$service_port");                // port of the service
    }

    public void checkVariable(String variableName) {
        List<String> allPossible =
            DissectorTester.create()
                .verbose()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, "# " + variableName + " #"))
                .getPossible();

        allPossible.forEach(p -> assertFalse(p, p.startsWith("UNKNOWN_NGINX_VARIABLE")));

    }

}
