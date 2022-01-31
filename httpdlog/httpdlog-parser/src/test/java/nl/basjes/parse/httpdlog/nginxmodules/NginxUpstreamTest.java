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

package nl.basjes.parse.httpdlog.nginxmodules;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// CHECKSTYLE.OFF: LineLength
class NginxUpstreamTest {

    private static final Logger LOG = LoggerFactory.getLogger(NginxUpstreamTest.class);

    private static class SingleFieldTestcase {
        final String logformat;
        final String logline;
        final String fieldName;
        final String expectedValue;

        SingleFieldTestcase(String logformat, String logline, String fieldName, String expectedValue) {
            this.logformat = logformat;
            this.logline = logline;
            this.fieldName = fieldName;
            this.expectedValue = expectedValue;
        }
    }

    @Test
    void testBasicLogFormat() {
        // From: http://articles.slicehost.com/2010/8/27/customizing-nginx-web-logs
        String logFormat = "\"$upstream_addr\" \"$upstream_bytes_received\"";
        String logLine = "\"192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock : 192.168.10.1:80, 192.168.10.2:80\" \"1, 2, 3 : 4, 5\"";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)

            .expect("UPSTREAM_ADDR_LIST:nginxmodule.upstream.addr",
                "192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock : 192.168.10.1:80, 192.168.10.2:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.0.value",                  "192.168.1.1:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.0.redirected",             "192.168.1.1:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.1.value",                  "192.168.1.2:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.1.redirected",             "192.168.1.2:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.2.value",                  "unix:/tmp/sock")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.2.redirected",             "192.168.10.1:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.3.value",                  "192.168.10.2:80")
            .expect("UPSTREAM_ADDR:nginxmodule.upstream.addr.3.redirected",             "192.168.10.2:80")
            .expectAbsentString("UPSTREAM_ADDR:nginxmodule.upstream.addr.4.value")
            .expectAbsentString("UPSTREAM_ADDR:nginxmodule.upstream.addr.4.redirected")

            .expect("UPSTREAM_BYTES_LIST:nginxmodule.upstream.bytes.received",          "1, 2, 3 : 4, 5")
            .expect("BYTES:nginxmodule.upstream.bytes.received.0.value",                "1")
            .expect("BYTES:nginxmodule.upstream.bytes.received.0.redirected",           "1")
            .expect("BYTES:nginxmodule.upstream.bytes.received.1.value",                "2")
            .expect("BYTES:nginxmodule.upstream.bytes.received.1.redirected",           "2")
            .expect("BYTES:nginxmodule.upstream.bytes.received.2.value",                "3")
            .expect("BYTES:nginxmodule.upstream.bytes.received.2.redirected",           "4")
            .expect("BYTES:nginxmodule.upstream.bytes.received.3.value",                "5")
            .expect("BYTES:nginxmodule.upstream.bytes.received.3.redirected",           "5")
            .expectAbsentString("BYTES:nginxmodule.upstream.bytes.received.4.value")
            .expectAbsentString("BYTES:nginxmodule.upstream.bytes.received.4.redirected")


            .checkExpectations()

            .printPossible()
            .printAllPossibleValues();
    }

    @Test
    void testFullLine() {
        String logFormat = "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\" \"$http_x_forwarded_for\" $request_time $upstream_response_time $pipe";

        String logLine   = "10.77.150.123 - - [15/Dec/2018:19:27:57 -0500] \"GET /25.chunk.js HTTP/1.1\" 200 84210 \"https://api.demo.com/\" \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36\" \"-\" 0.002 0.002 .";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)
            .expect("SECOND_MILLIS:nginxmodule.upstream.response.time.0.value",      "0.002")
            .expect("SECOND_MILLIS:nginxmodule.upstream.response.time.0.redirected", "0.002")
            .expect("MICROSECONDS:nginxmodule.upstream.response.time.0.value",      "2000")
            .expect("MICROSECONDS:nginxmodule.upstream.response.time.0.redirected", "2000")

            .checkExpectations()

