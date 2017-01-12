/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JettyLogFormatParserTest {

    // ------------------------------------------

    public static class TestRecord {
        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings("UnusedDeclaration")
        @Field({
             "IP:connection.client.host"
            ,"NUMBER:connection.client.logname"
            ,"STRING:connection.client.user"
            ,"TIME.STAMP:request.receive.time"
            ,"TIME.DAY:request.receive.time.day"
            ,"HTTP.FIRSTLINE:request.firstline"
            ,"STRING:request.status.last"
            ,"BYTES:response.body.bytes"
            ,"HTTP.URI:request.referer"
            ,"HTTP.USERAGENT:request.user-agent"
            ,"MICROSECONDS:response.server.processing.time"
        })
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public Map<String, String> getResults() {
            return results;
        }
    }

    // ------------------------------------------

    @Test
    public void buggyJettyLogline() throws Exception {
        // In Jetty
        // - an extra space is included if the useragent is absent (the >"-"  < near the end).
        // - two extra spaces are included if the user field is absent ( " - " instead of "-" )
        String[] lines = {
            "0.0.0.0 - x [24/Jul/2016:07:08:31 +0000] \"GET http://[:1]/foo HTTP/1.1\" 400 0 \"http://other.site\" \"-\"  8",
            "0.0.0.0 -  -  [24/Jul/2016:07:08:31 +0000] \"GET http://[:1]/foo HTTP/1.1\" 400 0 \"http://other.site\" \"-\"  8",
            "0.0.0.0 - x [24/Jul/2016:07:08:31 +0000] \"GET http://[:1]/foo HTTP/1.1\" 400 0 \"http://other.site\" \"Mozilla/5.0 (dummy)\" 8",
            "0.0.0.0 -  -  [24/Jul/2016:07:08:31 +0000] \"GET http://[:1]/foo HTTP/1.1\" 400 0 \"http://other.site\" \"Mozilla/5.0 (dummy)\" 8",
        };

        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class,
            "ENABLE JETTY FIX\n"+
            "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %D"
        );

        for (String line:lines) {
            TestRecord record = new TestRecord();
            parser.parse(record, line);
            Map<String, String> results = record.getResults();

            assertEquals("0.0.0.0",results.get("IP:connection.client.host"));
            assertEquals(null,results.get("NUMBER:connection.client.logname"));

            String user = results.get("STRING:connection.client.user");
            if (user!=null) {
                assertEquals("x", user);
            }
            assertEquals("24/Jul/2016:07:08:31 +0000",results.get("TIME.STAMP:request.receive.time"));
            assertEquals("24",results.get("TIME.DAY:request.receive.time.day"));
            assertEquals("GET http://[:1]/foo HTTP/1.1",results.get("HTTP.FIRSTLINE:request.firstline"));
            assertEquals("400",results.get("STRING:request.status.last"));
            assertEquals("0",results.get("BYTES:response.body.bytes"));
            assertEquals("http://other.site",results.get("HTTP.URI:request.referer"));

            String useragent = results.get("HTTP.USERAGENT:request.user-agent");
            if (useragent!=null) {
                assertEquals("Mozilla/5.0 (dummy)", useragent);
            }
            assertEquals("8",results.get("MICROSECONDS:response.server.processing.time"));

        }
    }

    // -----------------------------------------

}
