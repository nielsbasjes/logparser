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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.httpdlog.HttpdLogFormatDissector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.ROOT;
import static java.util.Locale.UK;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// CHECKSTYLE.OFF: LineLength
class TestTimeStampDissector {

    @Test
    void ensureDefaultLocaleFollowsISOWeekFields() {
        WeekFields localeWeekFields = WeekFields.of(new TimeStampDissector().getLocale());
        WeekFields isoWeekFields    = WeekFields.ISO;
        assertEquals(localeWeekFields.getFirstDayOfWeek(), isoWeekFields.getFirstDayOfWeek());
        assertEquals(localeWeekFields.getMinimalDaysInFirstWeek(), isoWeekFields.getMinimalDaysInFirstWeek());
    }

    @Test
    void testTimeStampDissector() {
        DissectorTester.create()
            .withDissector(new TimeStampDissector())
            .withInput("31/Dec/2012:23:00:44 -0700")

            .expect("TIME.EPOCH:epoch",             "1357020044000")
            .expect("TIME.EPOCH:epoch",             1357020044000L)
            .expect("TIME.YEAR:year",               "2012")
            .expect("TIME.YEAR:year",               2012L)
            .expect("TIME.MONTH:month",             "12")
            .expect("TIME.MONTH:month",             12L)
            .expect("TIME.MONTHNAME:monthname",     "December")
            .expect("TIME.DAY:day",                 "31")
            .expect("TIME.DAY:day",                 31L)
            .expect("TIME.HOUR:hour",               "23")
            .expect("TIME.HOUR:hour",               23L)
            .expect("TIME.MINUTE:minute",           "0")
            .expect("TIME.MINUTE:minute",           0L)
            .expect("TIME.SECOND:second",           "44")
            .expect("TIME.SECOND:second",           44L)
            .expect("TIME.DATE:date",               "2012-12-31")
            .expect("TIME.TIME:time",               "23:00:44")
            .expect("TIME.ZONE:timezone",           "-07:00")
            .expect("TIME.YEAR:year_utc",           "2013")
            .expect("TIME.YEAR:year_utc",           2013L)
            .expect("TIME.MONTH:month_utc",         "1")
            .expect("TIME.MONTH:month_utc",         1L)
            .expect("TIME.MONTHNAME:monthname_utc", "January")
            .expect("TIME.DAY:day_utc",             "1")
            .expect("TIME.DAY:day_utc",             1L)
            .expect("TIME.HOUR:hour_utc",           "6")
            .expect("TIME.HOUR:hour_utc",           6L)
            .expect("TIME.MINUTE:minute_utc",       "0")
            .expect("TIME.MINUTE:minute_utc",       0L)
            .expect("TIME.SECOND:second_utc",       "44")
            .expect("TIME.SECOND:second_utc",       44L)
            .expect("TIME.DATE:date_utc",           "2013-01-01")
            .expect("TIME.TIME:time_utc",           "06:00:44")

            .checkExpectations();
    }

    @Test
    void testTimeStampDissectorPossibles() {
        DissectorTester.create()
            .withDissector(new TimeStampDissector())

            .expectPossible("TIME.EPOCH:epoch")
            .expectPossible("TIME.YEAR:year")
            .expectPossible("TIME.MONTH:month")
            .expectPossible("TIME.MONTHNAME:monthname")
            .expectPossible("TIME.DAY:day")
            .expectPossible("TIME.HOUR:hour")
            .expectPossible("TIME.MINUTE:minute")
            .expectPossible("TIME.SECOND:second")
            .expectPossible("TIME.DATE:date")
            .expectPossible("TIME.TIME:time")
            .expectPossible("TIME.ZONE:timezone")
            .expectPossible("TIME.YEAR:year_utc")
            .expectPossible("TIME.MONTH:month_utc")
            .expectPossible("TIME.MONTHNAME:monthname_utc")
            .expectPossible("TIME.DAY:day_utc")
            .expectPossible("TIME.HOUR:hour_utc")
            .expectPossible("TIME.MINUTE:minute_utc")
            .expectPossible("TIME.SECOND:second_utc")
            .expectPossible("TIME.DATE:date_utc")
            .expectPossible("TIME.TIME:time_utc")

            .checkExpectations();
    }

