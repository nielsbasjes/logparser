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

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.ApacheHttpdLogFormatDissector;
import nl.basjes.parse.httpdlog.dissectors.TimeStampDissector;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TimeStampDissectorTest {

    public class TestRecord {

        private final Map<String, String> results     = new HashMap<>(32);
        private final Map<String, Long>   longResults = new HashMap<>(32);

        @Field({
            "TIME.STAMP:request.receive.time",
            "TIME.EPOCH:request.receive.time.epoch",
            "TIME.SECOND:request.receive.time.second",
            "TIME.MINUTE:request.receive.time.minute",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.DAY:request.receive.time.day",
            "TIME.MONTH:request.receive.time.month",
            "TIME.YEAR:request.receive.time.year",
            "TIME.MONTHNAME:request.receive.time.monthname",
            "TIME.SECOND:request.receive.time.second_utc",
            "TIME.MINUTE:request.receive.time.minute_utc",
            "TIME.HOUR:request.receive.time.hour_utc",
            "TIME.DAY:request.receive.time.day_utc",
            "TIME.MONTH:request.receive.time.month_utc",
            "TIME.YEAR:request.receive.time.year_utc",
            "TIME.MONTHNAME:request.receive.time.monthname_utc"})
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        @Field({
            "TIME.DAY:request.receive.time.day",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.MINUTE:request.receive.time.minute",
            "TIME.SECOND:request.receive.time.second",
            "TIME.DAY:request.receive.time.day_utc",
            "TIME.HOUR:request.receive.time.hour_utc",
            "TIME.MINUTE:request.receive.time.minute_utc",
            "TIME.SECOND:request.receive.time.second_utc",
            "TIME.EPOCH:request.receive.time.epoch"})
        public void setValueLong(final String name, final Long value) {
            longResults.put(name, value);
        }

    }


    class TimeParser extends Parser<TestRecord> {
        public TimeParser() {
            super(TestRecord.class);
            addDissector(new ApacheHttpdLogFormatDissector("%t"));
            addDissector(new TimeStampDissector("[dd/MMM/yyyy:HH:mm:ss ZZ]"));
            setRootType("APACHELOGLINE");
        }
    }

    public void testTimeStampDissector() throws Exception {
        TimeParser timeParser = new TimeParser();
        TestRecord record = new TestRecord();
        timeParser.parse(record, "[31/Dec/2012:23:00:44 -0700]");

        Map<String, String> results = record.results;
        Map<String, Long> longResults = record.longResults;

        assertEquals("[31/Dec/2012:23:00:44 -0700]", results.get("TIME.STAMP:request.receive.time"));
        assertEquals("1357020044000", results.get("TIME.EPOCH:request.receive.time.epoch"));
        assertEquals(new Long(1357020044000L), longResults.get("TIME.EPOCH:request.receive.time.epoch"));

        assertEquals("2012", results.get("TIME.YEAR:request.receive.time.year"));
        assertEquals("12", results.get("TIME.MONTH:request.receive.time.month"));
        assertEquals("December", results.get("TIME.MONTHNAME:request.receive.time.monthname"));
        assertEquals("31", results.get("TIME.DAY:request.receive.time.day"));
        assertEquals(new Long(31), longResults.get("TIME.DAY:request.receive.time.day"));
        assertEquals("23", results.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals(new Long(23), longResults.get("TIME.HOUR:request.receive.time.hour"));
        assertEquals("0", results.get("TIME.MINUTE:request.receive.time.minute"));
        assertEquals(new Long(0), longResults.get("TIME.MINUTE:request.receive.time.minute"));
        assertEquals("44", results.get("TIME.SECOND:request.receive.time.second"));
        assertEquals(new Long(44), longResults.get("TIME.SECOND:request.receive.time.second"));

        assertEquals("2013", results.get("TIME.YEAR:request.receive.time.year_utc"));
        assertEquals("1", results.get("TIME.MONTH:request.receive.time.month_utc"));
        assertEquals("January", results.get("TIME.MONTHNAME:request.receive.time.monthname_utc"));
        assertEquals("1", results.get("TIME.DAY:request.receive.time.day_utc"));
        assertEquals(new Long(1), longResults.get("TIME.DAY:request.receive.time.day_utc"));
        assertEquals("6", results.get("TIME.HOUR:request.receive.time.hour_utc"));
        assertEquals(new Long(6), longResults.get("TIME.HOUR:request.receive.time.hour_utc"));
        assertEquals("0", results.get("TIME.MINUTE:request.receive.time.minute_utc"));
        assertEquals(new Long(0), longResults.get("TIME.MINUTE:request.receive.time.minute_utc"));
        assertEquals("44", results.get("TIME.SECOND:request.receive.time.second_utc"));
        assertEquals(new Long(44), longResults.get("TIME.SECOND:request.receive.time.second_utc"));
    }

}
