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

import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.httpdlog.ApacheHttpdLogFormatDissector;
import org.junit.Ignore;
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

            .verbose()
            .check();
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
            .expect("TIME.YEAR:year_utc", "2016")
            .check();
    }

    @Test
    public void testHandlingOfNotYetImplementedSpecialTimeFormat() throws Exception {
        // Test both the original form and the documented workaround.
        String logformat = "%{%Y-%m-%dT%H:%M:%S%z}t | %{timestamp}i";
        String input     = "2012-12-31T23:00:44 -0700 | 2012-12-31T23:00:44 -0700";

        DissectorTester.create()
            .withDissector(new ApacheHttpdLogFormatDissector(logformat))
            .withInput(input)
            .expect("TIME.LOCALIZEDSTRING:request.receive.time", "2012-12-31T23:00:44 -0700")
            .expect("HTTP.HEADER:request.header.timestamp", "2012-12-31T23:00:44 -0700")
            .check();
    }

    // FIXME: Implement the real thing.
    @Ignore
    @Test
    public void testSpecialTimeFormat() throws Exception {
        String logformat = "%{%Y-%m-%dT%H:%M:%S%z}t";

        DissectorTester.create()
            .withDissector(new ApacheHttpdLogFormatDissector(logformat))
            .withDissector(new TimeStampDissector())
            .withInput("2012-12-31T23:00:44-0700")

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

            .verbose()
            .check();

    }


}