    @Test
    void testStrftimeStampDissectorPossibles() {

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{%Y-%m-%dT%H:%M:%S%z}t"))
            .withInput("2012-12-31T23:00:44-0700")
            .expect("TIME.EPOCH:request.receive.time.epoch", "1357020044000")

            .expectPossible("TIME.LOCALIZEDSTRING:request.receive.time")

            .expectPossible("TIME.EPOCH:request.receive.time.epoch")
            .expectPossible("TIME.YEAR:request.receive.time.year")
            .expectPossible("TIME.MONTH:request.receive.time.month")
            .expectPossible("TIME.MONTHNAME:request.receive.time.monthname")
            .expectPossible("TIME.DAY:request.receive.time.day")
            .expectPossible("TIME.HOUR:request.receive.time.hour")
            .expectPossible("TIME.MINUTE:request.receive.time.minute")
            .expectPossible("TIME.SECOND:request.receive.time.second")
            .expectPossible("TIME.DATE:request.receive.time.date")
            .expectPossible("TIME.TIME:request.receive.time.time")
            .expectPossible("TIME.ZONE:request.receive.time.timezone")
            .expectPossible("TIME.YEAR:request.receive.time.year_utc")
            .expectPossible("TIME.MONTH:request.receive.time.month_utc")
            .expectPossible("TIME.MONTHNAME:request.receive.time.monthname_utc")
            .expectPossible("TIME.DAY:request.receive.time.day_utc")
            .expectPossible("TIME.HOUR:request.receive.time.hour_utc")
            .expectPossible("TIME.MINUTE:request.receive.time.minute_utc")
            .expectPossible("TIME.SECOND:request.receive.time.second_utc")
            .expectPossible("TIME.DATE:request.receive.time.date_utc")
            .expectPossible("TIME.TIME:request.receive.time.time_utc")

            .checkExpectations();
    }


    @Test
    void testTimeStamUpperLowerCaseVariations() {
        DissectorTester.create()
            .withDissector(new TimeStampDissector())

            .withInput("30/sep/2016:00:00:06 +0000")
            .withInput("30/Sep/2016:00:00:06 +0000")
            .withInput("30/sEp/2016:00:00:06 +0000")
            .withInput("30/SEp/2016:00:00:06 +0000")
            .withInput("30/seP/2016:00:00:06 +0000")
            .withInput("30/SeP/2016:00:00:06 +0000")
            .withInput("30/sEP/2016:00:00:06 +0000")
            .withInput("30/SEP/2016:00:00:06 +0000")

            .expect("TIME.YEAR:year_utc",       "2016")
            .expect("TIME.MONTH:month_utc",     "9")
            .expect("TIME.DAY:day_utc",         "30")

            .checkExpectations();
    }

    private static Stream<Arguments> locales() {
        return Stream.of(
            Arguments.of("Root",    ROOT),
            Arguments.of("English", ENGLISH),
            Arguments.of("US",      US),
            Arguments.of("UK",      UK),
            Arguments.of("AU",      new Locale("en", "AU")),
            Arguments.of("NL",      new Locale("nl", "NL"))
        );
    }
    @ParameterizedTest(name = "Test {index}: {0} ({1})")
    @MethodSource("locales")
    void testTimeStampMonthNameVariations(String name, Locale locale) {
        DissectorTester.create()
            .withDissector(new TimeStampDissector().setLocale(locale))
            .withInput("30/jun/2016:00:00:06 +0000")
            .withInput("30/June/2016:00:00:06 +0000")
            .expect("TIME.YEAR:year_utc",       "2016")
            .expect("TIME.MONTH:month_utc",     "6")
            .expect("TIME.DAY:day_utc",         "30")
            .checkExpectations();

        DissectorTester.create()
            .withDissector(new TimeStampDissector().setLocale(locale))
            .withInput("30/jul/2016:00:00:06 +0000")
            .withInput("30/July/2016:00:00:06 +0000")
            .expect("TIME.YEAR:year_utc",       "2016")
            .expect("TIME.MONTH:month_utc",     "7")
            .expect("TIME.DAY:day_utc",         "30")
            .checkExpectations();

        DissectorTester.create()
            .withDissector(new TimeStampDissector().setLocale(locale))
            .withInput("30/sep/2016:00:00:06 +0000")
            .withInput("30/Sept/2016:00:00:06 +0000")
            .expect("TIME.YEAR:year_utc",       "2016")
            .expect("TIME.MONTH:month_utc",     "9")
            .expect("TIME.DAY:day_utc",         "30")
            .checkExpectations();

        AssertionError dissectionFailure;
        dissectionFailure = assertThrows(AssertionError.class, () -> {
            DissectorTester.create()
                .withDissector(new TimeStampDissector().setLocale(locale))
                .withInput("30/xyz/2016:00:00:06 +0000") // Intentionally bad : xyz
                .expect("TIME.YEAR:year_utc", "2016")
                .expect("TIME.MONTH:month_utc", "9")
                .expect("TIME.DAY:day_utc", "30")
                .checkExpectations();
        });
        assertTrue(dissectionFailure.getMessage().contains("could not be parsed at index"));

        dissectionFailure = assertThrows(AssertionError.class, () -> {
            DissectorTester.create()
                .withDissector(new TimeStampDissector().setLocale(locale))
                .withInput("30/sepr/2016:00:00:06 +0000") // Intentionally bad: sepr
                .expect("TIME.YEAR:year_utc", "2016")
                .expect("TIME.MONTH:month_utc", "9")
                .expect("TIME.DAY:day_utc", "30")
                .checkExpectations();
        });
        assertTrue(dissectionFailure.getMessage().contains("could not be parsed at index"));
    }

