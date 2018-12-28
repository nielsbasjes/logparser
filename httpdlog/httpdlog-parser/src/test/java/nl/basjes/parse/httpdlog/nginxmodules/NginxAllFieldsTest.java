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

package nl.basjes.parse.httpdlog.nginxmodules;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

// This test simply checks which of the fields known in the Nginx documentation have been implemented.
public class NginxAllFieldsTest {
    // All variables mentioned on http://nginx.org/en/docs/varindex.html

    private static final List<String> ALL_NGINX_VARIABLES = new ArrayList<>(256);

    static {
        ALL_NGINX_VARIABLES.add("$arg_");                        // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$args");                        // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$binary_remote_addr");          // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$binary_remote_addr");          // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$body_bytes_sent");             // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$bytes_received");              // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$bytes_sent");                  // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$bytes_sent");                  // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$bytes_sent");                  // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$connection");                  // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$connection");                  // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$connection");                  // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$connection_requests");         // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$connection_requests");         // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$content_length");              // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$content_type");                // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$cookie_");                     // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$document_root");               // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$document_uri");                // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$host");                        // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$hostname");                    // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$hostname");                    // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$http_");                       // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$https");                       // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$is_args");                     // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$limit_rate");                  // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$msec");                        // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$msec");                        // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$msec");                        // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$nginx_version");               // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$nginx_version");               // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$pid");                         // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$pid");                         // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$pipe");                        // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$pipe");                        // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$protocol");                    // ngx_stream_core_module

        ALL_NGINX_VARIABLES.add("$proxy_protocol_addr");         // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$proxy_protocol_addr");         // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$proxy_protocol_port");         // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$proxy_protocol_port");         // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$query_string");                // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$realpath_root");               // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$remote_addr");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$remote_addr");                 // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$remote_port");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$remote_port");                 // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$remote_user");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request");                     // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_body");                // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_body_file");           // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_completion");          // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_filename");            // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_id");                  // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_length");              // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_length");              // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$request_method");              // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_time");                // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$request_time");                // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$request_uri");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$scheme");                      // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$sent_http_somename");                  // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$sent_trailer_somename");               // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$server_addr");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$server_addr");                 // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$server_name");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$server_port");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$server_port");                 // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$server_protocol");             // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$session_time");                // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$status");                      // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$status");                      // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$status");                      // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$tcpinfo_rtt");                 // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$tcpinfo_rttvar");              // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$tcpinfo_snd_cwnd");            // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$tcpinfo_rcv_space");           // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$time_iso8601");                // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$time_iso8601");                // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$time_iso8601");                // ngx_stream_core_module
        ALL_NGINX_VARIABLES.add("$time_local");                  // ngx_http_core_module
        ALL_NGINX_VARIABLES.add("$time_local");                  // ngx_http_log_module
        ALL_NGINX_VARIABLES.add("$time_local");                  // ngx_stream_core_module

//        ALL_NGINX_VARIABLES.add("$secure_link");                 // ngx_http_secure_link_module
//        ALL_NGINX_VARIABLES.add("$secure_link_expires");         // ngx_http_secure_link_module

//        ALL_NGINX_VARIABLES.add("$session_log_binary_id");       // ngx_http_session_log_module
//        ALL_NGINX_VARIABLES.add("$session_log_id");              // ngx_http_session_log_module

//        ALL_NGINX_VARIABLES.add("$slice_range");                 // ngx_http_slice_module

//        ALL_NGINX_VARIABLES.add("$proxy_add_x_forwarded_for");   // ngx_http_proxy_module
//        ALL_NGINX_VARIABLES.add("$proxy_host");                  // ngx_http_proxy_module
//        ALL_NGINX_VARIABLES.add("$proxy_port");                  // ngx_http_proxy_module

        ALL_NGINX_VARIABLES.add("$ssl_cipher");                  // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_cipher");                  // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_ciphers");                 // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_ciphers");                 // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_cert");             // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_cert");             // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_escaped_cert");     // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_fingerprint");      // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_fingerprint");      // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_i_dn");             // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_i_dn");             // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_i_dn_legacy");      // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_raw_cert");         // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_raw_cert");         // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_s_dn");             // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_s_dn");             // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_s_dn_legacy");      // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_serial");           // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_serial");           // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_v_end");            // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_v_end");            // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_v_remain");         // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_v_remain");         // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_v_start");          // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_v_start");          // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_verify");           // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_client_verify");           // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_curves");                  // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_curves");                  // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_early_data");              // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_preread_alpn_protocols");  // ngx_stream_ssl_preread_module
        ALL_NGINX_VARIABLES.add("$ssl_preread_protocol");        // ngx_stream_ssl_preread_module
        ALL_NGINX_VARIABLES.add("$ssl_preread_server_name");     // ngx_stream_ssl_preread_module
        ALL_NGINX_VARIABLES.add("$ssl_protocol");                // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_protocol");                // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_server_name");             // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_server_name");             // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_session_id");              // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_session_id");              // ngx_stream_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_session_reused");          // ngx_http_ssl_module
        ALL_NGINX_VARIABLES.add("$ssl_session_reused");          // ngx_stream_ssl_module

        ALL_NGINX_VARIABLES.add("$upstream_addr");               // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_addr");               // ngx_stream_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_bytes_received");     // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_bytes_received");     // ngx_stream_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_bytes_sent");         // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_bytes_sent");         // ngx_stream_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_cache_status");       // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_connect_time");       // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_connect_time");       // ngx_stream_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_cookie_");            // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_first_byte_time");    // ngx_stream_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_header_time");        // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_http_");              // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_queue_time");         // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_response_length");    // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_response_time");      // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_session_time");       // ngx_stream_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_status");             // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$upstream_trailer_");           // ngx_http_upstream_module
        ALL_NGINX_VARIABLES.add("$uri");                         // ngx_http_core_module

//        ALL_NGINX_VARIABLES.add("$uid_got");                     // ngx_http_userid_module
//        ALL_NGINX_VARIABLES.add("$uid_reset");                   // ngx_http_userid_module
//        ALL_NGINX_VARIABLES.add("$uid_set");                     // ngx_http_userid_module

//        ALL_NGINX_VARIABLES.add("$ancient_browser");             // ngx_http_browser_module
//        ALL_NGINX_VARIABLES.add("$modern_browser");              // ngx_http_browser_module
//        ALL_NGINX_VARIABLES.add("$msie");                        // ngx_http_browser_module

//        ALL_NGINX_VARIABLES.add("$connections_active");          // ngx_http_stub_status_module
//        ALL_NGINX_VARIABLES.add("$connections_reading");         // ngx_http_stub_status_module
//        ALL_NGINX_VARIABLES.add("$connections_waiting");         // ngx_http_stub_status_module
//        ALL_NGINX_VARIABLES.add("$connections_writing");         // ngx_http_stub_status_module

//        ALL_NGINX_VARIABLES.add("$date_gmt");                    // ngx_http_ssi_module
//        ALL_NGINX_VARIABLES.add("$date_local");                  // ngx_http_ssi_module

//        ALL_NGINX_VARIABLES.add("$fastcgi_path_info");           // ngx_http_fastcgi_module
//        ALL_NGINX_VARIABLES.add("$fastcgi_script_name");         // ngx_http_fastcgi_module

        ALL_NGINX_VARIABLES.add("$geoip_area_code");             // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_area_code");             // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city");                  // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city");                  // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_continent_code");   // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_continent_code");   // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_country_code");     // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_country_code");     // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_country_code3");    // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_country_code3");    // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_country_name");     // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_city_country_name");     // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_country_code");          // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_country_code");          // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_country_code3");         // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_country_code3");         // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_country_name");          // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_country_name");          // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_dma_code");              // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_dma_code");              // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_latitude");              // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_latitude");              // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_longitude");             // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_longitude");             // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_org");                   // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_org");                   // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_postal_code");           // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_postal_code");           // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_region");                // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_region");                // ngx_stream_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_region_name");           // ngx_http_geoip_module
        ALL_NGINX_VARIABLES.add("$geoip_region_name");           // ngx_stream_geoip_module

//        ALL_NGINX_VARIABLES.add("$gzip_ratio");                  // ngx_http_gzip_module

//        ALL_NGINX_VARIABLES.add("$spdy");                        // ngx_http_spdy_module
//        ALL_NGINX_VARIABLES.add("$spdy_request_priority");       // ngx_http_spdy_module
//        ALL_NGINX_VARIABLES.add("$http2");                       // ngx_http_v2_module

//        ALL_NGINX_VARIABLES.add("$invalid_referer");             // ngx_http_referer_module

//        ALL_NGINX_VARIABLES.add("$jwt_claim_");                  // ngx_http_auth_jwt_module
//        ALL_NGINX_VARIABLES.add("$jwt_header_");                 // ngx_http_auth_jwt_module

//        ALL_NGINX_VARIABLES.add("$memcached_key");               // ngx_http_memcached_module

//        ALL_NGINX_VARIABLES.add("$realip_remote_addr");          // ngx_http_realip_module
//        ALL_NGINX_VARIABLES.add("$realip_remote_addr");          // ngx_stream_realip_module
//        ALL_NGINX_VARIABLES.add("$realip_remote_port");          // ngx_http_realip_module
//        ALL_NGINX_VARIABLES.add("$realip_remote_port");          // ngx_stream_realip_module

    }

    @Test
    public void ensureAllFieldsAreHandled() {
        StringBuilder sb = new StringBuilder(4096);
        ALL_NGINX_VARIABLES.forEach(v -> sb.append('"').append(v).append("\" # "));

        List<String> allPossible =
            DissectorTester.create()
                .verbose()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, sb.toString()))
                .getPossible();

        allPossible.forEach(p -> assertFalse(p, p.startsWith("UNKNOWN_NGINX_VARIABLE")));

    }

}
