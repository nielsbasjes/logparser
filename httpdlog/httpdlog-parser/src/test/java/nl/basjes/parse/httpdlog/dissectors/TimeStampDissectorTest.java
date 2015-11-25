/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.httpdlog.HttpdLogFormatDissector;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TimeStampDissectorTest {

    public class TestRecord {

        private final Map<String, String> results     = new HashMap<>(32);
        private final Map<String, Long>   longResults = new HashMap<>(32);

        @SuppressWarnings({"unused"}) // Used via reflection
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

        @SuppressWarnings({"unused"}) // Used via reflection
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
            Dissector httpdLogFormatDissector = new HttpdLogFormatDissector("%t");
            addDissector(httpdLogFormatDissector);
            addDissector(new TimeStampDissector());
            setRootType(httpdLogFormatDissector.getInputType());
        }
    }

    @Test
    public void testTimeStampDissector() throws Exception {
        TimeParser timeParser = new TimeParser();
        TestRecord record = new TestRecord();
        timeParser.parse(record, "[31/Dec/2012:23:00:44 -0700]");

        Map<String, String> results = record.results;
        Map<String, Long> longResults = record.longResults;

        assertEquals("31/Dec/2012:23:00:44 -0700", results.get("TIME.STAMP:request.receive.time"));
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

    // FIXME: Implement this.
    @Ignore
    @Test
    public void testSpecialTimeFormat() throws Exception {
        String logformat = "%{%Y-%m-%dT%H:%M:%S%z}t";

        Parser<TestRecord> parser = new ApacheHttpdLoglineParser<>(TestRecord.class, logformat);

        TestRecord record = new TestRecord();
        parser.parse(record, "2012-12-31T23:00:44-0700");

        Map<String, String> results = record.results;
        Map<String, Long> longResults = record.longResults;

        assertEquals("31/Dec/2012:23:00:44 -0700", results.get("TIME.STAMP:request.receive.time"));

        // Unix time
        assertEquals("1357020044000", results.get("TIME.EPOCH:request.receive.time.epoch"));
        assertEquals(new Long(1357020044000L), longResults.get("TIME.EPOCH:request.receive.time.epoch"));

        // Local
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

        // UTC
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