    @Test
    void testHandlingOfNotYetImplementedSpecialTimeFormat() {
        // Test both the original form and the documented workaround.
        String logformat = "%{%Y-%m-%dT%H:%M:%S%z}t | %{timestamp}i";
        String input     = "2012-12-31T23:00:44 -0700 | 2012-12-31T23:00:44 -0700";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(input)
            .expect("TIME.LOCALIZEDSTRING:request.receive.time", "2012-12-31T23:00:44 -0700")
            .expect("HTTP.HEADER:request.header.timestamp", "2012-12-31T23:00:44 -0700")
            .checkExpectations();
    }

    @Test
    void testSpecialTimeFormat() {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{%Y-%m-%dT%H:%M:%S%z}t"))

            .withInput("2012-12-31T23:00:44-0700")

            .expect("TIME.EPOCH:request.receive.time.epoch",              "1357020044000")
            .expect("TIME.EPOCH:request.receive.time.epoch",              1357020044000L)

            .expect("TIME.YEAR:request.receive.time.year",                "2012")
            .expect("TIME.YEAR:request.receive.time.year",                2012L)
            .expect("TIME.MONTH:request.receive.time.month",              "12")
            .expect("TIME.MONTH:request.receive.time.month",              12L)
            .expect("TIME.MONTHNAME:request.receive.time.monthname",      "December")
            .expect("TIME.DAY:request.receive.time.day",                  "31")
            .expect("TIME.DAY:request.receive.time.day",                  31L)
            .expect("TIME.HOUR:request.receive.time.hour",                "23")
            .expect("TIME.HOUR:request.receive.time.hour",                23L)
            .expect("TIME.MINUTE:request.receive.time.minute",            "0")
            .expect("TIME.MINUTE:request.receive.time.minute",            0L)
            .expect("TIME.SECOND:request.receive.time.second",            "44")
            .expect("TIME.SECOND:request.receive.time.second",            44L)
            .expect("TIME.DATE:request.receive.time.date",                "2012-12-31")
            .expect("TIME.TIME:request.receive.time.time",                "23:00:44")
            .expect("TIME.ZONE:request.receive.time.timezone",            "-07:00")

            .expect("TIME.YEAR:request.receive.time.year_utc",            "2013")
            .expect("TIME.YEAR:request.receive.time.year_utc",            2013L)
            .expect("TIME.MONTH:request.receive.time.month_utc",          "1")
            .expect("TIME.MONTH:request.receive.time.month_utc",          1L)
            .expect("TIME.MONTHNAME:request.receive.time.monthname_utc",  "January")
            .expect("TIME.DAY:request.receive.time.day_utc",              "1")
            .expect("TIME.DAY:request.receive.time.day_utc",              1L)
            .expect("TIME.HOUR:request.receive.time.hour_utc",            "6")
            .expect("TIME.HOUR:request.receive.time.hour_utc",            6L)
            .expect("TIME.MINUTE:request.receive.time.minute_utc",        "0")
            .expect("TIME.MINUTE:request.receive.time.minute_utc",        0L)
            .expect("TIME.SECOND:request.receive.time.second_utc",        "44")
            .expect("TIME.SECOND:request.receive.time.second_utc",        44L)
            .expect("TIME.DATE:request.receive.time.date_utc",            "2013-01-01")
            .expect("TIME.TIME:request.receive.time.time_utc",            "06:00:44")

            .checkExpectations();
    }

    @Test
    void testSpecialTimeFormatBegin() {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{begin:%Y-%m-%dT%H:%M:%S%z}t"))
            .withInput("2012-12-31T23:00:44-0700")
            .expect("TIME.EPOCH:request.receive.time.begin.epoch", "1357020044000")
            .checkExpectations();
    }

