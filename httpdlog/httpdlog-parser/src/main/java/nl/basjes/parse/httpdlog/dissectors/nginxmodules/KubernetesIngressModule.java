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
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

//
// https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/log-format/
//
public class KubernetesIngressModule implements NginxModule {

    private static final String PREFIX = "nginxmodule.kubernetes";

    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // $the_real_ip
        // the source IP address of the client
        parsers.add(new TokenParser("$the_real_ip", PREFIX + ".the_real_ip", "IP", Casts.STRING_ONLY, FORMAT_STRING));

        // $proxy_upstream_name
        // name of the upstream. The format is upstream-<namespace>-<service name>-<service port>
        parsers.add(new TokenParser("$proxy_upstream_name", PREFIX + ".proxy_upstream_name", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $req_id
        // the randomly generated ID of the request
        parsers.add(new TokenParser("$req_id", PREFIX + ".req_id", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $namespace
        // namespace of the ingress
        parsers.add(new TokenParser("$namespace", PREFIX + ".namespace", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $ingress_name
        // name of the ingress
        parsers.add(new TokenParser("$ingress_name", PREFIX + ".ingress_name", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $service_name
        // name of the service
        parsers.add(new TokenParser("$service_name", PREFIX + ".service.name", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $service_port
        // port of the service
        parsers.add(new TokenParser("$service_port", PREFIX + ".service.port", "PORT", Casts.STRING_ONLY, FORMAT_STRING));


        return parsers;
    }
}
