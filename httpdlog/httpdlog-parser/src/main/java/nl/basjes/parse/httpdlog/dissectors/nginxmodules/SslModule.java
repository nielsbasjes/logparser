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

import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

// Implement the tokens described here:
// https://nginx.org/en/docs/http/ngx_http_upstream_module.html#variables
// https://nginx.org/en/docs/stream/ngx_stream_upstream_module.html#variables
// https://nginx.org/en/docs/stream/ngx_stream_ssl_preread_module.html#variables
public class SslModule implements NginxModule {

    private static final String PREFIX = "nginxmodule.ssl";

    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // $ssl_cipher
        // returns the string of ciphers used for an established SSL connection;
        parsers.add(new TokenParser("$ssl_cipher",
                    PREFIX + ".cipher", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_ciphers
        // returns the list of ciphers supported by the client (1.11.7).
        // Known ciphers are listed by names, unknown are shown in hexadecimal,
        // for example: AES128-SHA:AES256-SHA:0x00ff
        parsers.add(new TokenParser("$ssl_ciphers",
                    PREFIX + ".client.ciphers", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_escaped_cert
        // returns the client certificate in the PEM format (urlencoded) for an established SSL connection (1.13.5);
        parsers.add(new TokenParser("$ssl_client_escaped_cert",
                    PREFIX + ".client.cert", "PEM_CERT_URLENCODED",
                    STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $ssl_client_cert --> IS DEPRECATED USE $ssl_client_escaped_cert
        // returns the client certificate in the PEM format for an established SSL connection,
        // with each line except the first prepended with the tab character;
        // this is intended for the use in the proxy_set_header directive;
        // The variable is deprecated, the $ssl_client_escaped_cert variable should be used instead.
        parsers.add(new TokenParser("$ssl_client_cert",
                    PREFIX + ".client.cert", "PEM_CERT",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_raw_cert
        // returns the client certificate in the PEM format for an established SSL connection;
        parsers.add(new TokenParser("$ssl_client_raw_cert",
                    PREFIX + ".client.cert", "PEM_CERT_RAW",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_fingerprint
        // returns the SHA1 fingerprint of the client certificate for an established SSL connection (1.7.1);
        parsers.add(new TokenParser("$ssl_client_fingerprint",
                    PREFIX + ".client.cert.fingerprint", "SHA1",
                    STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $ssl_client_i_dn
        // returns the “issuer DN” string of the client certificate for an established SSL connection according to RFC 2253 (1.11.6);
        parsers.add(new TokenParser("$ssl_client_i_dn",
                    PREFIX + ".client.cert.issuer_dn", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_i_dn_legacy
        // returns the “issuer DN” string of the client certificate for an established SSL connection;
        // Prior to version 1.11.6, the variable name was $ssl_client_i_dn.
        parsers.add(new TokenParser("$ssl_client_i_dn_legacy",
                    PREFIX + ".client.cert.issuer_dn.legacy", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_s_dn
        // returns the “subject DN” string of the client certificate for an established SSL connection according to RFC 2253 (1.11.6);
        parsers.add(new TokenParser("$ssl_client_s_dn",
                    PREFIX + ".client.cert.subject_dn", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_s_dn_legacy
        // returns the “subject DN” string of the client certificate for an established SSL connection;
        // Prior to version 1.11.6, the variable name was $ssl_client_s_dn.
        parsers.add(new TokenParser("$ssl_client_s_dn_legacy",
                    PREFIX + ".client.cert.subject_dn.legacy", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_serial
        // returns the serial number of the client certificate for an established SSL connection;
        parsers.add(new TokenParser("$ssl_client_serial",
                    PREFIX + ".client.cert.serial", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_v_end
        // returns the end date of the client certificate (1.11.7);
        parsers.add(new TokenParser("$ssl_client_v_end",
                    PREFIX + ".client.cert.end_date", "STRING", // TODO: What is the format of this date?
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_v_remain
        // returns the number of days until the client certificate expires (1.11.7);
        parsers.add(new TokenParser("$ssl_client_v_remain",
                    PREFIX + ".client.cert.remain_days", "STRING", // TODO: What is the format of this?
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_v_start
        // returns the start date of the client certificate (1.11.7);
        parsers.add(new TokenParser("$ssl_client_v_start",
                    PREFIX + ".client.cert.start_date", "STRING", // TODO: What is the format of this date?
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_client_verify
        // returns the result of client certificate verification:
        //      “SUCCESS”, “FAILED:reason”, and “NONE” if a certificate was not present;
        // Prior to version 1.11.7, the “FAILED” result did not contain the reason string.
        parsers.add(new TokenParser("$ssl_client_verify",
                    PREFIX + ".client.cert.verify", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_curves
        // returns the list of curves supported by the client (1.11.7).
        // Known curves are listed by names, unknown are shown in hexadecimal,
        // for example: 0x001d:prime256v1:secp521r1:secp384r1
        // The variable is supported only when using OpenSSL version 1.0.2 or higher.
        // With older versions, the variable value will be an empty string. The variable is available only for new sessions.
        parsers.add(new TokenParser("$ssl_curves",
                    PREFIX + ".client.curves", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_early_data
        // returns “1” if TLS 1.3 early data is used and the handshake is not complete, otherwise “” (1.15.3).
        parsers.add(new TokenParser("$ssl_early_data",
                    PREFIX + ".early_data", "STRING",
                    STRING_ONLY, "1?"));

        // $ssl_protocol
        // returns the protocol of an established SSL connection;
        parsers.add(new TokenParser("$ssl_protocol",
                    PREFIX + ".protocol", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_server_name
        // returns the server name requested through SNI (1.7.0);
        parsers.add(new TokenParser("$ssl_server_name",
                    PREFIX + ".server_name", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_session_id
        // returns the session identifier of an established SSL connection;
        parsers.add(new TokenParser("$ssl_session_id",
                    PREFIX + ".session.id", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_session_reused
        // returns “r” if an SSL session was reused, or “.” otherwise (1.5.11).
        parsers.add(new TokenParser("$ssl_session_reused",
                    PREFIX + ".session.reused", "STRING",
                    STRING_ONLY, "(r|.)"));

        // $ssl_preread_protocol
        // the highest SSL protocol version supported by the client (1.15.2)
        parsers.add(new TokenParser("$ssl_preread_protocol",
                    PREFIX + ".preread.protocol", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_preread_server_name
        // server name requested through SNI
        parsers.add(new TokenParser("$ssl_preread_server_name",
                    PREFIX + ".preread.server_name", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        // $ssl_preread_alpn_protocols
        // list of protocols advertised by the client through ALPN (1.13.10).
        // The values are separated by commas.
        parsers.add(new TokenParser("$ssl_preread_alpn_protocols",
                    PREFIX + ".preread.alpn_protocols", "STRING",
                    STRING_ONLY, FORMAT_STRING));

        return parsers;
    }
}