    @Test
    void testSpecialTimeFormatEnd() {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{end:%Y-%m-%dT%H:%M:%S%z}t"))
            .withInput("2012-12-31T23:00:44-0700")
            .expect("TIME.EPOCH:request.receive.time.end.epoch", "1357020044000")
            .checkExpectations();
    }

    @Test
    void testSpecialTimeFormatMultiFields1() {
        String logline = "12/21/16 2016-12-21 20:50 20:50:25 08:50:25 PM Wed Wednesday Dec December 21 2016 Dec 20 08 356 20  8 12 50 PM 1482349825 25 3 2016 +0100";
        String logformat = "%{%D %F %R %T %r %a %A %b %B %d %G %h %H %I %j %k %l %m %M %p %s %S %u %Y %z}t";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch",                    "1482349825000")

            .expect("TIME.DATE:request.receive.time.date",                      "2016-12-21")
            .expect("TIME.TIME:request.receive.time.time",                      "20:50:25")
            .expect("TIME.YEAR:request.receive.time.year",                      "2016")
            .expect("TIME.MONTH:request.receive.time.month",                    "12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname",            "December")
            .expect("TIME.YEAR:request.receive.time.weekyear",                  "2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear",            "51")
            .expect("TIME.DAY:request.receive.time.day",                        "21")
            .expect("TIME.HOUR:request.receive.time.hour",                      "20")
            .expect("TIME.MINUTE:request.receive.time.minute",                  "50")
            .expect("TIME.SECOND:request.receive.time.second",                  "25")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond",        "0")
            .expect("TIME.ZONE:request.receive.time.timezone",                  "+01:00")

            .expect("TIME.DATE:request.receive.time.date_utc",                  "2016-12-21")
            .expect("TIME.TIME:request.receive.time.time_utc",                  "19:50:25")
            .expect("TIME.YEAR:request.receive.time.year_utc",                  "2016")
            .expect("TIME.MONTH:request.receive.time.month_utc",                "12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname_utc",        "December")
            .expect("TIME.YEAR:request.receive.time.weekyear_utc",              "2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear_utc",        "51")
            .expect("TIME.DAY:request.receive.time.day_utc",                    "21")
            .expect("TIME.HOUR:request.receive.time.hour_utc",                  "19")
            .expect("TIME.MINUTE:request.receive.time.minute_utc",              "50")
            .expect("TIME.SECOND:request.receive.time.second_utc",              "25")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc",    "0")

            .checkExpectations();
    }

    @Test
    void testSpecialTimeFormatMultiFields2() {
        String logline = "127.0.0.1 - - [22/Dec/2016:00:09:54 +0100] \"GET / HTTP/1.1\" 200 3525 \"12/22/16 2016-12-22 00:09 00:09:54 12:09:54 AM Thu Thursday Dec December 22 2016 Dec 00 12 357  0 12 12 09 AM 1482361794 54 4 2016 +0100\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36\"";
        String logformat = "%h %l %u %t \"%r\" %>s %O \"%{%D %F %R %T %r %a %A %b %B %d %G %h %H %I %j %k %l %m %M %p %s %S %u %Y %z}t\" \"%{User-Agent}i\"";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch",                    "1482361794000")

            .expect("TIME.DATE:request.receive.time.date",                      "2016-12-22")
            .expect("TIME.TIME:request.receive.time.time",                      "00:09:54")
            .expect("TIME.YEAR:request.receive.time.year",                      "2016")
            .expect("TIME.MONTH:request.receive.time.month",                    "12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname",            "December")
            .expect("TIME.YEAR:request.receive.time.weekyear",                  "2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear",            "51")
            .expect("TIME.DAY:request.receive.time.day",                        "22")
            .expect("TIME.HOUR:request.receive.time.hour",                      "0")
            .expect("TIME.MINUTE:request.receive.time.minute",                  "9")
            .expect("TIME.SECOND:request.receive.time.second",                  "54")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond",        "0")
            .expect("TIME.ZONE:request.receive.time.timezone",                  "+01:00")

            .expect("TIME.DATE:request.receive.time.date_utc",                  "2016-12-21")
            .expect("TIME.TIME:request.receive.time.time_utc",                  "23:09:54")
            .expect("TIME.YEAR:request.receive.time.year_utc",                  "2016")
            .expect("TIME.MONTH:request.receive.time.month_utc",                "12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname_utc",        "December")
            .expect("TIME.YEAR:request.receive.time.weekyear_utc",              "2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear_utc",        "51")
            .expect("TIME.DAY:request.receive.time.day_utc",                    "21")
            .expect("TIME.HOUR:request.receive.time.hour_utc",                  "23")
            .expect("TIME.MINUTE:request.receive.time.minute_utc",              "9")
            .expect("TIME.SECOND:request.receive.time.second_utc",              "54")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc",    "0")

            .checkExpectations();
    }

