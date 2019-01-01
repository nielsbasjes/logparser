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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;


// CHECKSTYLE.OFF: LineLength
public class JsonLogFormatTest {
    // As seen here: http://mail-archives.apache.org/mod_mbox/kafka-users/201408.mbox/%3C1407447350019.7022@roomkey.com%3E
    // LogFormat "{\"@timestamp\":\"%{%Y-%m-%dT%H:%M:%S%z}t\",\"mod_proxy\":{\"x-forwarded-for\":\"%{X-Forwarded-For}i\"},\"mod_headers\":{\"referer\":\"%{Referer}i\",\"user-agent\":\"%{User-Agent}i\",\"host\":\"%{Host}i\"},\"mod_log\":{\"server_name\":\"%V\",\"remote_logname\":\"%l\",\"remote_user\":\"%u\",\"first_request\":\"%r\",\"last_request_status\":\"%>s\",\"response_size_bytes\":%B,\"duration_usec\":%D,\"@version\":1 }" logstash_json

    private static final String LOGFORMAT = "{\"@timestamp\":\"%{%Y-%m-%dT%H:%M:%S %z}t\",\"mod_proxy\":{\"x-forwarded-for\":\"%{X-Forwarded-For}i\"},\"mod_headers\":{\"referer\":\"%{Referer}i\",\"user-agent\":\"%{User-Agent}i\",\"host\":\"%{Host}i\"},\"mod_log\":{\"server_name\":\"%V\",\"remote_logname\":\"%l\",\"remote_user\":\"%u\",\"first_request\":\"%r\",\"last_request_status\":\"%>s\",\"response_size_bytes\":%B,\"duration_usec\":%D,\"@version\":1 }";

    private static final String[] LOGLINES = {
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"-\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET / HTTP/1.1\",\"last_request_status\":\"403\",\"response_size_bytes\":4897,\"duration_usec\":909,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /noindex/css/bootstrap.min.css HTTP/1.1\",\"last_request_status\":\"200\",\"response_size_bytes\":19341,\"duration_usec\":657,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /noindex/css/open-sans.css HTTP/1.1\",\"last_request_status\":\"200\",\"response_size_bytes\":5081,\"duration_usec\":680,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /images/apache_pb.gif HTTP/1.1\",\"last_request_status\":\"200\",\"response_size_bytes\":2326,\"duration_usec\":728,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /images/poweredby.png HTTP/1.1\",\"last_request_status\":\"200\",\"response_size_bytes\":3956,\"duration_usec\":498,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/noindex/css/open-sans.css\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /noindex/css/fonts/Light/OpenSans-Light.woff HTTP/1.1\",\"last_request_status\":\"404\",\"response_size_bytes\":241,\"duration_usec\":147,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/noindex/css/open-sans.css\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /noindex/css/fonts/Bold/OpenSans-Bold.woff HTTP/1.1\",\"last_request_status\":\"404\",\"response_size_bytes\":239,\"duration_usec\":536,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/noindex/css/open-sans.css\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /noindex/css/fonts/Bold/OpenSans-Bold.ttf HTTP/1.1\",\"last_request_status\":\"404\",\"response_size_bytes\":238,\"duration_usec\":347,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/noindex/css/open-sans.css\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /noindex/css/fonts/Light/OpenSans-Light.ttf HTTP/1.1\",\"last_request_status\":\"404\",\"response_size_bytes\":240,\"duration_usec\":268,\"@version\":1 }",
        "{\"@timestamp\":\"2015-11-25T15:24:45 +0100\",\"mod_proxy\":{\"x-forwarded-for\":\"-\"},\"mod_headers\":{\"referer\":\"http://localhost/\",\"user-agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\"host\":\"localhost\"},\"mod_log\":{\"server_name\":\"localhost\",\"remote_logname\":\"-\",\"remote_user\":\"-\",\"first_request\":\"GET /favicon.ico HTTP/1.1\",\"last_request_status\":\"404\",\"response_size_bytes\":209,\"duration_usec\":342,\"@version\":1 }",
    };

    @Test
    public void testBasicParsing() {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOGFORMAT);

        DissectorTester tester = DissectorTester.create()
            .withParser(parser)
            .verbose();

        for (String logline: LOGLINES){
            tester.withInput(logline);
        }

        tester.expectValuePresent("TIME.LOCALIZEDSTRING:request.receive.time")
              .expectValuePresent("STRING:connection.server.name")
              .expectValuePresent("NUMBER:connection.client.logname")
              .expectValuePresent("STRING:connection.client.user")
              .expectValuePresent("HTTP.HEADER:request.header.x-forwarded-for")
              .expectValuePresent("HTTP.URI:request.referer")
              .expectValuePresent("HTTP.USERAGENT:request.user-agent")
              .expectValuePresent("HTTP.HEADER:request.header.host")
              .expectValuePresent("HTTP.FIRSTLINE:request.firstline")
              .expectValuePresent("HTTP.METHOD:request.firstline.method")
              .expectValuePresent("HTTP.URI:request.firstline.uri")
              .expectValuePresent("HTTP.PROTOCOL:request.firstline.protocol")
              .expectValuePresent("HTTP.PROTOCOL.VERSION:request.firstline.protocol.version")
              .expectValuePresent("STRING:request.status.last")
              .expectValuePresent("BYTES:response.body.bytes")
              .expectValuePresent("MICROSECONDS:response.server.processing.time")
              .expectValuePresent("HTTP.QUERYSTRING:request.firstline.uri.query")
              .expectValuePresent("HTTP.PATH:request.firstline.uri.path")
              .expectValuePresent("HTTP.REF:request.firstline.uri.ref");

        for (String path: parser.getPossiblePaths()){
            tester.expectPossible(path);
        }

        tester.checkExpectations();
    }

}
