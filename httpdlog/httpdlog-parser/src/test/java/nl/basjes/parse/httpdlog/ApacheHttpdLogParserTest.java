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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ApacheHttpdLogParserTest {

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
    private static final String LOG_FORMAT = "%%%h %a %A %l %u %t \"%r\" %>s %b %p \"%q\" \"%!200,304,302{Referer}i\" %D " +
            "\"%200{User-agent}i\" \"%{Cookie}i\" \"%{Set-Cookie}o\" \"%{If-None-Match}i\" \"%{Etag}o\"";

    // Because header names are case insensitive we use the lowercase version internally
    // The modifiers ( like '!200,304,302') are to be removed.
    // This next value is what should be used internally
    private static final String EXPECTED_LOG_FORMAT = "%%%h %a %A %l %u [%t] \"%r\" %>s %b %p \"%q\" \"%{referer}i\" %D " +
            "\"%{user-agent}i\" \"%{cookie}i\" \"%{set-cookie}o\" \"%{if-none-match}i\" \"%{etag}o\"";

    // ------------------------------------------

    /**
     * Test of initialize method, of class ApacheHttpdLogParser.
     */
    @Test
    void fullTest1() throws Exception {
        String line = "%127.0.0.1 127.0.0.1 127.0.0.1 - - [31/Dec/2012:23:49:40 +0100] "
                + "\"GET /icons/powered_by_rh.png?aap=noot&res=1024x768 HTTP/1.1\" 200 1213 "
                + "80 \"\" \"http://localhost/index.php?mies=wim\" 351 "
                + "\"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" "
                + "\"jquery-ui-theme=Eggplant\" \"Apache=127.0.0.1.1344635380111339; path=/; domain=.basjes.nl\" \"-\" "
                + "\"\\\"3780ff-4bd-4c1ce3df91380\\\"\"";

        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOG_FORMAT);

        // Manually add an extra dissector
        parser.addDissector(new ScreenResolutionDissector());
        parser.addTypeRemapping("request.firstline.uri.query.res", "SCREENRESOLUTION");
        List<String> extraFields = new ArrayList<>();
        extraFields.add("SCREENWIDTH:request.firstline.uri.query.res.width");
        extraFields.add("SCREENHEIGHT:request.firstline.uri.query.res.height");
        parser.addParseTarget(TestRecord.class.getMethod("setValue", String.class, String.class), extraFields);

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
        assertEquals("\"3780ff-4bd-4c1ce3df91380\"", results.get("HTTP.HEADER:response.header.etag"));

        assertEquals("Eggplant", results.get("HTTP.COOKIE:request.cookies.jquery-ui-theme"));
        assertEquals("Apache=127.0.0.1.1344635380111339; path=/; domain=.basjes.nl", results.get("HTTP.SETCOOKIE:response.cookies.apache"));
        assertEquals(".basjes.nl", results.get("STRING:response.cookies.apache.domain"));

    }

    // ------------------------------------------

    @Test
    void fullTest2() throws Exception {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOG_FORMAT);

        String line = "%127.0.0.1 127.0.0.1 127.0.0.1 - - [10/Aug/2012:23:55:11 +0200] \"GET /icons/powered_by_rh.png HTTP/1.1\" 200 1213 80"
                + " \"\" \"http://localhost/\" 1306 \"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\""
                + " \"jquery-ui-theme=Eggplant; Apache=127.0.0.1.1344635667182858\" \"-\" \"-\" \"\\\"3780ff-4bd-4c1ce3df91380\\\"\"";

        TestRecord record = new TestRecord();
        parser.parse(record, line);
        Map<String, String> results = record.getResults();

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
        assertEquals("\"3780ff-4bd-4c1ce3df91380\"", results.get("HTTP.HEADER:response.header.etag"));
        // assertEquals("351",results.get("COOKIE:request.cookie.jquery-ui-theme"));
    }

    // ------------------------------------------

    @Test
    void fullTestTooLongUri() throws Exception {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOG_FORMAT);

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
        assertEquals("\"3780ff-4bd-4c1ce3df91380\"", results.get("HTTP.HEADER:response.header.etag"));
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
    void testMissing() throws Exception {
        try {
            Parser<TestRecordMissing> parser = new HttpdLoglineParser<>(TestRecordMissing.class, LOG_FORMAT);
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
    void testMissing2() throws Exception {
        try {
            Parser<TestRecordMissing2> parser = new HttpdLoglineParser<>(TestRecordMissing2.class, LOG_FORMAT);
            parser.parse(""); // Just to trigger the internal assembly of things (that should fail).
            fail("Missing exception.");
        } catch (MissingDissectorsException e) {
            assertTrue(e.getMessage().contains("BLURP:request.firstline.uri.query.thisshouldbemissing"));
        }
    }

    // ------------------------------------------

    @Test
    void testGetPossiblePaths() {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOG_FORMAT);

        List<String> paths = parser.getPossiblePaths(5);
        assertEquals(true, paths.contains("TIME.SECOND:request.receive.time.second"));
        assertEquals(true, paths.contains("STRING:request.firstline.uri.query.*"));
        assertEquals(true, paths.contains("STRING:response.cookies.*.expires"));
        assertEquals(true, paths.contains("HTTP.HEADER:response.header.etag"));

        assertEquals(false, paths.contains("FIXED_STRING:fixed_string"));
    }

    // ------------------------------------------

    @Test
    void testGetPossiblePathsWithUnusableLogFormat() {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, "Empty");

        List<String> paths = parser.getPossiblePaths(5);
        assertTrue(paths == null || paths.isEmpty(), "The output should be empty!");
    }

    // ------------------------------------------
    @Test
    void testLogFormatCleanup(){
        ApacheHttpdLogFormatDissector d = new ApacheHttpdLogFormatDissector();

        assertEquals("foo", d.cleanupLogFormat("foo"));
        assertEquals(EXPECTED_LOG_FORMAT, d.cleanupLogFormat(LOG_FORMAT));
        assertEquals("%{user-agent}i %% %{referer}i %s %{user-agent}i %% %{referer}i",
                d.cleanupLogFormat("%400,501{User-agent}i %% %!200,304,302{Referer}i %s %{User-agent}i %% %{Referer}i"));
    }

    @Test
    void verifyCommonFormatNamesMapping() {
        ApacheHttpdLogFormatDissector dissector = new ApacheHttpdLogFormatDissector("combined");
        assertEquals("%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"", dissector.getLogFormat());
    }

    // ------------------------------------------

    public static class EmptyTestRecord extends HashMap<String, String> {
        @Override
        public String put(String key, String value) {
            return super.put(key, value);
        }
        private static final long serialVersionUID = 1L;
    }

    @Test
    void testQueryStringDissector() throws Exception {
        String logformat = "%r";

        Parser<EmptyTestRecord> parser = new HttpdLoglineParser<>(EmptyTestRecord.class, logformat);

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
        assertEquals(null, record.get("HTTP.QUERYSTRING:request.firstline.uri.query"));
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
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

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
        assertEquals(null, record.get("HTTP.REF:request.firstline.uri.ref"));

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
     * result in several fields failing to be parsed because they are different than the specifications.
     */
    @Test
    void test408ModReqTimeout() throws Exception {

        final String logformat =
            "\"%%\" \"%a\" \"%{c}a\" \"%A\" \"%B\" \"%b\" \"%D\" \"%f\" \"%h\" \"%H\" \"%k\" " +
            "\"%l\" \"%L\" \"%m\" \"%p\" \"%{canonical}p\" \"%{local}p\" \"%{remote}p\" \"%P\" \"%{pid}P\" \"%{tid}P\"" +
            " \"%{hextid}P\" \"%q\" \"%r\" \"%R\" \"%s\" \"%>s\" \"%t\" \"%{msec}t\" \"%{begin:msec}t\" \"%{end:msec}t" +
            "\" \"%{usec}t\" \"%{begin:usec}t\" \"%{end:usec}t\" \"%{msec_frac}t\" \"%{begin:msec_frac}t\" \"%{end:mse" +
            "c_frac}t\" \"%{usec_frac}t\" \"%{begin:usec_frac}t\" \"%{end:usec_frac}t\" \"%T\" \"%u\" \"%U\" \"%v\" \"" +
            "%V\" \"%X\" \"%I\" \"%O\" \"%{cookie}i\" \"%{set-cookie}o\" \"%{user-agent}i\" \"%{referer}i\"";

        String line200 = "\"%\" \"127.0.0.1\" \"127.0.0.1\" \"127.0.0.1\" \"3186\" \"3186\" \"1302\" \"/var/www/html/index.html\" " +
            "\"127.0.0.1\" \"HTTP/1.1\" \"0\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"50142\" \"10344\" \"10344\" " +
            "\"139854162249472\" \"139854162249472\" \"\" \"GET / HTTP/1.1\" \"-\" \"200\" \"200\" " +
            "\"[09/Aug/2016:22:57:59 +0200]\" \"1470776279833\" \"1470776279833\" \"1470776279835\" \"1470776279833934\" " +
            "\"1470776279833934\" \"1470776279835236\" \"833\" \"833\" \"835\" \"833934\" \"833934\" \"835236\" \"0\" " +
            "\"-\" \"/index.html\" \"committer.lan.basjes.nl\" \"localhost\" \"+\" \"490\" \"3525\" \"-\" \"-\" " +
            "\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\" \"-\"";

        String line408 = "\"%\" \"127.0.0.1\" \"127.0.0.1\" \"127.0.0.1\" \"0\" \"-\" \"34\" \"-\" " +
            "\"127.0.0.1\" \"HTTP/1.0\" \"0\" \"-\" \"-\" \"-\" \"80\" \"80\" \"80\" \"50150\" \"10344\" \"10344\" " +
            "\"139854067267328\" \"139854067267328\" \"\" \"-\" \"-\" \"408\" \"408\" " +
            "\"[09/Aug/2016:22:59:14 +0200]\" \"1470776354625\" \"1470776354625\" \"1470776354625\" \"1470776354625377\" " +
            "\"1470776354625377\" \"1470776354625411\" \"625\" \"625\" \"625\" \"625377\" \"625377\" \"625411\" \"0\" " +
            "\"-\" \"-\" \"committer.lan.basjes.nl\" \"committer.lan.basjes.nl\" \"-\" \"0\" \"0\" \"-\" \"-\" \"-\" \"-\"";

        Parser<EmptyTestRecord> parser =
            new HttpdLoglineParser<>(EmptyTestRecord.class, logformat)
            .addParseTarget(EmptyTestRecord.class.getMethod("put", String.class, String.class),
                            "STRING:request.firstline.uri.query.foo");
        parser.parse(new EmptyTestRecord(), line200);
        parser.parse(new EmptyTestRecord(), line408);
    }

    @Test
    void testFailOnMissingDissectors() {
        assertThrows(MissingDissectorsException.class, () -> {
            String line = "[09/Aug/2016:22:57:59 +0200]";

            String[] params = {
                "STRING:request.firstline.uri.query.foo",
                "TIME.EPOCH:request.receive.time.epoch",
            };

            new HttpdLoglineParser<>(EmptyTestRecord.class, "%t")
                .addParseTarget(EmptyTestRecord.class.getMethod("put", String.class, String.class), Arrays.asList(params))
                .failOnMissingDissectors()
                .parse(new EmptyTestRecord(), line);
        });
    }

    @Test
    void testIgnoreMissingDissectors() throws Exception {
        String line = "[09/Aug/2016:22:57:59 +0200]";

        new HttpdLoglineParser<>(EmptyTestRecord.class, "%t")
            .addParseTarget(EmptyTestRecord.class.getMethod("put", String.class, String.class),
                            Arrays.asList("STRING:request.firstline.uri.query.foo",
                                          "TIME.EPOCH:request.receive.time.epoch"))
            .ignoreMissingDissectors()
            .parse(new EmptyTestRecord(), line);
    }

    @Test
    void testExternalExample() {
        // Found on 2022-06-10 on
        // https://github.com/cdapio/cdap/blob/develop/cdap-docs/user-guide/source/data-preparation/directives/parse-as-log.rst
        String logFormat = "%t %u [%D %h %{True-Client-IP}i %{UNIQUE_ID}e %r] %{Cookie}i %s \"%{User-Agent}i\" \"%{host}i\" %l %b %{Referer}i";
        String logLine = "[03/Dec/2013:10:53:59 +0000] - [32002 10.102.4.254 195.229.241.182 Up24RwpmBAwAAA1LWJsAAAAR GET " +
            "/content/dam/Central_Library/Street_Shots/Youth/2012/09sep/LFW/Gallery_03/LFW_SS13_SEPT_12_777.jpg." +
            "image.W0N539E3452S3991w313.original.jpg HTTP/1.1] __utmc=94539802; dtCookie=EFD9D09B6A2E1789F1329FC1" +
            "381A356A|_default|1; dtPC=471217988_141#_load_; Carte::KerberosLexicon_getdomain=6701c1320dd96688b2e" +
            "40b92ce748eee7ae99722; UserData=Username%3ALSHARMA%3AHomepage%3A1%3AReReg%3A0%3ATrialist%3A0%3ALangua" +
            "ge%3Aen%3ACcode%3Aae%3AForceReReg%3A0; UserID=1375493%3A12345%3A1234567890%3A123%3Accode%3Aae; USER_D" +
            "ATA=1375493%3ALSharma%3ALokesh%3ASharma%3Alokesh.sharma%40landmarkgroup.com%3A0%3A1%3Aen%3Aae%3A%3Ado" +
            "main%3A1386060868.51392%3A6701c1320dd96688b2e40b92ce748eee7ae99722; MODE=FONTIS; __utma=94539802.9110" +
            "97326.1339390457.1386060848.1386065609.190; __utmz=94539802.1384758205.177.38.utmcsr=google|utmccn=(o" +
            "rganic)|utmcmd=organic|utmctr=(not%20provided); __kti=1339390460526,http%3A%2F%2Fwww.domain.com%2F,;" +
            "__ktv=28e8-6c4-be3-ce54137d9e48271; WT_FPC=id=2.50.27.157-3067016480.30226245:lv=1386047044279:ss=138" +
            "6046439530; _opt_vi_3FNG8DZU=42880957-D2F1-4DC5-AF16-FEF88891D24E; __hstc=145721067.750d315a49c642681" +
            "92826b3911a4e5a.1351772962050.1381151113005.1381297633204.66; hsfirstvisit=http%3A%2F%2Fwww.domain.co" +
            "m%2F|http%3A%2F%2Fwww.google.co.in%2Furl%3Fsa%3Dt%26rct%3Dj%26q%3Ddomain.com%26source%3Dweb%26cd%3D1%" +
            "26ved%3D0CB0QFjAA%26url%3Dhttp%3A%2F%2Fwww.domain.com%2F%26ei%3DDmuSULW3AcTLhAfJ24CoDA%26usg%3DAFQjCN" +
            "GvPmmyn8Bk67OUv-HwjVU4Ff3q1w|1351772962000; hubspotutk=750d315a49c64268192826b3911a4e5a; __ptca=14572" +
            "1067.jQ7lN5U3C4eN.1351758562.1381136713.1381283233.66; __ptv_62vY4e=jQ7lN5U3C4eN; __pti_62vY4e=jQ7lN5" +
            "U3C4eN; __ptcz=145721067.1351758562.1.0.ptmcsr=google|ptmcmd=organic|ptmccn=(organic)|ptmctr=domain." +
            "com; RM=Lsharma%3Ac163b6097f90d2869e537f95900e1c464daa8fb9; wcid=Up2cRApmBAwAAFOiVhcAAAAH%3Af32e5e5f5" +
            "b593175bfc71af082ab26e4055efeb6; __utmb=94539802.71.9.1386067462709; edge_auth=ip%3D195.229.241.182~" +
            "expires%3D1386069280~access%3D%2Fapps%2F%2A%21%2Fbin%2F%2A%21%2Fcontent%2F%2A%21%2Fetc%2F%2A%21%2Fho" +
            "me%2F%2A%21%2Flibs%2F%2A%21%2Freport%2F%2A%21%2Fsection%2F%2A%21%2Fdomain%2F%2A~md5%3D5b47f341723924" +
            "87dcd44c1d837e2e54; has_js=1; SECTION=%2Fcontent%2Fsection%2Finspiration-design%2Fstreet-shots.html;" +
            "JSESSIONID=b9377099-7708-45ae-b6e7-c575ffe82187; WT_FPC=id=2.50.27.157-3067016480.30226245:lv=138605" +
            "3618209:ss=1386053618209; USER_GROUP=LSharma%3Afalse; NSC_wtfswfs_xfcgbsn40-41=ffffffff096e1a1d45525" +
            "d5f4f58455e445a4a423660 200 \"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)\" " +
            "\"www.domain.com\" - 24516 http://www.domain.com/content/report/Street_Shots/Youth/Global_round_up/201" +
            "3/01_Jan/mens_youth_stylingglobalround-up1.html";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(nl.basjes.parse.core.test.TestRecord.class, logFormat)
                .addTypeRemapping("server.environment.unique_id", "MOD_UNIQUE_ID"))
            .withInput(logLine)
            .printPossible()
            .printAllPossibleValues()
            // A normal field
            .expect("IP:connection.client.host", "10.102.4.254")
            // The timestamp was parsed and normalized to the Epoch milliseconds
            .expect("TIME.EPOCH:request.receive.time.epoch", "1386068039000")
            // A cookie with a very strange name
            .expect("HTTP.COOKIE:request.cookies.carte::kerberoslexicon_getdomain", "6701c1320dd96688b2e40b92ce748eee7ae99722")
            // A cookie value that needed a lot of decoding
            .expect("HTTP.COOKIE:request.cookies.hsfirstvisit", "http://www.domain.com/|http://www.google.co.in/url?sa=t&rct=j&q=domain.com&source=web&cd=1&ved=0CB0QFjAA&url=http://www.domain.com/&ei=DmuSULW3AcTLhAfJ24CoDA&usg=AFQjCNGvPmmyn8Bk67OUv-HwjVU4Ff3q1w|1351772962000")
            // This is the IP which was extracted from the UNIQUE_ID ( "Up24RwpmBAwAAA1LWJsAAAAR" in this case).
            .expect("IP:server.environment.unique_id.ip", "10.102.4.12")
            .checkExpectations();
    }


}
