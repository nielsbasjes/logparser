/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
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

package nl.basjes.parse.apachehttpdlogparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.apachehttpdlog.logformat.ApacheHttpdLogFormatDisector;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;

import org.junit.Test;

public class ApacheHttpdLogParserTest {

    // ------------------------------------------

    public static class TestRecord {
        private final Map<String, String> results = new HashMap<>(32);

        @Field({
            "STRING:request.firstline.uri.query.aap",
            "STRING:request.firstline.uri.query.noot",
            "STRING:request.querystring.aap",
            "IP:connection.client.ip",
            "NUMBER:connection.client.logname",
            "STRING:connection.client.user",
            "TIME.STAMP:request.receive.time",
            "TIME.SECOND:request.receive.time.second",
            "HTTP.URI:request.firstline.uri",
            "STRING:request.status.last",
            "BYTES:response.body.bytesclf",
            "HTTP.URI:request.referer",
            "STRING:request.referer.query.mies",
            "STRING:request.referer.query.wim",
            "HTTP.USERAGENT:request.user-agent",
            "TIME.DAY:request.receive.time.day",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.MONTHNAME:request.receive.time.monthname",
            "TIME.EPOCH:request.receive.time.epoch",
            "TIME.WEEK:request.receive.time.weekofweekyear",
            "TIME.YEAR:request.receive.time.weekyear",
            "TIME.YEAR:request.receive.time.year",
            "HTTP.COOKIES:request.cookies",
            "HTTP.SETCOOKIES:response.cookies",
            "HTTP.COOKIE:request.cookies.jquery-ui-theme",
            "HTTP.SETCOOKIE:response.cookies.apache",
            "STRING:response.cookies.apache.domain",
            "MICROSECONDS:server.process.time",
            "STRING:request.status.last",
            "HTTP.HEADER:response.header.etag"})
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public Map<String, String> getResults() {
            return results;
        }
    }

    // ------------------------------------------

    // LogFormat
    // "%h %a %A %l %u %t \"%r\" %>s %b %p \"%q\" \"%{Referer}i\" %D \"%{User-agent}i\" \"%{Cookie}i\" \"%{Set-Cookie}o\" "
    // +"\"%{If-None-Match}i\" \"%{Etag}o\""
    // fullcombined
    private final String logFormat = "%%%h %a %A %l %u %t \"%r\" %>s %b %p \"%q\" \"%{Referer}i\" %D \"%{User-agent}i\" \"%{Cookie}i\" "
                             + "\"%{Set-Cookie}o\" \"%{If-None-Match}i\" \"%{Etag}o\"";

    // Because header names are case insensitive we use the lowercase version internally
    // This next value is what should be used internally
    private final String expectedLogFormat = "%%%h %a %A %l %u %t \"%r\" %>s %b %p \"%q\" \"%{referer}i\" %D \"%{user-agent}i\" \"%{cookie}i\" "
            + "\"%{set-cookie}o\" \"%{if-none-match}i\" \"%{etag}o\"";

    // ------------------------------------------

    /**
     * Test of initialize method, of class ApacheHttpdLogParser.
     */
    @Test
    public void fullTest1() throws Exception {
        String line = "%127.0.0.1 127.0.0.1 127.0.0.1 - - [31/Dec/2012:23:49:40 +0100] "
                + "\"GET /icons/powered_by_rh.png?aap=noot HTTP/1.1\" 200 1213 "
                + "80 \"\" \"http://localhost/index.php?mies=wim\" 351 "
                + "\"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" "
                + "\"jquery-ui-theme=Eggplant\" \"Apache=127.0.0.1.1344635380111339; path=/; domain=.basjes.nl\" \"-\" "
                + "\"\\\"3780ff-4bd-4c1ce3df91380\\\"\"";

        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logFormat);

        TestRecord record = new TestRecord();
        parser.parse(record, line);
        Map<String, String> results = record.getResults();

        System.out.println(results.toString());