//            .printPossible();
            .printAllPossibleValues();

    }

    @Test
    public void validateAllFields() {
        List<SingleFieldTestcase> fieldsTests = new ArrayList<>();

        String addrList = "192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock : 192.168.10.1:80, 192.168.10.2:80";
        final String numList = "1, 2, 3 : 4, 5";
        final String timeList = "1.001, 2.002, 3.003 : 4.004, 5.005";
        final String statusList = "111, 222, 333 : 444, 555";

        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.0.value", "192.168.1.1:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.0.redirected", "192.168.1.1:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.1.value", "192.168.1.2:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.1.redirected", "192.168.1.2:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.2.value", "unix:/tmp/sock"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.2.redirected", "192.168.10.1:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.3.value", "192.168.10.2:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.3.redirected", "192.168.10.2:80"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_addr", addrList, "UPSTREAM_ADDR:nginxmodule.upstream.addr.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.0.value", "1"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.0.redirected", "1"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.1.value", "2"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.1.redirected", "2"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.2.value", "3"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.2.redirected", "4"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.3.value", "5"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.3.redirected", "5"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_received", numList, "BYTES:nginxmodule.upstream.bytes.received.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.0.value", "1"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.0.redirected", "1"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.1.value", "2"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.1.redirected", "2"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.2.value", "3"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.2.redirected", "4"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.3.value", "5"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.3.redirected", "5"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_bytes_sent", numList, "BYTES:nginxmodule.upstream.bytes.sent.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_cache_status", "STALE", "UPSTREAM_CACHE_STATUS:nginxmodule.upstream.cache.status", "STALE"));

        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.0.value", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.0.redirected", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.1.value", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.1.redirected", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.2.value", "3.003"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.2.redirected", "4.004"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.3.value", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.3.redirected", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_connect_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.connect.time.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_cookie_mycookie", "MyValue", "HTTP.COOKIE:nginxmodule.upstream.response.cookies.mycookie", "MyValue"));

        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.0.value", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.0.redirected", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.1.value", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.1.redirected", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.2.value", "3.003"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.2.redirected", "4.004"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.3.value", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.3.redirected", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_header_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.header.time.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_http_myheader", "MyValue", "HTTP.HEADER:nginxmodule.upstream.header.myheader", "MyValue"));

        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.0.value", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.0.redirected", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.1.value", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.1.redirected", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.2.value", "3.003"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.2.redirected", "4.004"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.3.value", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.3.redirected", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_queue_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.queue.time.4.redirected", null));


        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.0.value", "1"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.0.redirected", "1"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.1.value", "2"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.1.redirected", "2"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.2.value", "3"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.2.redirected", "4"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.3.value", "5"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.3.redirected", "5"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_length", numList, "BYTES:nginxmodule.upstream.response.length.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.0.value", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.0.redirected", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.1.value", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.1.redirected", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.2.value", "3.003"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.2.redirected", "4.004"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.3.value", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.3.redirected", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_response_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.response.time.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.0.value", "111"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.0.redirected", "111"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.1.value", "222"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.1.redirected", "222"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.2.value", "333"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.2.redirected", "444"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.3.value", "555"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.3.redirected", "555"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_status", statusList, "UPSTREAM_STATUS:nginxmodule.upstream.status.4.redirected", null));


        fieldsTests.add(new SingleFieldTestcase("$upstream_trailer_mytrailer", "MyValue", "HTTP.TRAILER:nginxmodule.upstream.trailer.mytrailer", "MyValue"));

        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.0.value", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.0.redirected", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.1.value", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.1.redirected", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.2.value", "3.003"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.2.redirected", "4.004"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.3.value", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.3.redirected", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_first_byte_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.first_byte.time.4.redirected", null));

        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.0.value", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.0.redirected", "1.001"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.1.value", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.1.redirected", "2.002"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.2.value", "3.003"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.2.redirected", "4.004"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.3.value", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.3.redirected", "5.005"));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.4.value", null));
        fieldsTests.add(new SingleFieldTestcase("$upstream_session_time", timeList, "SECOND_MILLIS:nginxmodule.upstream.session.time.4.redirected", null));

        for (SingleFieldTestcase testCase: fieldsTests) {
            DissectorTester tester =
                DissectorTester.create()
                .printSeparator()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, testCase.logformat))
                .withInput(testCase.logline);
            if (testCase.expectedValue == null) {
                tester.expectAbsentString(testCase.fieldName);
            } else {
                tester.expect(testCase.fieldName, testCase.expectedValue);
            }
            tester
                .checkExpectations();
        }

    }




}
