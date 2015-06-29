/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestHttpUriDissector {
    public static class MyRecord {

        private final Map<String, String> results = new HashMap<>(32);

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
        parser.parse(record, "http://www.example.com/some/thing/else/index.html?foofoo=barbar");

        assertEquals("Full input", "http://www.example.com/some/thing/else/index.html?foofoo=barbar", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", "http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", "www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&foofoo=barbar", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
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
        parser.parse(record, "/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla");

        assertEquals("Full input", "/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla", record.getValue("HTTP.URI:request.referer"));
        assertEquals("Protocol is wrong", null, record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
        assertEquals("Userinfo is wrong", null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
        assertEquals("Host is wrong", null, record.getValue("HTTP.HOST:request.referer.host"));
        assertEquals("Port is wrong", null, record.getValue("HTTP.PORT:request.referer.port"));
        assertEquals("Path is wrong", "/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
        assertEquals("QueryString is wrong", "&aap=noot&foofoo=barbar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
        assertEquals("Ref is wrong", "blabla", record.getValue("HTTP.REF:request.referer.ref"));
    }


}
