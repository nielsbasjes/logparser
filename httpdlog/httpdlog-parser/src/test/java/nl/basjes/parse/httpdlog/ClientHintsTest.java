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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.jupiter.api.Test;


class ClientHintsTest {
    @Test
    void testClientHintsWithEscapedDoubleQuotes() {
        String logFormat = "%a %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Sec-CH-UA}i\" \"%{Sec-CH-UA-Arch}i\" \"%{Sec-CH-UA-Bitness}i\" \"%{Sec-CH-UA-Full-Version}i\" \"%{Sec-CH-UA-Full-Version-List}i\" \"%{Sec-CH-UA-Mobile}i\" \"%{Sec-CH-UA-Model}i\" \"%{Sec-CH-UA-Platform}i\" \"%{Sec-CH-UA-Platform-Version}i\" \"%{Sec-CH-UA-WoW64}i\" %V";

        // 10.20.30.40 - - [03/May/2022:14:08:37 +0200] "GET / HTTP/1.1" 200 16165 "https://github.com/nielsbasjes/yauaa" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36" "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"" "\"x86\"" "\"64\"" "\"100.0.4896.88\"" "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.88\", \"Google Chrome\";v=\"100.0.4896.88\"" "?0" "\"\"" "\"macOS\"" "\"12.2.1\"" "?0" try.yauaa.basjes.nl

        String testLine = "10.20.30.40 - - [03/May/2022:14:08:37 +0200] \"GET / HTTP/1.1\" 200 16165 \"https://github.com/nielsbasjes/yauaa\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36\" \"\\\" Not A;Brand\\\";v=\\\"99\\\", \\\"Chromium\\\";v=\\\"100\\\", \\\"Google Chrome\\\";v=\\\"100\\\"\" \"\\\"x86\\\"\" \"\\\"64\\\"\" \"\\\"100.0.4896.88\\\"\" \"\\\" Not A;Brand\\\";v=\\\"99.0.0.0\\\", \\\"Chromium\\\";v=\\\"100.0.4896.88\\\", \\\"Google Chrome\\\";v=\\\"100.0.4896.88\\\"\" \"?0\" \"\\\"\\\"\" \"\\\"macOS\\\"\" \"\\\"12.2.1\\\"\" \"?0\" try.yauaa.basjes.nl";
        DissectorTester.create()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(testLine)
//            .getPossible().forEach(System.out::println);

            .expect("HTTP.USERAGENT:request.user-agent",                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36")
            .expect("HTTP.HEADER:request.header.sec-ch-ua",                     "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-arch",                "\"x86\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-bitness",             "\"64\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-full-version",        "\"100.0.4896.88\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-full-version-list",   "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.88\", \"Google Chrome\";v=\"100.0.4896.88\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-mobile",              "?0")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-model",               "\"\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-platform",            "\"macOS\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-platform-version",    "\"12.2.1\"")
            .expect("HTTP.HEADER:request.header.sec-ch-ua-wow64",               "?0")

            .checkExpectations();
    }

}
