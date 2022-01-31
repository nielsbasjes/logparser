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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.test.DissectorTester;
import org.junit.jupiter.api.Test;

class TestQueryStringDissector {

    @Test
    void testQueryString() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())
            .withDissector(new QueryStringFieldDissector())

            .withInput("/some/thing/else/index.html&aap=1&noot=&mies&")

            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=1&noot=&mies&")
            .expect("STRING:query.aap",          "1")           // Present with value
            .expect("STRING:query.noot",         "")            // Present without value
            .expect("STRING:query.mies",         "")            // Present without value
            .expectAbsentString("STRING:query.wim")  // NOT Present

            .checkExpectations();
    }



}
