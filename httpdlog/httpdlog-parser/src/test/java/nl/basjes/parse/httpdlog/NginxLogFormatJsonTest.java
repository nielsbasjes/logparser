/*
 * Apache HTTPD logparsing made easy
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

class NginxLogFormatJsonTest {

    @Test
    void testJsonFormat() {

        String logFormat =  "{ " +
            "\"message\":\"$request_uri\"," +
            "\"client\": \"$remote_addr\"," +
            "\"auth\": \"$remote_user\", " +
            "\"bytes\": \"$body_bytes_sent\", " +
            "\"time_in_sec\": \"$request_time\", " +
            "\"response\": \"$status\", " +
            "\"verb\":\"$request_method\"," +
            "\"referrer\": \"$http_referer\", " +
            "\"site\":\"$http_host\"," +
            "\"httpversion\":\"$server_protocol\"," +
            "\"logtype\":\"accesslog\"," +
            "\"agent\": \"$http_user_agent\" }";

        String logLine = "{ " +
            "\"message\":\"/one/two/tool.git/info/refs?service=upload-pack\"," +
            "\"client\": \"10.11.12.13\"," +
            "\"auth\": \"-\", " +
            "\"bytes\": \"178\", " +
            "\"time_in_sec\": \"0.000\", " +
            "\"response\": \"301\", " +
            "\"verb\":\"GET\"," +
            "\"referrer\": \"-\", " +
            "\"site\":\"some.thing.example.com\"," +
            "\"httpversion\":\"HTTP/1.1\"," +
            "\"logtype\":\"accesslog\"," +
            "\"agent\": \"git/1.9.5.msysgit.0\" }";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)

            .printPossible()
            .printAllPossibleValues();
    }

}
