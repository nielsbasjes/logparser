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

class TestHttpFirstLineDissector {
    @Test
    void testNormal() {
        DissectorTester.create()
            .withDissector(new HttpFirstLineDissector())
            .withDissector(new HttpFirstLineProtocolDissector())
            .withInput("GET /index.html HTTP/1.1")
            .expect("HTTP.METHOD:method",                     "GET")
            .expect("HTTP.URI:uri",                           "/index.html")
            .expect("HTTP.PROTOCOL:protocol",                 "HTTP")
            .expect("HTTP.PROTOCOL.VERSION:protocol.version", "1.1")
            .checkExpectations();
    }

    @Test
    void testChoppedFirstLine() {
        DissectorTester.create()
            .withDissector(new HttpFirstLineDissector())
            .withDissector(new HttpFirstLineProtocolDissector())
            .withInput("GET /index.html HTT")
            .expect("HTTP.METHOD:method",                     "GET")
            .expect("HTTP.URI:uri",                           "/index.html HTT")
            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.PROTOCOL.VERSION:protocol.version")
            .checkExpectations();
    }

    @Test
    void testInvalidFirstLine() {
        DissectorTester.create()
            .withDissector(new HttpFirstLineDissector())
            .withInput("\\x16\\x03\\x01")
            .expectAbsentString("HTTP.METHOD:method")
            .expectAbsentString("HTTP.URI:uri")
            .checkExpectations();
    }

    @Test
    void testStrangeCommandVersionControl() {
        DissectorTester.create()
            .withDissector(new HttpFirstLineDissector())
            .withDissector(new HttpFirstLineProtocolDissector())
            .withInput("VERSION-CONTROL /index.html HTTP/1.1")
            .expect("HTTP.METHOD:method",                     "VERSION-CONTROL")
            .expect("HTTP.URI:uri",                           "/index.html")
            .expect("HTTP.PROTOCOL:protocol",                 "HTTP")
            .expect("HTTP.PROTOCOL.VERSION:protocol.version", "1.1")
            .checkExpectations();
    }

    @Test
    void testProtocol() {
        DissectorTester.create()
            .withDissector("protocol", new HttpFirstLineProtocolDissector())
            .withInput("FOO/1.2")
            .expect("HTTP.PROTOCOL:protocol",                 "FOO")
            .expect("HTTP.PROTOCOL.VERSION:protocol.version", "1.2")
            .checkExpectations();
    }

    @Test
    void testChoppedProtocol() {
        DissectorTester.create()
            .withDissector("protocol", new HttpFirstLineProtocolDissector())
            .withInput("FOO")
            .expect("HTTP.PROTOCOL:protocol",                 (String)null)
            .expect("HTTP.PROTOCOL.VERSION:protocol.version", (String)null)
            .checkExpectations();
    }

    @Test
    void testEmptyProtocol1() {
        DissectorTester.create()
            .withDissector("protocol", new HttpFirstLineProtocolDissector())
            .withInput("")
            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.PROTOCOL.VERSION:protocol.version")
            .checkExpectations();
    }

    @Test
    void testEmptyProtocol2() {
        DissectorTester.create()
            .withDissector("protocol", new HttpFirstLineProtocolDissector())
            .withInput("-")
            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.PROTOCOL.VERSION:protocol.version")
            .checkExpectations();
    }

}