    @Test
    void testSpecialTimeLeadingSpaces1() {
        String logline = "12/21/16 2016-12-21 20:50 20:50:25 08:50:25 PM Wed Wednesday Dec December 21 2016 Dec 20 08 356 20  8 12 50 PM 1482349825 25 3 2016 +0100";
        String logformat = "%{%D %F %R %T %r %a %A %b %B %d %G %h %H %I %j %k %l %m %M %p %s %S %u %Y %z}t";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", "1482349825000")
            .checkExpectations();
    }

    @Test
    void testSpecialTimeLeadingSpaces2a() {
        String logline = "127.0.0.1 - - [01/Jan/2017:13:01:21 +0100] \"GET / HTTP/1.1\" 200 3525 \"01/01/17 2017-01-01 13:01 13:01:21 01:01:21 PM Sun Sunday Jan January 01 2017 Jan 13 01 001 13  1 01 01 PM 21 7 2017 +0100\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36\"";
        String logformat = "%h %l %u %t \"%r\" %>s %O \"%{%D %F %R %T %r %a %A %b %B %d %G %h %H %I %j %k %l %m %M %p %S %u %Y %z}t\" \"%{User-Agent}i\"";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", "1483272081000")
            .checkExpectations();
    }

    @Test
    void testMultipleSpecialTime() {
        // As described here: http://httpd.apache.org/docs/current/mod/mod_log_config.html#examples
        // You can use the %{format}t directive multiple times to build up a time format using the extended format tokens like msec_frac:
        // Timestamp including milliseconds
        //          "%{%d/%b/%Y %T}t.%{msec_frac}t %{%z}t"

        String logline   = "01/Jan/2017 21:52:58.483 +0100";

        // The original logformat
        // String logformat = "%{%d/%b/%Y %T}t.%{msec_frac}t %{%z}t";

        // The transformation
        // logformat = logformat.replaceAll("\\}t([^%{]+)%\\{","$1");

        // The reformatted result.
        String logformat = "%{%d/%b/%Y %T.msec_frac %z}t";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", "1483303978483")
            .checkExpectations();
    }

    @Test
    void testReportedSpecialTime() {
        String logline = "28/feb/2017:03:39:40 +0800";
        String logformat = "%{%d/%b/%Y:%H:%M:%S %z}t";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", "1488224380000")
            .checkExpectations();
    }

