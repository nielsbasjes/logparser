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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestHttpFirstLineDissector {
    public static class MyRecord {

        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({
            "HTTP.FIRSTLINE:request.firstline",
            "HTTP.METHOD:request.firstline.method",
            "HTTP.URI:request.firstline.uri",
            "HTTP.PROTOCOL:request.firstline.protocol",
            "HTTP.PROTOCOL.VERSION:request.firstline.protocol.version",
        })
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public String getValue(final String name) {
            return results.get(name);
        }

        public void clear() {
            results.clear();
        }
    }

    private static Parser<MyRecord> parser;
    private static MyRecord record;

    @BeforeClass
    public static void setUp() {
        String logformat = "%r";
        parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);
        record = new MyRecord();
    }

    @Test
    public void testNormal() throws Exception {
        record.clear();
        parser.parse(record, "GET /index.html HTTP/1.1");
        assertEquals("Wrong request.firstline", "GET /index.html HTTP/1.1", record.getValue("HTTP.FIRSTLINE:request.firstline"));
        assertEquals("Wrong request.firstline.method", "GET", record.getValue("HTTP.METHOD:request.firstline.method"));
        assertEquals("Wrong request.firstline.uri", "/index.html", record.getValue("HTTP.URI:request.firstline.uri"));
        assertEquals("Wrong request.firstline.protocol", "HTTP", record.getValue("HTTP.PROTOCOL:request.firstline.protocol"));
        assertEquals("Wrong request.firstline.protocol.version", "1.1", record.getValue("HTTP.PROTOCOL.VERSION:request.firstline.protocol.version"));
    }

    @Test
    public void testChoppedFirstLine() throws Exception {
        record.clear();
        parser.parse(record, "GET /index.html HTT");
        assertEquals("Wrong request.firstline", "GET /index.html HTT", record.getValue("HTTP.FIRSTLINE:request.firstline"));
        assertEquals("Wrong request.firstline.method", "GET", record.getValue("HTTP.METHOD:request.firstline.method"));
        assertEquals("Wrong request.firstline.uri", "/index.html HTT", record.getValue("HTTP.URI:request.firstline.uri"));
        assertEquals("Wrong request.firstline.protocol", null, record.getValue("HTTP.PROTOCOL:request.firstline.protocol"));
        assertEquals("Wrong request.firstline.protocol.version", null, record.getValue("HTTP.PROTOCOL.VERSION:request.firstline.protocol.version"));
    }

    @Test
    public void testStrangeCommandVersionControl() throws Exception {
        record.clear();
        parser.parse(record, "VERSION-CONTROL /index.html HTTP/1.1");
        assertEquals("Wrong request.firstline", "VERSION-CONTROL /index.html HTTP/1.1", record.getValue("HTTP.FIRSTLINE:request.firstline"));
        assertEquals("Wrong request.firstline.method", "VERSION-CONTROL", record.getValue("HTTP.METHOD:request.firstline.method"));
        assertEquals("Wrong request.firstline.uri", "/index.html", record.getValue("HTTP.URI:request.firstline.uri"));
        assertEquals("Wrong request.firstline.protocol", "HTTP", record.getValue("HTTP.PROTOCOL:request.firstline.protocol"));
        assertEquals("Wrong request.firstline.protocol.version", "1.1", record.getValue("HTTP.PROTOCOL.VERSION:request.firstline.protocol.version"));
    }
}
