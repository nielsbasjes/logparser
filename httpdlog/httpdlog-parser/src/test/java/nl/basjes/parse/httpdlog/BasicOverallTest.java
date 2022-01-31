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

import nl.basjes.parse.core.Parser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicOverallTest {
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

    private static final String LOG_FORMAT =
            "\"%%\" \"%a\" \"%{c}a\" \"%A\" \"%B\" \"%b\" \"%D\" \"%f\" \"%h\" \"%H\" \"%k\" " +
            "\"%l\" \"%L\" \"%m\" \"%p\" \"%{canonical}p\" \"%{local}p\" \"%{remote}p\" \"%P\" \"%{pid}P\" \"%{tid}P\"" +
            " \"%{hextid}P\" \"%q\" \"%r\" \"%R\" \"%s\" \"%>s\" \"%t\" \"%{msec}t\" \"%{begin:msec}t\" \"%{end:msec}t" +
            "\" \"%{usec}t\" \"%{begin:usec}t\" \"%{end:usec}t\" \"%{msec_frac}t\" \"%{begin:msec_frac}t\" \"%{end:mse" +
            "c_frac}t\" \"%{usec_frac}t\" \"%{begin:usec_frac}t\" \"%{end:usec_frac}t\" \"%T\" \"%u\" \"%U\" \"%v\" \"" +
            "%V\" \"%X\" \"%I\" \"%O\" \"%{cookie}i\" \"%{set-cookie}o\" \"%{user-agent}i\" \"%{referer}i\"";

    private static final String[] LOG_LINES = {
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
    void testBasicParsing() throws Exception {
        Parser<MyRecord> parser = new HttpdLoglineParser<>(MyRecord.class, LOG_FORMAT);
        MyRecord         record = new MyRecord();

        List<String> paths = parser.getPossiblePaths();

        parser.addParseTarget(record.getClass().getMethod("setValue", String.class, String.class), paths);

        for (String logline : LOG_LINES) {
            record.clear();
            parser.parse(record, logline);
            System.out.println(record);
        }
    }

    @Test
    void ensureAllOutputsAreThere() {
        Parser<MyRecord> parser = new HttpdLoglineParser<>(MyRecord.class, LOG_FORMAT);
        List<String> paths = parser.getPossiblePaths(15, true); // Get the list presorted

        String pathString = paths
            .stream()
            .map(s -> String.format("%-30s: %s", s.substring(0, s.indexOf(':')), s.substring(s.indexOf(':')+1)))
            .collect(Collectors.joining("\n"));

        String expectedPaths =
            "IP                            : connection.client.host.last\n" +
            "IP                            : connection.client.host\n" +
            "IP                            : connection.client.ip.last\n" +
            "IP                            : connection.client.ip\n" +
            "NUMBER                        : connection.client.logname.last\n" +
            "NUMBER                        : connection.client.logname\n" +
            "IP                            : connection.client.peerip.last\n" +
            "IP                            : connection.client.peerip\n" +
            "PORT                          : connection.client.port.last\n" +
            "PORT                          : connection.client.port\n" +
            "STRING                        : connection.client.user.last\n" +
            "STRING                        : connection.client.user\n" +
            "NUMBER                        : connection.keepalivecount.last\n" +
            "NUMBER                        : connection.keepalivecount\n" +
            "NUMBER                        : connection.server.child.hexthreadid.last\n" +
            "NUMBER                        : connection.server.child.hexthreadid\n" +
            "NUMBER                        : connection.server.child.processid.last\n" +
            "NUMBER                        : connection.server.child.processid\n" +
            "NUMBER                        : connection.server.child.threadid.last\n" +
            "NUMBER                        : connection.server.child.threadid\n" +
            "IP                            : connection.server.ip.last\n" +
            "IP                            : connection.server.ip\n" +
            "STRING                        : connection.server.name.canonical.last\n" +
            "STRING                        : connection.server.name.canonical\n" +
            "STRING                        : connection.server.name.last\n" +
            "STRING                        : connection.server.name\n" +
            "PORT                          : connection.server.port.canonical.last\n" +
            "PORT                          : connection.server.port.canonical\n" +
            "PORT                          : connection.server.port.last\n" +
            "PORT                          : connection.server.port\n" +
            "BYTES                         : request.bytes.last\n" +
            "BYTESCLF                      : request.bytes.last\n" +
            "BYTES                         : request.bytes\n" +
            "BYTESCLF                      : request.bytes\n" +
            "HTTP.COOKIE                   : request.cookies.*\n" +
            "HTTP.COOKIE                   : request.cookies.last.*\n" +
            "HTTP.COOKIES                  : request.cookies.last\n" +
            "HTTP.COOKIES                  : request.cookies\n" +
            "STRING                        : request.errorlogid.last\n" +
            "STRING                        : request.errorlogid\n" +
            "HTTP.METHOD                   : request.firstline.method\n" +
            "HTTP.METHOD                   : request.firstline.original.method\n" +
            "HTTP.PROTOCOL.VERSION         : request.firstline.original.protocol.version\n" +
            "HTTP.PROTOCOL                 : request.firstline.original.protocol\n" +
            "HTTP.PROTOCOL_VERSION         : request.firstline.original.protocol\n" +
            "HTTP.HOST                     : request.firstline.original.uri.host\n" +
            "HTTP.PATH                     : request.firstline.original.uri.path\n" +
            "HTTP.PORT                     : request.firstline.original.uri.port\n" +
            "HTTP.PROTOCOL                 : request.firstline.original.uri.protocol\n" +
            "STRING                        : request.firstline.original.uri.query.*\n" +
            "HTTP.QUERYSTRING              : request.firstline.original.uri.query\n" +
            "HTTP.REF                      : request.firstline.original.uri.ref\n" +
            "HTTP.USERINFO                 : request.firstline.original.uri.userinfo\n" +
            "HTTP.URI                      : request.firstline.original.uri\n" +
            "HTTP.FIRSTLINE                : request.firstline.original\n" +
            "HTTP.PROTOCOL.VERSION         : request.firstline.protocol.version\n" +
            "HTTP.PROTOCOL                 : request.firstline.protocol\n" +
            "HTTP.PROTOCOL_VERSION         : request.firstline.protocol\n" +
            "HTTP.HOST                     : request.firstline.uri.host\n" +
            "HTTP.PATH                     : request.firstline.uri.path\n" +
            "HTTP.PORT                     : request.firstline.uri.port\n" +
            "HTTP.PROTOCOL                 : request.firstline.uri.protocol\n" +
            "STRING                        : request.firstline.uri.query.*\n" +
            "HTTP.QUERYSTRING              : request.firstline.uri.query\n" +
            "HTTP.REF                      : request.firstline.uri.ref\n" +
            "HTTP.USERINFO                 : request.firstline.uri.userinfo\n" +
            "HTTP.URI                      : request.firstline.uri\n" +
            "HTTP.FIRSTLINE                : request.firstline\n" +
            "STRING                        : request.handler.last\n" +
            "STRING                        : request.handler\n" +
            "HTTP.METHOD                   : request.method.last\n" +
            "HTTP.METHOD                   : request.method\n" +
            "PROTOCOL                      : request.protocol.last\n" +
            "PROTOCOL                      : request.protocol\n" +
            "STRING                        : request.querystring.*\n" +
            "STRING                        : request.querystring.last.*\n" +
            "HTTP.QUERYSTRING              : request.querystring.last\n" +
            "HTTP.QUERYSTRING              : request.querystring\n" +
            "TIME.EPOCH                    : request.receive.time.begin.msec.last\n" +
            "TIME.EPOCH                    : request.receive.time.begin.msec\n" +
            "TIME.EPOCH                    : request.receive.time.begin.msec_frac.last\n" +
            "TIME.EPOCH                    : request.receive.time.begin.msec_frac\n" +
            "TIME.EPOCH.USEC               : request.receive.time.begin.usec.last\n" +
            "TIME.EPOCH.USEC               : request.receive.time.begin.usec\n" +
            "TIME.EPOCH.USEC_FRAC          : request.receive.time.begin.usec_frac.last\n" +
            "TIME.EPOCH.USEC_FRAC          : request.receive.time.begin.usec_frac\n" +
            "TIME.DATE                     : request.receive.time.date\n" +
            "TIME.DATE                     : request.receive.time.date_utc\n" +
            "TIME.DAY                      : request.receive.time.day\n" +
            "TIME.DAY                      : request.receive.time.day_utc\n" +
            "TIME.EPOCH                    : request.receive.time.end.msec.last\n" +
            "TIME.EPOCH                    : request.receive.time.end.msec\n" +
            "TIME.EPOCH                    : request.receive.time.end.msec_frac.last\n" +
            "TIME.EPOCH                    : request.receive.time.end.msec_frac\n" +
            "TIME.EPOCH.USEC               : request.receive.time.end.usec.last\n" +
            "TIME.EPOCH.USEC               : request.receive.time.end.usec\n" +
            "TIME.EPOCH.USEC_FRAC          : request.receive.time.end.usec_frac.last\n" +
            "TIME.EPOCH.USEC_FRAC          : request.receive.time.end.usec_frac\n" +
            "TIME.EPOCH                    : request.receive.time.epoch\n" +
            "TIME.HOUR                     : request.receive.time.hour\n" +
            "TIME.HOUR                     : request.receive.time.hour_utc\n" +
            "TIME.DATE                     : request.receive.time.last.date\n" +
            "TIME.DATE                     : request.receive.time.last.date_utc\n" +
            "TIME.DAY                      : request.receive.time.last.day\n" +
            "TIME.DAY                      : request.receive.time.last.day_utc\n" +
            "TIME.EPOCH                    : request.receive.time.last.epoch\n" +
            "TIME.HOUR                     : request.receive.time.last.hour\n" +
            "TIME.HOUR                     : request.receive.time.last.hour_utc\n" +
            "TIME.MICROSECOND              : request.receive.time.last.microsecond\n" +
            "TIME.MICROSECOND              : request.receive.time.last.microsecond_utc\n" +
            "TIME.MILLISECOND              : request.receive.time.last.millisecond\n" +
            "TIME.MILLISECOND              : request.receive.time.last.millisecond_utc\n" +
            "TIME.MINUTE                   : request.receive.time.last.minute\n" +
            "TIME.MINUTE                   : request.receive.time.last.minute_utc\n" +
            "TIME.MONTH                    : request.receive.time.last.month\n" +
            "TIME.MONTH                    : request.receive.time.last.month_utc\n" +
            "TIME.MONTHNAME                : request.receive.time.last.monthname\n" +
            "TIME.MONTHNAME                : request.receive.time.last.monthname_utc\n" +
            "TIME.NANOSECOND               : request.receive.time.last.nanosecond\n" +
            "TIME.NANOSECOND               : request.receive.time.last.nanosecond_utc\n" +
            "TIME.SECOND                   : request.receive.time.last.second\n" +
            "TIME.SECOND                   : request.receive.time.last.second_utc\n" +
            "TIME.TIME                     : request.receive.time.last.time\n" +
            "TIME.TIME                     : request.receive.time.last.time_utc\n" +
            "TIME.ZONE                     : request.receive.time.last.timezone\n" +
            "TIME.WEEK                     : request.receive.time.last.weekofweekyear\n" +
            "TIME.WEEK                     : request.receive.time.last.weekofweekyear_utc\n" +
            "TIME.YEAR                     : request.receive.time.last.weekyear\n" +
            "TIME.YEAR                     : request.receive.time.last.weekyear_utc\n" +
            "TIME.YEAR                     : request.receive.time.last.year\n" +
            "TIME.YEAR                     : request.receive.time.last.year_utc\n" +
            "TIME.STAMP                    : request.receive.time.last\n" +
            "TIME.MICROSECOND              : request.receive.time.microsecond\n" +
            "TIME.MICROSECOND              : request.receive.time.microsecond_utc\n" +
            "TIME.MILLISECOND              : request.receive.time.millisecond\n" +
            "TIME.MILLISECOND              : request.receive.time.millisecond_utc\n" +
            "TIME.MINUTE                   : request.receive.time.minute\n" +
            "TIME.MINUTE                   : request.receive.time.minute_utc\n" +
            "TIME.MONTH                    : request.receive.time.month\n" +
            "TIME.MONTH                    : request.receive.time.month_utc\n" +
            "TIME.MONTHNAME                : request.receive.time.monthname\n" +
            "TIME.MONTHNAME                : request.receive.time.monthname_utc\n" +
            "TIME.EPOCH                    : request.receive.time.msec.last\n" +
            "TIME.EPOCH                    : request.receive.time.msec\n" +
            "TIME.EPOCH                    : request.receive.time.msec_frac.last\n" +
            "TIME.EPOCH                    : request.receive.time.msec_frac\n" +
            "TIME.NANOSECOND               : request.receive.time.nanosecond\n" +
            "TIME.NANOSECOND               : request.receive.time.nanosecond_utc\n" +
            "TIME.SECOND                   : request.receive.time.second\n" +
            "TIME.SECOND                   : request.receive.time.second_utc\n" +
            "TIME.TIME                     : request.receive.time.time\n" +
            "TIME.TIME                     : request.receive.time.time_utc\n" +
            "TIME.ZONE                     : request.receive.time.timezone\n" +
            "TIME.EPOCH.USEC               : request.receive.time.usec.last\n" +
            "TIME.EPOCH.USEC               : request.receive.time.usec\n" +
            "TIME.EPOCH.USEC_FRAC          : request.receive.time.usec_frac.last\n" +
            "TIME.EPOCH.USEC_FRAC          : request.receive.time.usec_frac\n" +
            "TIME.WEEK                     : request.receive.time.weekofweekyear\n" +
            "TIME.WEEK                     : request.receive.time.weekofweekyear_utc\n" +
            "TIME.YEAR                     : request.receive.time.weekyear\n" +
            "TIME.YEAR                     : request.receive.time.weekyear_utc\n" +
            "TIME.YEAR                     : request.receive.time.year\n" +
            "TIME.YEAR                     : request.receive.time.year_utc\n" +
            "TIME.STAMP                    : request.receive.time\n" +
            "HTTP.HOST                     : request.referer.host\n" +
            "HTTP.HOST                     : request.referer.last.host\n" +
            "HTTP.PATH                     : request.referer.last.path\n" +
            "HTTP.PORT                     : request.referer.last.port\n" +
            "HTTP.PROTOCOL                 : request.referer.last.protocol\n" +
            "STRING                        : request.referer.last.query.*\n" +
            "HTTP.QUERYSTRING              : request.referer.last.query\n" +
            "HTTP.REF                      : request.referer.last.ref\n" +
            "HTTP.USERINFO                 : request.referer.last.userinfo\n" +
            "HTTP.URI                      : request.referer.last\n" +
            "HTTP.PATH                     : request.referer.path\n" +
            "HTTP.PORT                     : request.referer.port\n" +
            "HTTP.PROTOCOL                 : request.referer.protocol\n" +
            "STRING                        : request.referer.query.*\n" +
            "HTTP.QUERYSTRING              : request.referer.query\n" +
            "HTTP.REF                      : request.referer.ref\n" +
            "HTTP.USERINFO                 : request.referer.userinfo\n" +
            "HTTP.URI                      : request.referer\n" +
            "PORT                          : request.server.port.canonical.last\n" +
            "PORT                          : request.server.port.canonical\n" +
            "STRING                        : request.status.last\n" +
            "STRING                        : request.status.original\n" +
            "STRING                        : request.status\n" +
            "URI                           : request.urlpath.original\n" +
            "URI                           : request.urlpath\n" +
            "HTTP.USERAGENT                : request.user-agent.last\n" +
            "HTTP.USERAGENT                : request.user-agent\n" +
            "BYTES                         : response.body.bytes.last\n" +
            "BYTESCLF                      : response.body.bytes.last\n" +
            "BYTES                         : response.body.bytes\n" +
            "BYTESCLF                      : response.body.bytes\n" +
            "BYTES                         : response.body.bytesclf\n" +
            "BYTESCLF                      : response.body.bytesclf\n" +
            "BYTES                         : response.bytes.last\n" +
            "BYTESCLF                      : response.bytes.last\n" +
            "BYTES                         : response.bytes\n" +
            "BYTESCLF                      : response.bytes\n" +
            "HTTP.CONNECTSTATUS            : response.connection.status.last\n" +
            "HTTP.CONNECTSTATUS            : response.connection.status\n" +
            "STRING                        : response.cookies.*.comment\n" +
            "STRING                        : response.cookies.*.domain\n" +
            "STRING                        : response.cookies.*.expires\n" +
            "TIME.EPOCH                    : response.cookies.*.expires\n" +
            "STRING                        : response.cookies.*.path\n" +
            "STRING                        : response.cookies.*.value\n" +
            "HTTP.SETCOOKIE                : response.cookies.*\n" +
            "STRING                        : response.cookies.last.*.comment\n" +
            "STRING                        : response.cookies.last.*.domain\n" +
            "STRING                        : response.cookies.last.*.expires\n" +
            "TIME.EPOCH                    : response.cookies.last.*.expires\n" +
            "STRING                        : response.cookies.last.*.path\n" +
            "STRING                        : response.cookies.last.*.value\n" +
            "HTTP.SETCOOKIE                : response.cookies.last.*\n" +
            "HTTP.SETCOOKIES               : response.cookies.last\n" +
            "HTTP.SETCOOKIES               : response.cookies\n" +
            "MICROSECONDS                  : response.server.processing.time.original\n" +
            "SECONDS                       : response.server.processing.time.original\n" +
            "MICROSECONDS                  : response.server.processing.time\n" +
            "SECONDS                       : response.server.processing.time\n" +
            "FILENAME                      : server.filename.last\n" +
            "FILENAME                      : server.filename\n" +
            "MICROSECONDS                  : server.process.time";

        assertEquals(expectedPaths, pathString);
    }
}