    @Test
    void testAllStrfFieldsLowValues() {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2001, 1, 2, 3, 4, 5, 678901234), ZoneId.of("CET"));

        checkStrfField(dateTime, "%a", "Tue");           // The abbreviated name of the day of the week according to the current locale.
        checkStrfField(dateTime, "%A", "Tuesday");       // The full name of the day of the week according to the current locale.
        checkStrfField(dateTime, "%b", "Jan");           // The abbreviated month name according to the current locale.
        checkStrfField(dateTime, "%h", "Jan");           // Equivalent to %b.
        checkStrfField(dateTime, "%B", "January");       // The full month name according to the current locale.;
        checkStrfField(dateTime, "%d", "02");            // The day of the month as a decimal number (range 01 to 31).
        checkStrfField(dateTime, "%D", "01/02/01");      // Equivalent to %m/%d/%y. (Yecch—for Americans only)
        checkStrfField(dateTime, "%e", " 2");            // Like %d, the day of the month as a decimal number, but a leading zero is replaced by a space.
        checkStrfField(dateTime, "%F", "2001-01-02");    // Equivalent to %Y-%m-%d (the ISO 8601 date format).
        checkStrfField(dateTime, "%G", "2001");          // The ISO 8601 week-based year (see NOTES) with century as a decimal number.
        checkStrfField(dateTime, "%g", "01");            // Like %G, but without century, that is, with a 2-digit year (00–99).
        checkStrfField(dateTime, "%H", "03");            // The hour as a decimal number using a 24-hour clock (range 00 to 23).
        checkStrfField(dateTime, "%I", "03");            // The hour as a decimal number using a 12-hour clock (range 01 to 12).
        checkStrfField(dateTime, "%j", "002");           // The day of the year as a decimal number (range 001 to 366).
        checkStrfField(dateTime, "%k", " 3");            // The hour (24-hour clock) as a decimal number (range 0 to 23); single digits are preceded by a blank. (See also %H)
        checkStrfField(dateTime, "%l", " 3");            // The hour (12-hour clock) as a decimal number (range 1 to 12); single digits are preceded by a blank. (See also %I)
        checkStrfField(dateTime, "%m", "01");            // The month as a decimal number (range 01 to 12).
        checkStrfField(dateTime, "%M", "04");            // The minute as a decimal number (range 00 to 59).
        checkStrfField(dateTime, "%p", "AM");            // Either "AM" or "PM" according to the given time value, or the corresponding strings for the current locale. Noon is treated as "PM" and midnight as "AM".
        checkStrfField(dateTime, "%P", "am");            // Like %p but in lowercase: "am" or "pm" or a corresponding string for the current locale.
        checkStrfField(dateTime, "%r", "03:04:05 AM");   // The time in a.m. or p.m. notation. In the POSIX locale this is equivalent to %I:%M:%S %p.
        checkStrfField(dateTime, "%R", "03:04");         // The time in 24-hour notation (%H:%M). For a version including the seconds, see %T below.
        checkStrfField(dateTime, "%s", "978401045");     // The number of seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC).
        checkStrfField(dateTime, "%S", "05");            // The second as a decimal number (range 00 to 60). (The range is up to 60 to allow for occasional leap seconds)
        checkStrfField(dateTime, "%T", "03:04:05");      // The time in 24-hour notation (%H:%M:%S).
        checkStrfField(dateTime, "%u", "2");             // The day of the week as a decimal, range 1 to 7, Monday being 1. See also %w.
        checkStrfField(dateTime, "%V", "1");             // The ISO 8601 week number (see NOTES) of the current year as a decimal number, range 01 to 53, where week 1 is the first week that has at least 4 days in the new year. See also %U and %W.
        checkStrfField(dateTime, "%W", "01");            // The week number of the current year as a decimal number, range 00 to 53, starting with the first Monday as the first day of week 01.
        checkStrfField(dateTime, "%y", "01");            // The year as a decimal number without a century (range 00 to 99).
        checkStrfField(dateTime, "%Y", "2001");          // The year as a decimal number including the century.
        checkStrfField(dateTime, "%z", "+0100");         // The +hhmm or -hhmm numeric timezone.
        checkStrfField(dateTime, "%Z", "CET");           // The timezone name or abbreviation.

        checkStrfField(dateTime, "msec_frac", "678");    // Apache HTTPD specific: milliseconds fraction
        checkStrfField(dateTime, "usec_frac", "678901"); // Apache HTTPD specific: microseconds fraction
        checkStrfField(dateTime, "%F %T.msec_frac %z", "2001-01-02 03:04:05.678 +0100");
        checkStrfField(dateTime, "%F %T.usec_frac %z", "2001-01-02 03:04:05.678901 +0100");

        // With extra '%'
        checkStrfField(dateTime, "%msec_frac", "678");    // Apache HTTPD specific: milliseconds fraction
        checkStrfField(dateTime, "%usec_frac", "678901"); // Apache HTTPD specific: microseconds fraction
        checkStrfField(dateTime, "%F %T.msec_frac %z", "2001-01-02 03:04:05.678 +0100");
        checkStrfField(dateTime, "%F %T.usec_frac %z", "2001-01-02 03:04:05.678901 +0100");
    }

    @Test
    void testAllStrfFieldsHighValues() {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2017, 11, 12, 23, 14, 15, 678901234), ZoneId.of("CET"));

        checkStrfField(dateTime, "%a", "Sun");           // The abbreviated name of the day of the week according to the current locale.
        checkStrfField(dateTime, "%A", "Sunday");        // The full name of the day of the week according to the current locale.
        checkStrfField(dateTime, "%b", "Nov");           // The abbreviated month name according to the current locale.
        checkStrfField(dateTime, "%h", "Nov");           // Equivalent to %b.
        checkStrfField(dateTime, "%B", "November");      // The full month name according to the current locale.;
        checkStrfField(dateTime, "%d", "12");            // The day of the month as a decimal number (range 01 to 31).
        checkStrfField(dateTime, "%D", "11/12/17");      // Equivalent to %m/%d/%y. (Yecch—for Americans only)
        checkStrfField(dateTime, "%e", "12");            // Like %d, the day of the month as a decimal number, but a leading zero is replaced by a space.
        checkStrfField(dateTime, "%F", "2017-11-12");    // Equivalent to %Y-%m-%d (the ISO 8601 date format).
        checkStrfField(dateTime, "%G", "2017");          // The ISO 8601 week-based year (see NOTES) with century as a decimal number. The 4-digit year corresponding to the ISO week number (see %V). This has the same format and value as %Y, except that if the ISO week number belongs to the previous or next year, that year is used instead.
        checkStrfField(dateTime, "%g", "17");            // Like %G, but without century, that is, with a 2-digit year (00–99).
        checkStrfField(dateTime, "%H", "23");            // The hour as a decimal number using a 24-hour clock (range 00 to 23).
        checkStrfField(dateTime, "%I", "11");            // The hour as a decimal number using a 12-hour clock (range 01 to 12).
        checkStrfField(dateTime, "%j", "316");           // The day of the year as a decimal number (range 001 to 366).
        checkStrfField(dateTime, "%k", "23");            // The hour (24-hour clock) as a decimal number (range 0 to 23); single digits are preceded by a blank. (See also %H)
        checkStrfField(dateTime, "%l", "11");            // The hour (12-hour clock) as a decimal number (range 1 to 12); single digits are preceded by a blank. (See also %I)
        checkStrfField(dateTime, "%m", "11");            // The month as a decimal number (range 01 to 12).
        checkStrfField(dateTime, "%M", "14");            // The minute as a decimal number (range 00 to 59).
        checkStrfField(dateTime, "%p", "PM");            // Either "AM" or "PM" according to the given time value, or the corresponding strings for the current locale. Noon is treated as "PM" and midnight as "AM".
        checkStrfField(dateTime, "%P", "pm");            // Like %p but in lowercase: "am" or "pm" or a corresponding string for the current locale.
        checkStrfField(dateTime, "%r", "11:14:15 PM");   // The time in a.m. or p.m. notation. In the POSIX locale this is equivalent to %I:%M:%S %p.
        checkStrfField(dateTime, "%R", "23:14");         // The time in 24-hour notation (%H:%M). For a version including the seconds, see %T below.
        checkStrfField(dateTime, "%s", "1510524855");    // The number of seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC).
        checkStrfField(dateTime, "%S", "15");            // The second as a decimal number (range 00 to 60). (The range is up to 60 to allow for occasional leap seconds)
        checkStrfField(dateTime, "%T", "23:14:15");      // The time in 24-hour notation (%H:%M:%S).
        checkStrfField(dateTime, "%u", "7");             // The day of the week as a decimal, range 1 to 7, Monday being 1. See also %w.
        checkStrfField(dateTime, "%V", "45");            // The ISO 8601 week number (see NOTES) of the current year as a decimal number, range 01 to 53, where week 1 is the first week that has at least 4 days in the new year. See also %U and %W.
        checkStrfField(dateTime, "%W", "45");            // The week number of the current year as a decimal number, range 00 to 53, starting with the first Monday as the first day of week 01.
        checkStrfField(dateTime, "%y", "17");            // The year as a decimal number without a century (range 00 to 99).
        checkStrfField(dateTime, "%Y", "2017");          // The year as a decimal number including the century.
        checkStrfField(dateTime, "%z", "+0100");         // The +hhmm or -hhmm numeric timezone.
        checkStrfField(dateTime, "%Z", "CET");           // The timezone name or abbreviation.

        checkStrfField(dateTime, "msec_frac", "678");    // Apache HTTPD specific: milliseconds fraction
        checkStrfField(dateTime, "usec_frac", "678901"); // Apache HTTPD specific: microseconds fraction
        checkStrfField(dateTime, "%F %T.msec_frac %z", "2017-11-12 23:14:15.678 +0100");
        checkStrfField(dateTime, "%F %T.usec_frac %z", "2017-11-12 23:14:15.678901 +0100");

        // With extra '%'
        checkStrfField(dateTime, "%msec_frac", "678");    // Apache HTTPD specific: milliseconds fraction
        checkStrfField(dateTime, "%usec_frac", "678901"); // Apache HTTPD specific: microseconds fraction
        checkStrfField(dateTime, "%F %T.%msec_frac %z", "2017-11-12 23:14:15.678 +0100");
        checkStrfField(dateTime, "%F %T.%usec_frac %z", "2017-11-12 23:14:15.678901 +0100");
    }


    private void checkStrfField(ZonedDateTime dateTime, String strffield, String expected) {
        try {
            DateTimeFormatter dateTimeFormatter = StrfTimeToDateTimeFormatter.convert(strffield, ZoneId.of("CET"));
            assertNotNull(dateTimeFormatter);
            String result = dateTime.format(dateTimeFormatter);
            assertEquals(expected, result, "Incorrect field " + strffield);
        } catch (DateTimeException dte) {
            fail("DateTimeException for field " + strffield + " "+ dte.getMessage());
        }
    }

    @Test
    void ensureUnsupportedFields() {
        checkUnsupported("%c"); // The preferred date and time representation for the current locale.
        checkUnsupported("%C"); // The century number (year/100) as a 2-digit integer.
        checkUnsupported("%U"); // The week number of the current year as a decimal number, range 00 to 53, starting with the first Sunday as the first day of week 01. See also %V and %W.
        checkUnsupported("%w"); // The day of the week as a decimal, range 0 to 6, Sunday being 0. See also %u.
        checkUnsupported("%x"); // The preferred date representation for the current locale without the time.
        checkUnsupported("%X"); // The preferred time representation for the current locale without the date.
        checkUnsupported("%+"); // The date and time in date(1) format.
    }

    private void checkUnsupported(String strffield) {
        try {
            StrfTimeToDateTimeFormatter.convert(strffield);
        } catch (StrfTimeToDateTimeFormatter.UnsupportedStrfField e) {
            return; // This is actually good
        } catch (Exception e) {
            fail("Unexpected exception:" + e.getMessage());
        }
        fail("DateTimeException for field " + strffield + " should be unsupported");
    }

    @Test
    void strfTimeWithMissingTimeZone() {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{%F %H:%M:%S}t"))
            .withInput("2017-12-25 00:00:00")
            .expect("TIME.EPOCH:request.receive.time.epoch", 1514160000000L)
            .checkExpectations();

        String logformat = "%a %l %u %{%F %H:%M:%S}t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" t=%D";
        String logline = "192.168.85.3 - - 2017-12-25 00:00:00 \"GET /up.html HTTP/1.0\" 203 8 \"-\" \"HTTP-Monitor/1.1\" \"-\" t=4920";
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", 1514160000000L)
            .checkExpectations();
    }

    @Test
    void testStrfTimeMsecfrac() {
        String logformat = "%a %l %u %{%F %H:%M:%S.msec_frac}t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" t=%D";
        String logline = "192.168.85.3 - - 2017-12-25 00:00:42.123 \"GET /up.html HTTP/1.0\" 203 8 \"-\" \"HTTP-Monitor/1.1\" \"-\" t=4920";
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", 1514160042123L)
            .expect("TIME.SECOND:request.receive.time.second", 42L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond", 123000L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond", 123000000L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond_utc", 123000L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond_utc", 123000000L)
            .checkExpectations();
    }

    @Test
    void testStrfTimeMsecfracP() {
        String logformat = "%a %l %u %{%F %H:%M:%S.%msec_frac}t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" t=%D";
        String logline = "192.168.85.3 - - 2017-12-25 00:00:42.123 \"GET /up.html HTTP/1.0\" 203 8 \"-\" \"HTTP-Monitor/1.1\" \"-\" t=4920";
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", 1514160042123L)
            .expect("TIME.SECOND:request.receive.time.second", 42L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond", 123000L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond", 123000000L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond_utc", 123000L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond_utc", 123000000L)
            .checkExpectations();
    }

    @Test
    void testStrfTimeUsecfrac() {
        String logformat = "%a %l %u %{%F %H:%M:%S.usec_frac}t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" t=%D";
        String logline = "192.168.85.3 - - 2017-12-25 00:00:42.123456 \"GET /up.html HTTP/1.0\" 203 8 \"-\" \"HTTP-Monitor/1.1\" \"-\" t=4920";
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", 1514160042123L)
            .expect("TIME.SECOND:request.receive.time.second", 42L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond", 123456L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond", 123456000L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond_utc", 123456L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond_utc", 123456000L)
            .checkExpectations();
    }

    @Test
    void testStrfTimeUsecfracP() {
        String logformat = "%a %l %u %{%F %H:%M:%S.%usec_frac}t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" t=%D";
        String logline = "192.168.85.3 - - 2017-12-25 00:00:42.123456 \"GET /up.html HTTP/1.0\" 203 8 \"-\" \"HTTP-Monitor/1.1\" \"-\" t=4920";
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch", 1514160042123L)
            .expect("TIME.SECOND:request.receive.time.second", 42L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond", 123456L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond", 123456000L)
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc", 123L)
            .expect("TIME.MICROSECOND:request.receive.time.microsecond_utc", 123456L)
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond_utc", 123456000L)
            .checkExpectations();
    }

}