        assertEquals("noot", results.get("STRING:request.firstline.uri.query.aap"));
        assertEquals(null, results.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, results.get("STRING:request.querystring.aap"));
        assertEquals("127.0.0.1", results.get("IP:connection.client.ip"));
        assertEquals(null, results.get("NUMBER:connection.client.logname"));
        assertEquals(null, results.get("STRING:connection.client.user"));
        assertEquals("[31/Dec/2012:23:49:40 +0100]", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("1356994180000", results.get("TIME.EPOCH:request.receive.time.epoch"));
        assertEquals("1", results.get("TIME.WEEK:request.receive.time.weekofweekyear"));
        assertEquals("2013", results.get("TIME.YEAR:request.receive.time.weekyear"));
        assertEquals("2012", results.get("TIME.YEAR:request.receive.time.year"));
        assertEquals("40", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals("/icons/powered_by_rh.png?aap=noot", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));
        assertEquals("1213", results.get("BYTES:response.body.bytesclf"));
        assertEquals("http://localhost/index.php?mies=wim", results.get("HTTP.URI:request.referer"));
        assertEquals("wim", results.get("STRING:request.referer.query.mies"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("31", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("December", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("351", results.get("MICROSECONDS:server.process.time"));
        assertEquals("Apache=127.0.0.1.1344635380111339; path=/; domain=.basjes.nl",
                results.get("HTTP.SETCOOKIES:response.cookies"));
        assertEquals("jquery-ui-theme=Eggplant", results.get("HTTP.COOKIES:request.cookies"));
        assertEquals("\\\"3780ff-4bd-4c1ce3df91380\\\"", results.get("HTTP.HEADER:response.header.etag"));

        assertEquals("Eggplant", results.get("HTTP.COOKIE:request.cookies.jquery-ui-theme"));
        assertEquals("Apache=127.0.0.1.1344635380111339; path=/; domain=.basjes.nl", results.get("HTTP.SETCOOKIE:response.cookies.apache"));
        assertEquals(".basjes.nl", results.get("STRING:response.cookies.apache.domain"));

    }

    // ------------------------------------------

    @Test
    public void fullTest2() throws Exception {
        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logFormat);

        String line = "%127.0.0.1 127.0.0.1 127.0.0.1 - - [10/Aug/2012:23:55:11 +0200] \"GET /icons/powered_by_rh.png HTTP/1.1\" 200 1213 80"
                + " \"\" \"http://localhost/\" 1306 \"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\""
                + " \"jquery-ui-theme=Eggplant; Apache=127.0.0.1.1344635667182858\" \"-\" \"-\" \"\\\"3780ff-4bd-4c1ce3df91380\\\"\"";

        TestRecord record = new TestRecord();
        parser.parse(record, line);
        Map<String, String> results = record.getResults();

        // System.out.println(results.toString());

        assertEquals(null, results.get("HTTP.QUERYSTRING:request.firstline.uri.query.foo"));
        assertEquals("127.0.0.1", results.get("IP:connection.client.ip"));
        assertEquals(null, results.get("NUMBER:connection.client.logname"));
        assertEquals(null, results.get("STRING:connection.client.user"));
        assertEquals("[10/Aug/2012:23:55:11 +0200]", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("11", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals("/icons/powered_by_rh.png", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));
        assertEquals("1213", results.get("BYTES:response.body.bytesclf"));
        assertEquals("http://localhost/", results.get("HTTP.URI:request.referer"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("10", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("August", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("1306", results.get("MICROSECONDS:server.process.time"));
        assertEquals(null, results.get("HTTP.SETCOOKIES:response.cookies"));
        assertEquals("jquery-ui-theme=Eggplant; Apache=127.0.0.1.1344635667182858",
                results.get("HTTP.COOKIES:request.cookies"));
        assertEquals("\\\"3780ff-4bd-4c1ce3df91380\\\"", results.get("HTTP.HEADER:response.header.etag"));
        // assertEquals("351",results.get("COOKIE:request.cookie.jquery-ui-theme"));
    }

    // ------------------------------------------

    public static class TestRecordMissing {
        @Field({ "STRING:request.firstline.uri.query.ThisShouldNOTBeMissing", "HEADER:response.header.Etag.ThisShouldBeMissing" })
        public void dummy(final String name, final String value) {
        }
    }

    @Test
    public void testMissing() throws Exception {
        try {
            Parser<TestRecordMissing> parser = new ApacheHttpdLoglineParser<>(TestRecordMissing.class, logFormat);
            parser.parse(""); // Just to trigger the internal assembly of things (that should fail).
            fail("Missing exception.");
        } catch (MissingDisectorsException e) {
            assertEquals("HEADER:response.header.etag.thisshouldbemissing ", e.getMessage());
        }
    }

    // ------------------------------------------

    public static class TestRecordMissing2 {
        @Field({ "BLURP:request.firstline.uri.query.ThisShouldBeMissing", "HTTP.HEADER:response.header.etag" })
        public void dummy(final String name, final String value) {
        }
    }

    @Test
    public void testMissing2() throws Exception {
        try {
            Parser<TestRecordMissing2> parser = new ApacheHttpdLoglineParser<>(TestRecordMissing2.class, logFormat);
            parser.parse(""); // Just to trigger the internal assembly of things (that should fail).
            fail("Missing exception.");
        } catch (MissingDisectorsException e) {
            assertEquals("BLURP:request.firstline.uri.query.thisshouldbemissing ", e.getMessage());
        }
    }

    // ------------------------------------------

    @Test
    public void testGetPossiblePaths() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logFormat);

        List<String> paths = parser.getPossiblePaths(5);
//        for (String path:paths){
//            System.out.println("--->"+path+"<---");
//        }
        assertEquals(true, paths.contains("TIME.SECOND:request.receive.time.second"));
        assertEquals(true, paths.contains("STRING:request.firstline.uri.query.*"));
        assertEquals(true, paths.contains("STRING:response.cookies.*.expires"));
        assertEquals(true, paths.contains("HTTP.HEADER:response.header.etag"));

        assertEquals(false, paths.contains("FIXED_STRING:fixed_string"));
    }

    // ------------------------------------------

    @Test
    public void testMakeHeaderNamesLowercaseInLogFormat(){
        assertEquals(expectedLogFormat, ApacheHttpdLogFormatDisector.makeHeaderNamesLowercaseInLogFormat(logFormat));
    }

    @Test
    public void verifyCommonFormatNamesMapping() throws Exception {
        ApacheHttpdLogFormatDisector disector = new ApacheHttpdLogFormatDisector("combined");
        assertEquals("%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"", disector.getLogFormat());
    }

    // ------------------------------------------

    public class EmptyTestRecord extends HashMap<String, String> {
        @Override
        public String put(String key, String value) {
            return super.put(key, value);
        }
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void testQueryStringDisector() throws Exception {
        String logformat = "%r";

        Parser<EmptyTestRecord> parser = new ApacheHttpdLoglineParser<>(EmptyTestRecord.class, logformat);

        String[] params = {"STRING:request.firstline.uri.query.foo",
                           "STRING:request.firstline.uri.query.bar",
                           "HTTP.PATH:request.firstline.uri.path",
                           "HTTP.QUERYSTRING:request.firstline.uri.query",
                           "HTTP.REF:request.firstline.uri.ref"
        };
        parser.addParseTarget(EmptyTestRecord.class.getMethod("put", String.class, String.class), Arrays.asList(params));

        EmptyTestRecord record = new EmptyTestRecord();

        parser.parse(record, "GET /index.html HTTP/1.1");
        assertEquals(null, record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?foo HTTP/1.1");
        assertEquals("", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&foo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html&foo HTTP/1.1");
        assertEquals("", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&foo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?foo=foofoo HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html&foo=foofoo HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?bar&foo=foofoo#bookmark HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("bookmark", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?bar=barbar&foo=foofoo#bookmark HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("barbar", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar=barbar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("bookmark", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html&bar=barbar&foo=foofoo HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("barbar", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar=barbar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html&bar=barbar?foo=foofoo HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("barbar", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar=barbar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("", record.get("HTTP.REF:request.firstline.uri.ref"));

    }

    // ------------------------------------------


    @Test(expected = UnsupportedOperationException.class)
    public void ensureNotYetImplementedFailure() throws Exception {
        new ApacheHttpdLogFormatDisector("%{format}t");
    }

}
