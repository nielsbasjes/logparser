/*
 * Apache HTTPD logparsing made easy
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

import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;

public class NginxLogFormatTest {

    @Test
    public void testBasicLogFormat() {
        // From: http://articles.slicehost.com/2010/8/27/customizing-nginx-web-logs
        String logFormat = "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"";
        String logLine = "123.65.150.10 - - [23/Aug/2010:03:50:59 +0000] \"POST /wordpress3/wp-admin/admin-ajax.php HTTP/1.1\" 200 2 \"http://www.example.com/wordpress3/wp-admin/post-new.php\" \"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.25 Safari/534.3\"";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)

            .printPossible()
            .printAllPossibleValues();

    }

}
