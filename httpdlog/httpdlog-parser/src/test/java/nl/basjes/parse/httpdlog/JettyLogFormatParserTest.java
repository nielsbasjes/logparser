/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JettyLogFormatParserTest {

    // ------------------------------------------

    public static class TestRecord {
        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings("UnusedDeclaration")
        @Field({
            "HTTP.URI:request.referer",
            "HTTP.USERAGENT:request.user-agent",
            "TIME.DAY:request.receive.time.day"
        })
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
    public void buggyJettyLogline() throws Exception {
        // In Jetty an extra space is included if the useragent is absent (the >"-"  < near the end).
        String line = "0.0.0.0 - - [24/Jul/2016:07:08:31 +0000] \"GET http://[:1]/foo HTTP/1.1\" 400 0 \"http://other.site\" \"-\"  - 8";

        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class,
            "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %I %O"     + "\n" +
            "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"  %I %O"
        );

        TestRecord record = new TestRecord();
        parser.parse(record, line);
        Map<String, String> results = record.getResults();

        System.out.println(results.toString());

        assertEquals("http://other.site", results.get("HTTP.URI:request.referer"));
        assertEquals(null, results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("24", results.get("TIME.DAY:request.receive.time.day"));
    }

    // ------------------------------------------

}
