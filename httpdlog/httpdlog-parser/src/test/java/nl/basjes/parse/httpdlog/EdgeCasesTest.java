/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
            .expectNull("HTTP.USERAGENT:request.user-agent")
            .expectNull("HTTP.HEADER:request.header.host")
            .expectNull("HTTP.HEADER:request.header.content-length")
            .expectNull("HTTP.URI:request.referer")

            // This thing should be unparsable
            .expect("HTTP.FIRSTLINE:request.firstline",             "\u0016\u0003\u0001")
            .expectAbsentString("HTTP.METHOD:request.firstline.method")
            .expectAbsentString("HTTP.URI:request.firstline.uri")
            .expectAbsentString("HTTP.PROTOCOL:request.firstline.protocol")

            .checkExpectations();
    }

    void checkBadUri(String logLine, String expectedUri) {
        String logFormat = "common";
        assertDoesNotThrow(() -> {
            DissectorTester.create()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
                .withInput(logLine)
                .expect("HTTP.URI:request.firstline.uri", expectedUri)
                .expectAbsentString("HTTP.HOST:request.firstline.uri.host")
                .expectAbsentString("HTTP.PATH:request.firstline.uri.path")
                .expectAbsentString("HTTP.PORT:request.firstline.uri.port")
                .expectAbsentString("HTTP.PROTOCOL:request.firstline.uri.protocol")
                .expectAbsentString("HTTP.QUERYSTRING:request.firstline.uri.query")
                .expectAbsentString("HTTP.REF:request.firstline.uri.ref")
                .expectAbsentString("HTTP.USERINFO:request.firstline.uri.userinfo")
                .printAllPossibleValues();
        });
    }

    @Test
    void testBadUri() {
        checkBadUri(
            "10.10.10.10 - - [28/Feb/2023:16:48:52 +0800] \"GET :@bxss.me::80/rpb.png HTTP/1.1\" 400 1160",
            ":@bxss.me::80/rpb.png");

        checkBadUri(
            "10.10.10.10 - - [28/Feb/2023:16:48:51 +0800] \"GET @bxss.me::80/rpb.png HTTP/1.1\" 400 1160",
            "@bxss.me::80/rpb.png");

        checkBadUri(
            "10.10.10.10 - - [28/Feb/2023:16:48:51 +0800] \"GET :@bxss.me/rpb.png HTTP/1.1\" 400 1160",
            ":@bxss.me/rpb.png");
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
