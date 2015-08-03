/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultiLineHttpdLogParserTest {

    // ------------------------------------------

    public static class TestRecord {
        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings("UnusedDeclaration")
        @Field({
            "IP:connection.client.host",
            "TIME.STAMP:request.receive.time",
            "TIME.SECOND:request.receive.time.second",
            "STRING:request.status.last",
            "BYTES:response.body.bytesclf",
            "HTTP.URI:request.firstline.uri",
            "HTTP.URI:request.referer",
            "HTTP.USERAGENT:request.user-agent"})
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public Map<String, String> getResults() {
            return results;
        }
    }

    // ------------------------------------------

    /**
     * Test of initialize method, of class ApacheHttpdLogParser.
     */
    @Test
    public void fullTest1() throws Exception {

        String logFormat = logFormat1 + '\n'
                         + '\n'
                         + logFormat2 + '\n'
                         + '\n';

        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logFormat);

        validateLine1(parser);
        validateLine1(parser);
        validateLine2(parser);
        validateLine2(parser);
        validateLine1(parser);
        validateLine1(parser);
        validateLine2(parser);
        validateLine2(parser);
        validateLine1(parser);
        validateLine1(parser);
        validateLine2(parser);
        validateLine2(parser);
    }

    private final String logFormat1 = "%h %t \"%r\" %>s %b \"%{Referer}i\"";
    private final String line1 = "127.0.0.1 [31/Dec/2012:23:49:41 +0100] "
            + "\"GET /foo HTTP/1.1\" 200 "
            + "1213 \"http://localhost/index.php?mies=wim\"";

    private void validateLine1 (Parser parser) throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        TestRecord record = new TestRecord();
        parser.parse(record, line1);
        Map<String, String> results = record.getResults();

        assertEquals("127.0.0.1", results.get("IP:connection.client.host"));
        assertEquals("[31/Dec/2012:23:49:41 +0100]", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("/foo", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));
        assertEquals("1213", results.get("BYTES:response.body.bytesclf"));
        assertEquals("http://localhost/index.php?mies=wim", results.get("HTTP.URI:request.referer"));
        assertEquals(null, results.get("HTTP.USERAGENT:request.user-agent"));
    }

    private final String logFormat2 = "%h %t \"%r\" %>s \"%{User-Agent}i\"";
    private final String line2 = "127.0.0.2 [31/Dec/2012:23:49:42 +0100] "
            + "\"GET /foo HTTP/1.1\" 404 "
            + "\"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\"";

    private void validateLine2 (Parser parser) throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        TestRecord record = new TestRecord();
        parser.parse(record, line2);
        Map<String, String> results = record.getResults();

        assertEquals("127.0.0.2", results.get("IP:connection.client.host"));
        assertEquals("[31/Dec/2012:23:49:42 +0100]", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("/foo", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("404", results.get("STRING:request.status.last"));
        assertEquals(null, results.get("BYTES:response.body.bytesclf"));
        assertEquals(null, results.get("HTTP.URI:request.referer"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                results.get("HTTP.USERAGENT:request.user-agent"));
    }

    // ------------------------------------------

}
