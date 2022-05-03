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

class EdgeCasesTest {
    @Test
    void testInvalidFirstLine() {
        String logFormat = "%a %{Host}i %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\" %{Content-length}i %P %A";
        String testLine = "1.2.3.4 - - [03/Apr/2017:03:27:28 -0600] \"\\x16\\x03\\x01\" 404 419 \"-\" \"-\" - 115052 5.6.7.8";
        DissectorTester.create()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(testLine)

            .expect("IP:connection.client.ip",                      "1.2.3.4")
            .expect("IP:connection.server.ip",                      "5.6.7.8")
            .expect("TIME.EPOCH:request.receive.time.last.epoch",   1491211648000L)
            .expect("STRING:connection.client.user",                (String)null) // Is present AND is null

            .expect("TIME.STAMP:request.receive.time.last",         "03/Apr/2017:03:27:28 -0600")
            .expect("TIME.DATE:request.receive.time.last.date",     "2017-04-03")
            .expect("TIME.TIME:request.receive.time.last.time",     "03:27:28")

            .expect("NUMBER:connection.server.child.processid",     "115052")
            .expect("BYTES:response.bytes",                         "419")
            .expect("STRING:request.status.last",                   "404")
            .expect("HTTP.USERAGENT:request.user-agent",            (String)null) // Is present AND is null
            .expect("HTTP.HEADER:request.header.host",              (String)null) // Is present AND is null
            .expect("HTTP.HEADER:request.header.content-length",    (String)null) // Is present AND is null
            .expect("HTTP.URI:request.referer",                     (String)null) // Is present AND is null

            // This thing should be unparsable
            .expect("HTTP.FIRSTLINE:request.firstline",             "\u0016\u0003\u0001")
            .expectAbsentString("HTTP.METHOD:request.firstline.method")
            .expectAbsentString("HTTP.URI:request.firstline.uri")
            .expectAbsentString("HTTP.PROTOCOL:request.firstline.protocol")

            .checkExpectations();
    }


    @Test
    void checkErrorLogging(){
        HttpdLogFormatDissector dissector = new HttpdLogFormatDissector();
        dissector
            // Apache format
            .addLogFormat("%t")
            .addMultipleLogFormats("%a\n%b\n%c")
            .addLogFormat("%b") // Already have this one
            // Nginx format
            .addLogFormat("$remote_addr")
            .addMultipleLogFormats("$time_local\n$body_bytes_sent\n$status")
            .addLogFormat("$body_bytes_sent") // Already have this one
            // Unable to determine
            .addLogFormat("blup");
    }

}
