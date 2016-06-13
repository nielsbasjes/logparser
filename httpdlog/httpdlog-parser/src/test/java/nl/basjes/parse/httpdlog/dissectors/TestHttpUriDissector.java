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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestHttpUriDissector {
    public static class MyRecord {

        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({
            "HTTP.URI:request.referer",
            "HTTP.PROTOCOL:request.referer.protocol",
            "HTTP.USERINFO:request.referer.userinfo",
            "HTTP.HOST:request.referer.host",
            "HTTP.PORT:request.referer.port",
            "HTTP.PATH:request.referer.path",
            "HTTP.QUERYSTRING:request.referer.query",
            "HTTP.REF:request.referer.ref"})
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
    public static void setUp() throws ParseException {
        String logformat = "%{Referer}i";
        parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);
        record = new MyRecord();
    }

    @Test
    public void testFullUrl1() throws Exception {
        parser.parse(record, "http://www.example.com/some/thing/else/index.html?foofoo=bar%20bar");

        assertEquals("Full input", "http://www.example.com/some/thing/else/index.html?foofoo=bar%20bar", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", "http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", "www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&foofoo=bar%20bar", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", null, record.getValue("HTTP.REF:request.referer.ref"));
    }

    @Test
    public void testFullUrl2() throws Exception {
        record.clear();
        parser.parse(record, "http://www.example.com/some/thing/else/index.html&aap=noot?foofoo=barbar&");

        assertEquals("Full input", "http://www.example.com/some/thing/else/index.html&aap=noot?foofoo=barbar&",
            record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", "http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", "www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&aap=noot&foofoo=barbar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", null, record.getValue("HTTP.REF:request.referer.ref"));
    }

    @Test
    public void testFullUrl3() throws Exception {
        record.clear();
        parser.parse(record, "http://www.example.com:8080/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla");

        assertEquals("Full input", "http://www.example.com:8080/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla",
            record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", "http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", "www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", "8080", record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&aap=noot&foofoo=barbar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", "blabla", record.getValue("HTTP.REF:request.referer.ref"));
    }

    @Test
    public void testFullUrl4() throws Exception {
        record.clear();
        parser.parse(record, "/some/thing/else/index.html?foofoo=barbar#blabla");

        assertEquals("Full input", "/some/thing/else/index.html?foofoo=barbar#blabla", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", null, record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", null, record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&foofoo=barbar", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", "blabla", record.getValue("HTTP.REF:request.referer.ref"));
    }

    @Test
    public void testFullUrl5() throws Exception {
        record.clear();
        parser.parse(record, "/some/thing/else/index.html&aap=noot?foofoo=bar%20bar&#bla%20bla");

        assertEquals("Full input", "/some/thing/else/index.html&aap=noot?foofoo=bar%20bar&#bla%20bla", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", null, record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", null, record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&aap=noot&foofoo=bar%20bar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", "bla bla", record.getValue("HTTP.REF:request.referer.ref"));
    }

    @Test
    public void testAndroidApp1() throws Exception {
        record.clear();
        parser.parse(record, "android-app://com.google.android.googlequicksearchbox");

        assertEquals("Full input", "android-app://com.google.android.googlequicksearchbox", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", "android-app", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", "com.google.android.googlequicksearchbox", record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", null, record.getValue("HTTP.REF:request.referer.ref"));
    }

    @Test
    public void testAndroidApp2() throws Exception {
        record.clear();
        parser.parse(record, "android-app://com.google.android.googlequicksearchbox/https/www.google.com");

        assertEquals("Full input", "android-app://com.google.android.googlequicksearchbox/https/www.google.com", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", "android-app", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", "com.google.android.googlequicksearchbox", record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/https/www.google.com", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", null, record.getValue("HTTP.REF:request.referer.ref"));
    }

}
