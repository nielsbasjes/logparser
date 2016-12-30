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

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.httpdlog.HttpdLogFormatDissector;
import org.junit.Test;

public class TestTimeStampDissector {

    @Test
    public void testTimeStampDissector() throws Exception {
        DissectorTester.create()
            .withDissector(new TimeStampDissector())
            .withInput("31/Dec/2012:23:00:44 -0700")

            .expect("TIME.EPOCH:epoch",             "1357020044000"  )
            .expect("TIME.EPOCH:epoch",             1357020044000L   )
            .expect("TIME.YEAR:year",               "2012"           )
            .expect("TIME.YEAR:year",               2012L            )
            .expect("TIME.MONTH:month",             "12"             )
            .expect("TIME.MONTH:month",             12L              )
            .expect("TIME.MONTHNAME:monthname",     "December"       )
            .expect("TIME.DAY:day",                 "31"             )
            .expect("TIME.DAY:day",                 31L              )
            .expect("TIME.HOUR:hour",               "23"             )
            .expect("TIME.HOUR:hour",               23L              )
            .expect("TIME.MINUTE:minute",           "0"              )
            .expect("TIME.MINUTE:minute",           0L               )
            .expect("TIME.SECOND:second",           "44"             )
            .expect("TIME.SECOND:second",           44L              )
            .expect("TIME.DATE:date",               "2012-12-31"     )
            .expect("TIME.TIME:time",               "23:00:44"       )
            .expect("TIME.YEAR:year_utc",           "2013"           )
            .expect("TIME.YEAR:year_utc",           2013L            )
            .expect("TIME.MONTH:month_utc",         "1"              )
            .expect("TIME.MONTH:month_utc",         1L               )
            .expect("TIME.MONTHNAME:monthname_utc", "January"        )
            .expect("TIME.DAY:day_utc",             "1"              )
            .expect("TIME.DAY:day_utc",             1L               )
            .expect("TIME.HOUR:hour_utc",           "6"              )
            .expect("TIME.HOUR:hour_utc",           6L               )
            .expect("TIME.MINUTE:minute_utc",       "0"              )
            .expect("TIME.MINUTE:minute_utc",       0L               )
            .expect("TIME.SECOND:second_utc",       "44"             )
            .expect("TIME.SECOND:second_utc",       44L              )
            .expect("TIME.DATE:date_utc",           "2013-01-01"     )
            .expect("TIME.TIME:time_utc",           "06:00:44"       )

            .checkExpectations();
    }

