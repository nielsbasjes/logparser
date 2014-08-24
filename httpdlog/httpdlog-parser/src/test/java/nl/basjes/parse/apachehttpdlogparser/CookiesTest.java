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

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CookiesTest {

    public class EmptyTestRecord {
    }

    public class TestRecord {

        private final Map<String, String> results     = new HashMap<>(32);
        private final Map<String, Long>   longResults = new HashMap<>(32);

        @Field({
            "HTTP.QUERYSTRING:request.firstline.uri.query",
            "IP:connection.client.ip",
            "NUMBER:connection.client.logname",
            "STRING:connection.client.user",
            "TIME.STAMP:request.receive.time",
            "TIME.SECOND:request.receive.time.second",
            "HTTP.URI:request.firstline.uri",
            "STRING:request.status.last",
            "BYTES:response.body.bytesclf",
            "HTTP.URI:request.referer",
            "HTTP.USERAGENT:request.user-agent",
            "TIME.DAY:request.receive.time.day",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.MONTHNAME:request.receive.time.monthname",
            "MICROSECONDS:server.process.time",
            "STRING:request.status.last",
            "HTTP.HEADER:response.header.etag",

            // Cookies
            "HTTP.COOKIES:request.cookies" ,
            "HTTP.COOKIE:request.cookies.apache" ,
            "HTTP.COOKIE:request.cookies.jquery-ui-theme",
            "HTTP.SETCOOKIES:response.cookies" ,
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
        @Field({
                "TIME.SECOND:request.receive.time.second",
// FIXME:                "BYTES:response.body.bytesclf",
                "TIME.DAY:request.receive.time.day",
                "TIME.HOUR:request.receive.time.hour",
        })

        public void setValueLong(final String name, final Long value) {
            longResults.put(name, value);
        }

        public Map<String, Long> getLongResults() {
            return longResults;
        }

    }

    private final String logformat = "%h %a %A %l %u %t \"%r\" " +
            "%>s %b %p \"%q\" \"%{Referer}i\" %D \"%{User-agent}i\" " +
            "\"%{Cookie}i\" " +
            "\"%{Set-Cookie}o\" " +
            "\"%{If-None-Match}i\" \"%{Etag}o\"";

    private final String cookiesLine =
            "127.0.0.1 127.0.0.1 127.0.0.1 - - [24/Oct/2012:23:00:44 +0200] \"GET /index.php HTTP/1.1\" " +
            "200 13 80 \"\" \"-\" 80991 \"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" " +
            "\"jquery-ui-theme=Eggplant; Apache=127.0.0.1.1351111543699529\" " +
            "\"" +
                "NBA-1=1234, " +
                "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT, " +
                "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/, " +
                "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/; domain=.basj.es" +
            "\" \"-\" \"-\"";

    // ----------------------------

    @Test
    public void testEmptyRecordPossibles() throws Exception {
        Parser<EmptyTestRecord> parser = new ApacheHttpdLoglineParser<>(EmptyTestRecord.class, logformat);

        List<String> possibles = parser.getPossiblePaths();
        for (String possible : possibles) {
            System.out.println(possible);
        }
    }

    // ---------------

    @Test
    public void testRecordPossibles() throws Exception {
        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logformat);

        List<String> possibles = parser.getPossiblePaths();
        for (String possible : possibles) {
            System.out.println(possible);
        }
    }

    // ---------------

    @Test
    public void cookiesTest() throws Exception {

        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logformat);

        TestRecord record = new TestRecord();
        parser.parse(record, cookiesLine);

        // ---------------

        Map<String, String> results = record.getResults();
        Map<String, Long> longResults = record.getLongResults();

        // System.out.println(results.toString());

        assertEquals(null, results.get("QUERYSTRING:request.firstline.uri.query.foo"));
        assertEquals("127.0.0.1", results.get("IP:connection.client.ip"));
        assertEquals(null, results.get("NUMBER:connection.client.logname"));
        assertEquals(null, results.get("STRING:connection.client.user"));
        assertEquals("[24/Oct/2012:23:00:44 +0200]", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("October", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("24", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals(new Long(24), longResults.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals(new Long(23), longResults.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("44", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals(new Long(44), longResults.get("TIME.SECOND:request.receive.time.second"));

        assertEquals("/index.php", results.get("HTTP.URI:request.firstline.uri"));
        assertEquals("200", results.get("STRING:request.status.last"));
        assertEquals("13", results.get("BYTES:response.body.bytesclf"));
        assertEquals(null, results.get("HTTP.URI:request.referer"));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0",
                     results.get("HTTP.USERAGENT:request.user-agent"));
        assertEquals("80991", results.get("MICROSECONDS:server.process.time"));
        assertEquals(null, results.get("HTTP.HEADER:response.header.etag"));

        assertEquals("Eggplant", results.get("HTTP.COOKIE:request.cookies.jquery-ui-theme"));
        assertEquals("127.0.0.1.1351111543699529", results.get("HTTP.COOKIE:request.cookies.apache"));
        assertEquals("NBA-1=1234, " +
                "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT, " +
                "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/, " +
                "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/; domain=.basj.es",
                results.get("HTTP.SETCOOKIES:response.cookies"));
        assertEquals("NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/; domain=.basj.es",
                results.get("HTTP.SETCOOKIE:response.cookies.nba-4"));
        assertEquals("1234", results.get("STRING:response.cookies.nba-4.value"));

        // The returned value may be off by 1 or 2 seconds due to rounding.
        assertEquals(1577836810D, Double.parseDouble(results.get("STRING:response.cookies.nba-4.expires")), 2D);
        assertEquals("/", results.get("STRING:response.cookies.nba-4.path"));
        assertEquals(".basj.es", results.get("STRING:response.cookies.nba-4.domain"));

    }
}
