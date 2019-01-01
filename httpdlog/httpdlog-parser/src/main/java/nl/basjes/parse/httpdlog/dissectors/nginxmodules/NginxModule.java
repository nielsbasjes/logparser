/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.Collections;
import java.util.List;

public interface NginxModule {
    List<TokenParser> getTokenParsers();

    default List<Dissector> getDissectors() {
        return Collections.emptyList(); // By default no extra dissectors
    }
}
