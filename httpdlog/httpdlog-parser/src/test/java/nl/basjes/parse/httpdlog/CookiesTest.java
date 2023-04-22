/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookiesTest {

    private static class EmptyTestRecord {
    }

    public static class TestRecord {

        private final Map<String, String> results     = new HashMap<>(32);
        private final Map<String, Long>   longResults = new HashMap<>(32);

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({
            "HTTP.QUERYSTRING:request.firstline.uri.query",
            "IP:connection.client.ip",
            "NUMBER:connection.client.logname",
            "STRING:connection.client.user",
            "HTTP.URI:request.firstline.uri",
            "STRING:request.status.last",
            "BYTESCLF:response.body.bytes",
            "HTTP.URI:request.referer",
            "HTTP.USERAGENT:request.user-agent",
            "TIME.STAMP:request.receive.time",
            "TIME.EPOCH:request.receive.time.epoch",
            "TIME.SECOND:request.receive.time.second",
            "TIME.DAY:request.receive.time.day",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.MONTH:request.receive.time.month",
            "TIME.YEAR:request.receive.time.year",
            "TIME.MONTHNAME:request.receive.time.monthname",
            "TIME.SECOND:request.receive.time.second_utc",
            "TIME.DAY:request.receive.time.day_utc",
            "TIME.HOUR:request.receive.time.hour_utc",
            "TIME.MONTH:request.receive.time.month_utc",
            "TIME.YEAR:request.receive.time.year_utc",
            "TIME.MONTHNAME:request.receive.time.monthname_utc",
            "MICROSECONDS:response.server.processing.time",
            "STRING:request.status.last",
            "HTTP.HEADER:response.header.etag",

            // Cookies
            "HTTP.COOKIES:request.cookies",
//            "HTTP.COOKIE:request.cookies.apache" ,
//            "HTTP.COOKIE:request.cookies.jquery-ui-theme",
            "HTTP.COOKIE:request.cookies.*",
            "HTTP.SETCOOKIES:response.cookies",
            "HTTP.SETCOOKIE:response.cookies.nba-4",
            "STRING:response.cookies.nba-4.value",
            "STRING:response.cookies.nba-4.expires",
            "STRING:response.cookies.nba-4.path",
            "STRING:response.cookies.nba-4.domain"
            })
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public Map<String, String> getResults() {
            return results;
        }

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({
            "BYTESCLF:response.body.bytes",
            "TIME.DAY:request.receive.time.day",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.SECOND:request.receive.time.second",
            "TIME.DAY:request.receive.time.day_utc",
            "TIME.HOUR:request.receive.time.hour_utc",
            "TIME.SECOND:request.receive.time.second_utc",
            "TIME.EPOCH:request.receive.time.epoch",
        })
        public void setValueLong(final String name, final Long value) {
            longResults.put(name, value);
        }

        public Map<String, Long> getLongResults() {
            return longResults;
        }

    }

    private static final String LOG_FORMAT = "%h %a %A %l %u %t \"%r\" " +
            "%>s %b %p \"%q\" \"%{Referer}i\" %D \"%{User-agent}i\" " +
            "\"%{Cookie}i\" " +
            "\"%{Set-Cookie}o\" " +
            "\"%{If-None-Match}i\" \"%{Etag}o\"";

    private static final String COOKIES_LINE =
            "127.0.0.1 127.0.0.1 127.0.0.1 - - [31/Dec/2012:23:00:44 -0700] \"GET /index.php HTTP/1.1\" " +
            "200 - 80 \"\" \"-\" 80991 \"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" " +
            "\"jquery-ui-theme=Eggplant; Apache=127.0.0.1.1351111543699529\" " +
            "\"" +
                "NBA-0=, " +
                "NBA-1=1111, " +
                "NBA-2=2222; expires=Wed, 01-Jan-2020 00:00:12 GMT, " +
                "NBA-3=3333; expires=Wed, 01-Jan-2020 00:00:13 GMT; path=/, " +
                "NBA-4=4444; expires=Wed, 01-Jan-2020 00:00:14 GMT; path=/; domain=.basj.es" +
            "\" \"-\" \"-\"";

    // ----------------------------

    @Test
    void testEmptyRecordPossibles() {
        Parser<EmptyTestRecord> parser = new HttpdLoglineParser<>(EmptyTestRecord.class, LOG_FORMAT);

        List<String> possibles = parser.getPossiblePaths();
        for (String possible : possibles) {
            System.out.println(possible);
        }
    }

    // ---------------

    @Test
    void testRecordPossibles() {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOG_FORMAT);

        List<String> possibles = parser.getPossiblePaths();
        for (String possible : possibles) {
            System.out.println(possible);
        }
    }

    // ---------------

    private void check(String expect, Map<String, String> results, String parameter) {
        assertEquals(expect, results.get(parameter));
    }


    @Test
    void cookiesTest() throws Exception {

        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, LOG_FORMAT);

        TestRecord record = new TestRecord();
        parser.parse(record, COOKIES_LINE);

        // ---------------

        Map<String, String> results = record.getResults();
        Map<String, Long> longResults = record.getLongResults();

        // System.out.println(results.toString());

        assertEquals(null, results.get("QUERYSTRING:request.firstline.uri.query.foo"));
        assertEquals("127.0.0.1", results.get("IP:connection.client.ip"));
        assertEquals(null, results.get("NUMBER:connection.client.logname"));
        assertEquals(null, results.get("STRING:connection.client.user"));
        assertEquals("31/Dec/2012:23:00:44 -0700", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("1357020044000", results.get("TIME.EPOCH:request.receive.time.epoch"));
        assertEquals(Long.valueOf(1357020044000L), longResults.get("TIME.EPOCH:request.receive.time.epoch"));

        assertEquals("2012", results.get("TIME.YEAR:request.receive.time.year"));
        assertEquals("12", results.get("TIME.MONTH:request.receive.time.month"));
        assertEquals("December", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("31", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals(Long.valueOf(31), longResults.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals(Long.valueOf(23), longResults.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("44", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals(Long.valueOf(44), longResults.get("TIME.SECOND:request.receive.time.second"));

        assertEquals("2013", results.get("TIME.YEAR:request.receive.time.year_utc"));
        assertEquals("1", results.get("TIME.MONTH:request.receive.time.month_utc"));
        assertEquals("January", results.get("TIME.MONTHNAME:request.receive.time.monthname_utc"));
        assertEquals("1", results.get("TIME.DAY:request.receive.time.day_utc"));
        assertEquals(Long.valueOf(1), longResults.get("TIME.DAY:request.receive.time.day_utc"));
        assertEquals("6", results.get("TIME.HOUR:request.receive.time.hour_utc"));
        assertEquals(Long.valueOf(6), longResults.get("TIME.HOUR:request.receive.time.hour_utc"));
        assertEquals("44", results.get("TIME.SECOND:request.receive.time.second_utc"));
        assertEquals(Long.valueOf(44), longResults.get("TIME.SECOND:request.receive.time.second_utc"));


        assertEquals("/index.php", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));

        // The "-" value means "Not specified" which is mapped to the setter being called
        // with a 'null' value intending to say "We know it is not there".
        assertTrue(results.containsKey("BYTESCLF:response.body.bytes"));
        assertEquals(null, results.get("BYTESCLF:response.body.bytes"));
        assertTrue(longResults.containsKey("BYTESCLF:response.body.bytes"));
        assertEquals(null, longResults.get("BYTESCLF:response.body.bytes"));

        assertEquals(null, results.get("HTTP.URI:request.referer"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                     results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("80991", results.get("MICROSECONDS:response.server.processing.time"));
        assertEquals(null, results.get("HTTP.HEADER:response.header.etag"));

        assertEquals("Eggplant", results.get("HTTP.COOKIE:request.cookies.jquery-ui-theme"));
        assertEquals("127.0.0.1.1351111543699529", results.get("HTTP.COOKIE:request.cookies.apache"));
        assertEquals("NBA-0=, " +
                "NBA-1=1111, " +
                "NBA-2=2222; expires=Wed, 01-Jan-2020 00:00:12 GMT, " +
                "NBA-3=3333; expires=Wed, 01-Jan-2020 00:00:13 GMT; path=/, " +
                "NBA-4=4444; expires=Wed, 01-Jan-2020 00:00:14 GMT; path=/; domain=.basj.es",
                results.get("HTTP.SETCOOKIES:response.cookies"));
        assertEquals("NBA-4=4444; expires=Wed, 01-Jan-2020 00:00:14 GMT; path=/; domain=.basj.es",
                results.get("HTTP.SETCOOKIE:response.cookies.nba-4"));
        assertEquals("4444", results.get("STRING:response.cookies.nba-4.value"));

        // The returned value may be off by 1 or 2 seconds due to rounding.
        assertEquals(1577836814D, Double.parseDouble(results.get("STRING:response.cookies.nba-4.expires")), 2D);
        assertEquals("/", results.get("STRING:response.cookies.nba-4.path"));
        assertEquals(".basj.es", results.get("STRING:response.cookies.nba-4.domain"));
    }
}