    @Test
    public void testTimeStampDissectorPossibles() throws Exception {
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
    public void testStrftimeStampDissectorPossibles() throws Exception {

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{%Y-%m-%dT%H:%M:%S%z}t"))
//            .withInput("2012-12-31T23:00:44-0700")
//            .expect("TIME.EPOCH:request.receive.time.epoch", "1357020044000")

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
    public void testTimeStamUpperLowerCaseVariations() throws Exception {
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

            .expect("TIME.YEAR:year_utc",       "2016"  )
            .expect("TIME.MONTH:month_utc",     "9"     )
            .expect("TIME.DAY:day_utc",         "30"    )

            .checkExpectations();
    }

    @Test
    public void testHandlingOfNotYetImplementedSpecialTimeFormat() throws Exception {
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
    public void testSpecialTimeFormat() throws Exception {
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
    public void testSpecialTimeFormatBegin() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{begin:%Y-%m-%dT%H:%M:%S%z}t"))
            .withInput("2012-12-31T23:00:44-0700")
            .expect("TIME.EPOCH:request.receive.time.begin.epoch", "1357020044000")
            .checkExpectations();
    }

    @Test
    public void testSpecialTimeFormatEnd() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%{end:%Y-%m-%dT%H:%M:%S%z}t"))
            .withInput("2012-12-31T23:00:44-0700")
            .expect("TIME.EPOCH:request.receive.time.end.epoch", "1357020044000")
            .checkExpectations();
    }

    @Test
    public void testSpecialTimeFormatMultiFields1() throws Exception {
        String logline = "12/21/16 2016-12-21 20:50 20:50:25 08:50:25 PM Wed Wednesday Dec December 20 21 2016 Dec 20 08 356 20  8 12 50 PM 25 3 2016 +0100";
        String logformat = "%{%D %F %R %T %r %a %A %b %B %C %d %G %h %H %I %j %k %l %m %M %p %S %u %Y %z}t";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch"                    ,"1482349825000")

            .expect("TIME.ZONE:request.receive.time.timezone"                  ,(String)null)

            .expect("TIME.DATE:request.receive.time.date"                      ,"2016-12-21")
            .expect("TIME.TIME:request.receive.time.time"                      ,"20:50:25")
            .expect("TIME.YEAR:request.receive.time.year"                      ,"2016")
            .expect("TIME.MONTH:request.receive.time.month"                    ,"12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname"            ,"December")
            .expect("TIME.YEAR:request.receive.time.weekyear"                  ,"2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear"            ,"51")
            .expect("TIME.DAY:request.receive.time.day"                        ,"21")
            .expect("TIME.HOUR:request.receive.time.hour"                      ,"20")
            .expect("TIME.MINUTE:request.receive.time.minute"                  ,"50")
            .expect("TIME.SECOND:request.receive.time.second"                  ,"25")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond"        ,"0")

            .expect("TIME.DATE:request.receive.time.date_utc"                  ,"2016-12-21")
            .expect("TIME.TIME:request.receive.time.time_utc"                  ,"19:50:25")
            .expect("TIME.YEAR:request.receive.time.year_utc"                  ,"2016")
            .expect("TIME.MONTH:request.receive.time.month_utc"                ,"12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname_utc"        ,"December")
            .expect("TIME.YEAR:request.receive.time.weekyear_utc"              ,"2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear_utc"        ,"51")
            .expect("TIME.DAY:request.receive.time.day_utc"                    ,"21")
            .expect("TIME.HOUR:request.receive.time.hour_utc"                  ,"19")
            .expect("TIME.MINUTE:request.receive.time.minute_utc"              ,"50")
            .expect("TIME.SECOND:request.receive.time.second_utc"              ,"25")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc"    ,"0")

            .checkExpectations();
    }

    @Test
    public void testSpecialTimeFormatMultiFields2() throws Exception {
        String logline = "127.0.0.1 - - [22/Dec/2016:00:09:54 +0100] \"GET / HTTP/1.1\" 200 3525 \"12/22/16 2016-12-22 00:09 00:09:54 12:09:54 AM Thu Thursday Dec December 20 22 2016 Dec 00 12 357  0 12 12 09 AM 54 4 2016 +0100\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36\"";
        String logformat = "%h %l %u %t \"%r\" %>s %O \"%{%D %F %R %T %r %a %A %b %B %C %d %G %h %H %I %j %k %l %m %M %p %S %u %Y %z}t\" \"%{User-Agent}i\"";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch"                    ,"1482361794000")

            .expect("TIME.ZONE:request.receive.time.timezone"                  ,(String)null)

            .expect("TIME.DATE:request.receive.time.date"                      ,"2016-12-22")
            .expect("TIME.TIME:request.receive.time.time"                      ,"00:09:54")
            .expect("TIME.YEAR:request.receive.time.year"                      ,"2016")
            .expect("TIME.MONTH:request.receive.time.month"                    ,"12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname"            ,"December")
            .expect("TIME.YEAR:request.receive.time.weekyear"                  ,"2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear"            ,"51")
            .expect("TIME.DAY:request.receive.time.day"                        ,"22")
            .expect("TIME.HOUR:request.receive.time.hour"                      ,"0")
            .expect("TIME.MINUTE:request.receive.time.minute"                  ,"9")
            .expect("TIME.SECOND:request.receive.time.second"                  ,"54")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond"        ,"0")

            .expect("TIME.DATE:request.receive.time.date_utc"                  ,"2016-12-21")
            .expect("TIME.TIME:request.receive.time.time_utc"                  ,"23:09:54")
            .expect("TIME.YEAR:request.receive.time.year_utc"                  ,"2016")
            .expect("TIME.MONTH:request.receive.time.month_utc"                ,"12")
            .expect("TIME.MONTHNAME:request.receive.time.monthname_utc"        ,"December")
            .expect("TIME.YEAR:request.receive.time.weekyear_utc"              ,"2016")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear_utc"        ,"51")
            .expect("TIME.DAY:request.receive.time.day_utc"                    ,"21")
            .expect("TIME.HOUR:request.receive.time.hour_utc"                  ,"23")
            .expect("TIME.MINUTE:request.receive.time.minute_utc"              ,"9")
            .expect("TIME.SECOND:request.receive.time.second_utc"              ,"54")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc"    ,"0")

            .checkExpectations();
    }

    @Test
    public void testSpecialTimeLeadingSpaces() throws Exception {
        String logline = "12/21/16 2016-12-21 20:50 20:50:25 08:50:25 PM Wed Wednesday Dec December 20 21 2016 Dec 20 08 356 20  8 12 50 PM 25 3 2016 +0100";
        String logformat = "%{%D %F %R %T %r %a %A %b %B %C %d %G %h %H %I %j %k %l %m %M %p %S %u %Y %z}t";

        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .withInput(logline)
            .expect("TIME.EPOCH:request.receive.time.epoch" ,"1482349825000")
            .checkExpectations();
    }

}
