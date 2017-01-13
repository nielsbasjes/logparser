/*
 * Apache HTTPD & NGINX Access log parsing made easy
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
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApacheHttpdLogParserTest {

    // ------------------------------------------

    public static class TestRecord {
        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings("UnusedDeclaration")
        @Field({
            "STRING:request.firstline.uri.query.*",
            "STRING:request.querystring.aap",
            "IP:connection.client.ip",
            "NUMBER:connection.client.logname",
            "STRING:connection.client.user",
            "TIME.STAMP:request.receive.time",
            "TIME.SECOND:request.receive.time.second",
            "HTTP.URI:request.firstline.uri",
            "STRING:request.status.last",
            "BYTESCLF:response.body.bytes",
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
            "MICROSECONDS:response.server.processing.time",
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
    private static final String logFormat = "%%%h %a %A %l %u %t \"%r\" %>s %b %p \"%q\" \"%!200,304,302{Referer}i\" %D " +
            "\"%200{User-agent}i\" \"%{Cookie}i\" \"%{Set-Cookie}o\" \"%{If-None-Match}i\" \"%{Etag}o\"";

    // Because header names are case insensitive we use the lowercase version internally
    // The modifiers ( like '!200,304,302') are to be removed.
    // This next value is what should be used internally
    private static final String expectedLogFormat = "%%%h %a %A %l %u [%t] \"%r\" %>s %b %p \"%q\" \"%{referer}i\" %D " +
            "\"%{user-agent}i\" \"%{cookie}i\" \"%{set-cookie}o\" \"%{if-none-match}i\" \"%{etag}o\"";

    // ------------------------------------------

    /**
     * Test of initialize method, of class ApacheHttpdLogParser.
     */
    @Test
    public void fullTest1() throws Exception {
        String line = "%127.0.0.1 127.0.0.1 127.0.0.1 - - [31/Dec/2012:23:49:40 +0100] "
                + "\"GET /icons/powered_by_rh.png?aap=noot&res=1024x768 HTTP/1.1\" 200 1213 "
                + "80 \"\" \"http://localhost/index.php?mies=wim\" 351 "
                + "\"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" "
                + "\"jquery-ui-theme=Eggplant\" \"Apache=127.0.0.1.1344635380111339; path=/; domain=.basjes.nl\" \"-\" "
                + "\"\\\"3780ff-4bd-4c1ce3df91380\\\"\"";

        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logFormat);

        // Manually add an extra dissector
        parser.addDissector(new ScreenResolutionDissector());
        parser.addTypeRemapping("request.firstline.uri.query.res", "SCREENRESOLUTION");
        List<String> extraFields = new ArrayList<>();
        extraFields.add("SCREENWIDTH:request.firstline.uri.query.res.width");
        extraFields.add( "SCREENHEIGHT:request.firstline.uri.query.res.height");
        parser.addParseTarget(TestRecord.class.getMethod("setValue",String.class, String.class), extraFields);

        TestRecord record = new TestRecord();
        parser.parse(record, line);
        Map<String, String> results = record.getResults();

        System.out.println(results.toString());

        assertEquals("noot", results.get("STRING:request.firstline.uri.query.aap"));
        assertEquals(null, results.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, results.get("STRING:request.querystring.aap"));
        assertEquals("1024", results.get("SCREENWIDTH:request.firstline.uri.query.res.width"));
        assertEquals("768", results.get("SCREENHEIGHT:request.firstline.uri.query.res.height"));

        assertEquals("127.0.0.1", results.get("IP:connection.client.ip"));
        assertEquals(null, results.get("NUMBER:connection.client.logname"));
        assertEquals(null, results.get("STRING:connection.client.user"));
        assertEquals("31/Dec/2012:23:49:40 +0100", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("1356994180000", results.get("TIME.EPOCH:request.receive.time.epoch"));
        assertEquals("1", results.get("TIME.WEEK:request.receive.time.weekofweekyear"));
        assertEquals("2013", results.get("TIME.YEAR:request.receive.time.weekyear"));
        assertEquals("2012", results.get("TIME.YEAR:request.receive.time.year"));
        assertEquals("40", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals("/icons/powered_by_rh.png?aap=noot&res=1024x768", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));
        assertEquals("1213", results.get("BYTESCLF:response.body.bytes"));
        assertEquals("http://localhost/index.php?mies=wim", results.get("HTTP.URI:request.referer"));
        assertEquals("wim", results.get("STRING:request.referer.query.mies"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("31", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("December", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("351", results.get("MICROSECONDS:response.server.processing.time"));
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
        assertEquals("10/Aug/2012:23:55:11 +0200", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("11", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals("/icons/powered_by_rh.png", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));
        assertEquals("1213", results.get("BYTESCLF:response.body.bytes"));
        assertEquals("http://localhost/", results.get("HTTP.URI:request.referer"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("10", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("August", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("1306", results.get("MICROSECONDS:response.server.processing.time"));
        assertEquals(null, results.get("HTTP.SETCOOKIES:response.cookies"));
        assertEquals("jquery-ui-theme=Eggplant; Apache=127.0.0.1.1344635667182858",
                results.get("HTTP.COOKIES:request.cookies"));
        assertEquals("\\\"3780ff-4bd-4c1ce3df91380\\\"", results.get("HTTP.HEADER:response.header.etag"));
        // assertEquals("351",results.get("COOKIE:request.cookie.jquery-ui-theme"));
    }

    // ------------------------------------------

    @Test
    public void fullTestTooLongUri() throws Exception {
        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logFormat);

        String line = "%127.0.0.1 127.0.0.1 127.0.0.1 - - [10/Aug/2012:23:55:11 +0200] \"GET /ImagineAURLHereThatIsTooLong\" 414 1213 80"
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
        assertEquals("10/Aug/2012:23:55:11 +0200", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("11", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals("/ImagineAURLHereThatIsTooLong", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("414", results.get("STRING:request.status.last"));
        assertEquals("1213", results.get("BYTESCLF:response.body.bytes"));
        assertEquals("http://localhost/", results.get("HTTP.URI:request.referer"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("10", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("August", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("1306", results.get("MICROSECONDS:response.server.processing.time"));
        assertEquals(null, results.get("HTTP.SETCOOKIES:response.cookies"));
        assertEquals("jquery-ui-theme=Eggplant; Apache=127.0.0.1.1344635667182858",
                results.get("HTTP.COOKIES:request.cookies"));
        assertEquals("\\\"3780ff-4bd-4c1ce3df91380\\\"", results.get("HTTP.HEADER:response.header.etag"));
        // assertEquals("351",results.get("COOKIE:request.cookie.jquery-ui-theme"));
    }

    // ------------------------------------------

    public static class TestRecordMissing {
        @SuppressWarnings({"UnusedDeclaration", "EmptyMethod"})
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
        } catch (MissingDissectorsException e) {
            assertTrue(e.getMessage().contains("HEADER:response.header.etag.thisshouldbemissing"));
        }
    }

    // ------------------------------------------

    public static class TestRecordMissing2 {
        @SuppressWarnings({"UnusedDeclaration", "EmptyMethod"})
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
        } catch (MissingDissectorsException e) {
            assertTrue(e.getMessage().contains("BLURP:request.firstline.uri.query.thisshouldbemissing"));
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
    public void testLogFormatCleanup(){
        ApacheHttpdLogFormatDissector d = new ApacheHttpdLogFormatDissector();

        assertEquals("foo", d.cleanupLogFormat("foo"));
        assertEquals(expectedLogFormat, d.cleanupLogFormat(logFormat));
        assertEquals("%{user-agent}i %% %{referer}i %s %{user-agent}i %% %{referer}i",
                d.cleanupLogFormat("%400,501{User-agent}i %% %!200,304,302{Referer}i %s %{User-agent}i %% %{Referer}i"));
    }

    @Test
    public void verifyCommonFormatNamesMapping() {
        ApacheHttpdLogFormatDissector dissector = new ApacheHttpdLogFormatDissector("combined");
        assertEquals("%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"", dissector.getLogFormat());
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
    public void testQueryStringDissector() throws Exception {
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
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?foo HTTP/1.1");
        assertEquals("", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&foo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html&foo HTTP/1.1");
        assertEquals("", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals(null, record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&foo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?foo=foofoo# HTTP/1.1");
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
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html?bar&foo=foofoo# HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
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
        parser.parse(record, "GET /index.html&bar=barbar&foo=foofoo#bla HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("barbar", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar=barbar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals("bla", record.get("HTTP.REF:request.firstline.uri.ref"));

        record.clear();
        parser.parse(record, "GET /index.html&bar=barbar?foo=foofoo HTTP/1.1");
        assertEquals("foofoo", record.get("STRING:request.firstline.uri.query.foo"));
        assertEquals("barbar", record.get("STRING:request.firstline.uri.query.bar"));
        assertEquals("/index.html", record.get("HTTP.PATH:request.firstline.uri.path"));
        assertEquals("&bar=barbar&foo=foofoo", record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

    }

    // ------------------------------------------

    /**
     * Test of mod_reqtimeout 408 status code
     * Assume  mod_reqtimeout is enabled and absolutely no data is entered by a client
     * after making the connection. The result is a http 408 status code and a logline that has proven to
     * result in several field failing to be parsed because they are different than the specifications.
     */
    @Test
    public void test408ModReqTimeout() throws Exception {

        final String logformat =
            "\"%%\" \"%a\" \"%{c}a\" \"%A\" \"%B\" \"%b\" \"%D\" \"%f\" \"%h\" \"%H\" \"%k\" " +
            "\"%l\" \"%L\" \"%m\" \"%p\" \"%{canonical}p\" \"%{local}p\" \"%{remote}p\" \"%P\" \"%{pid}P\" \"%{tid}P\"" +
            " \"%{hextid}P\" \"%q\" \"%r\" \"%R\" \"%s\" \"%>s\" \"%t\" \"%{msec}t\" \"%{begin:msec}t\" \"%{end:msec}t" +
            "\" \"%{usec}t\" \"%{begin:usec}t\" \"%{end:usec}t\" \"%{msec_frac}t\" \"%{begin:msec_frac}t\" \"%{end:mse" +
            "c_frac}t\" \"%{usec_frac}t\" \"%{begin:usec_frac}t\" \"%{end:usec_frac}t\" \"%T\" \"%u\" \"%U\" \"%v\" \"" +
            "%V\" \"%X\" \"%I\" \"%O\" \"%{cookie}i\" \"%{set-cookie}o\" \"%{user-agent}i\" \"%{referer}i\"";

        String line_200 = "\"%\" \"127.0.0.1\" \"127.0.0.1\" \"127.0.0.1\" \"3186\" \"3186\" \"1302\" \"/var/www/html/index.html\" " +
            "\"127.0.0.1\" \"HTTP/1.1\" \"0\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"50142\" \"10344\" \"10344\" " +
            "\"139854162249472\" \"139854162249472\" \"\" \"GET / HTTP/1.1\" \"-\" \"200\" \"200\" " +
            "\"[09/Aug/2016:22:57:59 +0200]\" \"1470776279833\" \"1470776279833\" \"1470776279835\" \"1470776279833934\" " +
            "\"1470776279833934\" \"1470776279835236\" \"833\" \"833\" \"835\" \"833934\" \"833934\" \"835236\" \"0\" " +
            "\"-\" \"/index.html\" \"committer.lan.basjes.nl\" \"localhost\" \"+\" \"490\" \"3525\" \"-\" \"-\" " +
            "\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\" \"-\"";

        String line_408 = "\"%\" \"127.0.0.1\" \"127.0.0.1\" \"127.0.0.1\" \"0\" \"-\" \"34\" \"-\" " +
            "\"127.0.0.1\" \"HTTP/1.0\" \"0\" \"-\" \"-\" \"-\" \"80\" \"80\" \"80\" \"50150\" \"10344\" \"10344\" " +
            "\"139854067267328\" \"139854067267328\" \"\" \"-\" \"-\" \"408\" \"408\" " +
            "\"[09/Aug/2016:22:59:14 +0200]\" \"1470776354625\" \"1470776354625\" \"1470776354625\" \"1470776354625377\" " +
            "\"1470776354625377\" \"1470776354625411\" \"625\" \"625\" \"625\" \"625377\" \"625377\" \"625411\" \"0\" " +
            "\"-\" \"-\" \"committer.lan.basjes.nl\" \"committer.lan.basjes.nl\" \"-\" \"0\" \"0\" \"-\" \"-\" \"-\" \"-\"";

        Parser<EmptyTestRecord> parser = new ApacheHttpdLoglineParser<>(EmptyTestRecord.class, logformat);
        String[] params = {
            "STRING:request.firstline.uri.query.foo",
        };
        parser.addParseTarget(EmptyTestRecord.class.getMethod("put", String.class, String.class), Arrays.asList(params));

        parser.parse(new EmptyTestRecord(), line_200);
        parser.parse(new EmptyTestRecord(), line_408);
    }

    @Test
    public void testIgnoreMissingDissectors() throws Exception {
        String line = "[09/Aug/2016:22:57:59 +0200]";

        String[] params = {
            "STRING:request.firstline.uri.query.foo",
        };

        Parser<EmptyTestRecord> parser = new ApacheHttpdLoglineParser<>(EmptyTestRecord.class, "%t");
        parser.addParseTarget(EmptyTestRecord.class.getMethod("put", String.class, String.class), Arrays.asList(params));

        parser.ignoreMissingDissectors();

        parser.parse(new EmptyTestRecord(), line);
    }


}
