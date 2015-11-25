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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Parser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BasicOverallTest {
    public static class MyRecord {

        private final List<String> results = new ArrayList<>();

        @SuppressWarnings({"unused"}) // Used via reflection
        public void setValue(final String name, final String value) {
            results.add(name + '=' + value);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(" ----------- BEGIN ----------\n");
            for (String value : results) {
                sb.append(value).append('\n');
            }
            sb.append(" ------------ END -----------\n");
            sb.append("\n");
            return sb.toString();
        }

        public void clear() {
            results.clear();
        }
    }

    private final String logformat = "\"%%\" \"%a\" \"%{c}a\" \"%A\" \"%B\" \"%b\" \"%D\" \"%f\" \"%h\" \"%H\" \"%k\" " +
            "\"%l\" \"%L\" \"%m\" \"%p\" \"%{canonical}p\" \"%{local}p\" \"%{remote}p\" \"%P\" \"%{pid}P\" \"%{tid}P\"" +
            " \"%{hextid}P\" \"%q\" \"%r\" \"%R\" \"%s\" \"%>s\" \"%t\" \"%{msec}t\" \"%{begin:msec}t\" \"%{end:msec}t" +
            "\" \"%{usec}t\" \"%{begin:usec}t\" \"%{end:usec}t\" \"%{msec_frac}t\" \"%{begin:msec_frac}t\" \"%{end:mse" +
            "c_frac}t\" \"%{usec_frac}t\" \"%{begin:usec_frac}t\" \"%{end:usec_frac}t\" \"%T\" \"%u\" \"%U\" \"%v\" \"" +
            "%V\" \"%X\" \"%I\" \"%O\" \"%{cookie}i\" \"%{set-cookie}o\" \"%{user-agent}i\" \"%{referer}i\"";

    private final String[] loglines = {
        "\"%\" \"172.17.42.1\" \"172.17.42.1\" \"172.17.0.2\" \"4880\" \"4880\" \"652\" \"/usr/share/httpd/noindex/ind" +
                "ex.html\" \"172.17.42.1\" \"HTTP/1.1\" \"0\" \"-\" \"VG9exZ0MX@uqta4OldejvQAAAAA\" \"GET\" \"80\" \"8" +
                "0\" \"80\" \"43417\" \"126\" \"126\" \"140597540726848\" \"140597540726848\" \"\" \"GET / HTTP/1.1\" " +
                "\"httpd/unix-directory\" \"403\" \"403\" \"[21/Nov/2014:15:48:21 +0000]\" \"1416584901018\" \"1416584" +
                "901018\" \"1416584901018\" \"1416584901018010\" \"1416584901018010\" \"1416584901018670\" \"018\" \"0" +
                "18\" \"018\" \"018010\" \"018010\" \"018670\" \"0\" \"-\" \"/\" \"172.17.0.2\" \"172.17.0.2\" \"+\" " +
                "\"367\" \"5188\" \"-\" \"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                " Chrome/38.0.2125.122 Safari/537.36\" \"-\"",
        "\"%\" \"172.17.42.1\" \"172.17.42.1\" \"172.17.0.2\" \"0\" \"-\" \"302\" \"/usr/share/httpd/noindex/css/boots" +
                "trap.min.css\" \"172.17.42.1\" \"HTTP/1.1\" \"1\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"43417\" " +
                "\"126\" \"126\" \"140597540726848\" \"140597540726848\" \"\" \"GET /css/bootstrap.min.css HTTP/1.1\" " +
                "\"-\" \"304\" \"304\" \"[21/Nov/2014:15:48:21 +0000]\" \"1416584901087\" \"1416584901087\" \"14165849" +
                "01087\" \"1416584901087115\" \"1416584901087115\" \"1416584901087417\" \"087\" \"087\" \"087\" \"0871" +
                "15\" \"087115\" \"087417\" \"0\" \"-\" \"/css/bootstrap.min.css\" \"172.17.0.2\" \"172.17.0.2\" \"+\"" +
                " \"448\" \"180\" \"-\" \"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                " Chrome/38.0.2125.122 Safari/537.36\" \"http://172.17.0.2/\"",
        "\"%\" \"172.17.42.1\" \"172.17.42.1\" \"172.17.0.2\" \"0\" \"-\" \"373\" \"/usr/share/httpd/noindex/css/open-" +
                "sans.css\" \"172.17.42.1\" \"HTTP/1.1\" \"0\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"43418\" \"12" +
                "7\" \"127\" \"140597540726848\" \"140597540726848\" \"\" \"GET /css/open-sans.css HTTP/1.1\" \"-\" \"" +
                "304\" \"304\" \"[21/Nov/2014:15:48:21 +0000]\" \"1416584901087\" \"1416584901087\" \"1416584901087\" " +
                "\"1416584901087430\" \"1416584901087430\" \"1416584901087803\" \"087\" \"087\" \"087\" \"087430\" \"0" +
                "87430\" \"087803\" \"0\" \"-\" \"/css/open-sans.css\" \"172.17.0.2\" \"172.17.0.2\" \"+\" \"444\" \"1" +
                "81\" \"-\" \"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0" +
                ".2125.122 Safari/537.36\" \"http://172.17.0.2/\"",
        "\"%\" \"172.17.42.1\" \"172.17.42.1\" \"172.17.0.2\" \"0\" \"-\" \"381\" \"/usr/share/httpd/noindex/images/ap" +
                "ache_pb.gif\" \"172.17.42.1\" \"HTTP/1.1\" \"0\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"43419\" " +
                "\"128\" \"128\" \"140597540726848\" \"140597540726848\" \"\" \"GET /images/apache_pb.gif HTTP/1.1\" " +
                "\"-\" \"304\" \"304\" \"[21/Nov/2014:15:48:21 +0000]\" \"1416584901087\" \"1416584901087\" \"14165849" +
                "01087\" \"1416584901087445\" \"1416584901087445\" \"1416584901087826\" \"087\" \"087\" \"087\" \"0874" +
                "45\" \"087445\" \"087826\" \"0\" \"-\" \"/images/apache_pb.gif\" \"172.17.0.2\" \"172.17.0.2\" \"+\" " +
                "\"448\" \"180\" \"-\" \"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/38.0.2125.122 Safari/537.36\" \"http://172.17.0.2/\"",
        "\"%\" \"172.17.42.1\" \"172.17.42.1\" \"172.17.0.2\" \"0\" \"-\" \"269\" \"/usr/share/httpd/noindex/images/po" +
                "weredby.png\" \"172.17.42.1\" \"HTTP/1.1\" \"1\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"43419\" " +
                "\"128\" \"128\" \"140597540726848\" \"140597540726848\" \"\" \"GET /images/poweredby.png HTTP/1.1\" " +
                "\"-\" \"304\" \"304\" \"[21/Nov/2014:15:48:21 +0000]\" \"1416584901091\" \"1416584901091\" \"14165849" +
                "01091\" \"1416584901091601\" \"1416584901091601\" \"1416584901091870\" \"091\" \"091\" \"091\" \"0916" +
                "01\" \"091601\" \"091870\" \"0\" \"-\" \"/images/poweredby.png\" \"172.17.0.2\" \"172.17.0.2\" \"+\" " +
                "\"448\" \"179\" \"-\" \"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/38.0.2125.122 Safari/537.36\" \"http://172.17.0.2/\"",
        "\"%\" \"172.17.42.1\" \"172.17.42.1\" \"172.17.0.2\" \"213\" \"213\" \"448\" \"/var/www/html/ladkshjfkjasdhf" +
                "\" \"172.17.42.1\" \"HTTP/1.1\" \"0\" \"-\" \"-\" \"GET\" \"80\" \"80\" \"80\" \"43482\" \"136\" \"13" +
                "6\" \"140597540726848\" \"140597540726848\" \"\" \"GET /ladkshjfkjasdhf HTTP/1.1\" \"-\" \"404\" \"40" +
                "4\" \"[21/Nov/2014:15:50:45 +0000]\" \"1416585045231\" \"1416585045231\" \"1416585045231\" \"14165850" +
                "45231085\" \"1416585045231085\" \"1416585045231533\" \"231\" \"231\" \"231\" \"231085\" \"231085\" \"" +
                "231533\" \"0\" \"-\" \"/ladkshjfkjasdhf\" \"172.17.0.2\" \"172.17.0.2\" \"+\" \"356\" \"429\" \"-\" " +
                "\"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 S" +
                "afari/537.36\" \"-\"",
    };


    @Test
    public void testBasicParsing() throws Exception {
        Parser<MyRecord> parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);
        MyRecord         record = new MyRecord();

        List<String> paths = parser.getPossiblePaths();

        parser.addParseTarget(record.getClass().getMethod("setValue", String.class, String.class), paths);

        for (String logline : loglines) {
            record.clear();
            parser.parse(record, logline);
            System.out.println(record.toString());
        }
    }

}
