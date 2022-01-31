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
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// CHECKSTYLE.OFF: LineLength
public class NginxLogFormatTest {

    private static final Logger LOG = LoggerFactory.getLogger(NginxLogFormatTest.class);

    @Test
    void testBasicLogFormat() {
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

    @Test
    void testBasicLogFormatDissector() {
        // From: http://articles.slicehost.com/2010/8/27/customizing-nginx-web-logs
        String logFormat = "combined";
        String logLine = "123.65.150.10 - - [23/Aug/2010:03:50:59 +0000] \"POST /wordpress3/wp-admin/admin-ajax.php HTTP/1.1\" 200 2 \"http://www.example.com/wordpress3/wp-admin/post-new.php\" \"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.25 Safari/534.3\"";

        HttpdLoglineParser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, "$msec");
        parser.dropDissector(HttpdLogFormatDissector.class);

        NginxHttpdLogFormatDissector dissector = new NginxHttpdLogFormatDissector();
        dissector.setLogFormat(logFormat);
        parser.addDissector(dissector);

        DissectorTester.create()
            .verbose()
            .withParser(parser)
            .withInput(logLine)
            .printPossible()
            .printAllPossibleValues();
    }

    @Test
    void testBasicLogFormatWithUnknownField() {
        // $remote_user_age is fake and doesn't exist.
        String logFormat = "$foobar $remote_user_age $remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"";
        String logLine = "something 42 123.65.150.10 - - [23/Aug/2010:03:50:59 +0000] \"POST /wordpress3/wp-admin/admin-ajax.php HTTP/1.1\" 200 2 \"http://www.example.com/wordpress3/wp-admin/post-new.php\" \"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.25 Safari/534.3\"";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)
            .expect("UNKNOWN_NGINX_VARIABLE:nginx.unknown.foobar", "something")
            .expect("UNKNOWN_NGINX_VARIABLE:nginx.unknown.remote_user_age", "42")
            .checkExpectations()
            .printPossible()
            .printAllPossibleValues();
    }


    @Test
    void testCompareApacheAndNginxOutput() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {

        String logFormatNginx  = "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"";

        // combined format except the logname was removed.
        String logFormatApache = "%h - %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";

        String logLine = "1.2.3.4 - - [23/Aug/2010:03:50:59 +0000] \"POST /foo.html?aap&noot=mies HTTP/1.1\" 200 2 \"http://www.example.com/bar.html?wim&zus=jet\" \"Niels Basjes/1.0\"";

        Parser<TestRecord> apacheParser = new HttpdLoglineParser<>(TestRecord.class, logFormatApache);

        String[] fieldsArray = {
            "HTTP.URI:request.referer",
            "HTTP.PROTOCOL:request.referer.protocol",
            "HTTP.USERINFO:request.referer.userinfo",
            "HTTP.HOST:request.referer.host",
            "HTTP.PORT:request.referer.port",
            "HTTP.PATH:request.referer.path",
            "HTTP.QUERYSTRING:request.referer.query",
            "STRING:request.referer.query.*",
            "HTTP.REF:request.referer.ref",
            "TIME.STAMP:request.receive.time",
            "TIME.DAY:request.receive.time.day",
            "TIME.MONTHNAME:request.receive.time.monthname",
            "TIME.MONTH:request.receive.time.month",
            "TIME.WEEK:request.receive.time.weekofweekyear",
            "TIME.YEAR:request.receive.time.weekyear",
            "TIME.YEAR:request.receive.time.year",
            "TIME.HOUR:request.receive.time.hour",
            "TIME.MINUTE:request.receive.time.minute",
            "TIME.SECOND:request.receive.time.second",
            "TIME.MILLISECOND:request.receive.time.millisecond",
            "TIME.DATE:request.receive.time.date",
            "TIME.TIME:request.receive.time.time",
            "TIME.ZONE:request.receive.time.timezone",
            "TIME.EPOCH:request.receive.time.epoch",
            "TIME.DAY:request.receive.time.day_utc",
            "TIME.MONTHNAME:request.receive.time.monthname_utc",
            "TIME.MONTH:request.receive.time.month_utc",
            "TIME.WEEK:request.receive.time.weekofweekyear_utc",
            "TIME.YEAR:request.receive.time.weekyear_utc",
            "TIME.YEAR:request.receive.time.year_utc",
            "TIME.HOUR:request.receive.time.hour_utc",
            "TIME.MINUTE:request.receive.time.minute_utc",
            "TIME.SECOND:request.receive.time.second_utc",
            "TIME.MILLISECOND:request.receive.time.millisecond_utc",
            "TIME.DATE:request.receive.time.date_utc",
            "TIME.TIME:request.receive.time.time_utc",
            "BYTESCLF:response.body.bytes",
            "BYTES:response.body.bytes",
            "STRING:request.status.last",
            "HTTP.USERAGENT:request.user-agent",
            "HTTP.FIRSTLINE:request.firstline",
            "HTTP.METHOD:request.firstline.method",
            "HTTP.URI:request.firstline.uri",
            "HTTP.PROTOCOL:request.firstline.uri.protocol",
            "HTTP.USERINFO:request.firstline.uri.userinfo",
            "HTTP.HOST:request.firstline.uri.host",
            "HTTP.PORT:request.firstline.uri.port",
            "HTTP.PATH:request.firstline.uri.path",
            "HTTP.QUERYSTRING:request.firstline.uri.query",
            "STRING:request.firstline.uri.query.*",
            "HTTP.REF:request.firstline.uri.ref",
            "HTTP.PROTOCOL_VERSION:request.firstline.protocol",
            "HTTP.PROTOCOL:request.firstline.protocol",
            "HTTP.PROTOCOL.VERSION:request.firstline.protocol.version",
            "IP:connection.client.host"
        };

        List<String> fields = Arrays.asList(fieldsArray); // apacheParser.getPossiblePaths();

        ArrayList<String> checkFields = new ArrayList<>(fields.size() + 10);
        String[] parameterNames = {"aap", "noot", "mies", "wim", "zus", "jet" };
        for (String field: fields) {
            if (field.endsWith(".*")){
                String fieldHead = field.substring(0, field.length()-1);
                for (String parameterName: parameterNames){
                    checkFields.add(fieldHead + parameterName);
                }
            } else {
                checkFields.add(field);
            }
        }

        Parser<TestRecord> nginxParser = new HttpdLoglineParser<>(TestRecord.class, logFormatNginx);
        for (String field: checkFields) {
            apacheParser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), field);
            nginxParser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), field);
        }

        LOG.info("Running Apache parser");
        TestRecord apacheRecord = apacheParser.parse(logLine);
        LOG.info("Running Nginx parser");
        TestRecord nginxRecord  = nginxParser.parse(logLine);

        for (String field: checkFields) {
            boolean apacheHasValue = apacheRecord.hasStringValue(field);
            boolean nginxHasValue = nginxRecord.hasStringValue(field);
            assertEquals(apacheHasValue, nginxHasValue, "Apache and Nginx values for field " + field + " are different.");

            if (apacheRecord.hasStringValue(field)) {
                String apacheValue = apacheRecord.getStringValue(field);
                String nginxValue = nginxRecord.getStringValue(field);
                assertEquals(apacheValue, nginxValue, "Apache and Nginx values for field " + field + " are different.");
            }

        }

    }


    @Test
    void testFullTestAllFields() {
        String logFormat =
                "# \"$status\" " +
                "# \"$time_iso8601\" " +
                "# \"$time_local\" " +
                "# \"$arg_name\" " +
                "# \"$args\" " +
                "# \"$query_string\" " +
                "# \"$binary_remote_addr\" " +
                "# \"$body_bytes_sent\" " +
                "# \"$bytes_sent\" " +
                "# \"$connection\" " +
                "# \"$connection_requests\" " +
                "# \"$content_length\" " +
                "# \"$content_type\" " +
                "# \"$cookie_name\" " +
                "# \"$document_root\" " +
                "# \"$host\" " +
                "# \"$hostname\" " +
                "# \"$http_name\" " +
                "# \"$https\" " +
                "# \"$is_args\" " +
                "# \"$limit_rate\" " +
                "# \"$msec\" " +
                "# \"$nginx_version\" " +
                "# \"$pid\" " +
                "# \"$pipe\" " +
                "# \"$proxy_protocol_addr\" " +
                "# \"$realpath_root\" " +
                "# \"$remote_addr\" " +
                "# \"$remote_port\" " +
                "# \"$remote_user\" " +
                "# \"$request\" " +
                "# \"$request_body\" " +
                "# \"$request_body_file\" " +
                "# \"$request_completion\" " +
                "# \"$request_filename\" " +
                "# \"$request_length\" " +
                "# \"$request_method\" " +
                "# \"$request_time\" " +
                "# \"$request_uri\" " +
                "# \"$scheme\" " +
                "# \"$sent_http_etag\" " +
                "# \"$sent_http_last_modified\" " +
                "# \"$server_addr\" " +
                "# \"$server_name\" " +
                "# \"$server_port\" " +
                "# \"$server_protocol\" " +
                "# \"$tcpinfo_rtt\" " +
                "# \"$tcpinfo_rttvar\" " +
                "# \"$tcpinfo_snd_cwnd\" " +
                "# \"$tcpinfo_rcv_space\" " +
                "# \"$uri\" " +
                "# \"$document_uri\" " +
                "# \"$http_user_agent\" " +
                "# \"$http_referer\" " +
                "#";
        String logLine =
              /* $status                  */  "# \"200\" " +
              /* $time_iso8601            */  "# \"2017-01-03T15:56:36+01:00\" " +
              /* $time_local              */  "# \"03/Jan/2017:15:56:36 +0100\" " +
              /* $arg_name                */  "# \"-\" " +
              /* $args                    */  "# \"aap&noot=&mies=wim\" " +
              /* $query_string            */  "# \"aap&noot=&mies=wim\" " +
              /* $binary_remote_addr      */  "# \"\\x7F\\x00\\x00\\x01\" " +
              /* $body_bytes_sent         */  "# \"436\" " +
              /* $bytes_sent              */  "# \"694\" " +
              /* $connection              */  "# \"5\" " +
              /* $connection_requests     */  "# \"4\" " +
              /* $content_length          */  "# \"-\" " +
              /* $content_type            */  "# \"-\" " +
              /* $cookie_name             */  "# \"-\" " +
              /* $document_root           */  "# \"/var/www/html\" " +
              /* $host                    */  "# \"localhost\" " +
              /* $hostname                */  "# \"hackbox\" " +
              /* $http_name               */  "# \"-\" " +
              /* $https                   */  "# \"\" " +
              /* $is_args                 */  "# \"?\" " +
              /* $limit_rate              */  "# \"0\" " +
              /* $msec                    */  "# \"1483455396.639\" " +
              /* $nginx_version           */  "# \"1.10.0\" " +
              /* $pid                     */  "# \"5137\" " +
              /* $pipe                    */  "# \".\" " +
              /* $proxy_protocol_addr     */  "# \"\" " +
              /* $realpath_root           */  "# \"/var/www/html\" " +
              /* $remote_addr             */  "# \"127.0.0.1\" " +
              /* $remote_port             */  "# \"44448\" " +
              /* $remote_user             */  "# \"-\" " +
              /* $request                 */  "# \"GET /?aap&noot=&mies=wim HTTP/1.1\" " +
              /* $request_body            */  "# \"-\" " +
              /* $request_body_file       */  "# \"-\" " +
              /* $request_completion      */  "# \"OK\" " +
              /* $request_filename        */  "# \"/var/www/html/index.html\" " +
              /* $request_length          */  "# \"491\" " +
              /* $request_method          */  "# \"GET\" " +
              /* $request_time            */  "# \"0.000\" " +
              /* $request_uri             */  "# \"/?aap&noot=&mies=wim\" " +
              /* $scheme                  */  "# \"http\" " +
              /* $sent_http_etag          */  "# \"W/\\x22586bbb8b-29e\\x22\" " +
              /* $sent_http_last_modified */  "# \"Tue, 03 Jan 2017 14:56:11 GMT\" " +
              /* $server_addr             */  "# \"127.0.0.1\" " +
              /* $server_name             */  "# \"_\" " +
              /* $server_port             */  "# \"80\" " +
              /* $server_protocol         */  "# \"HTTP/1.1\" " +
              /* $tcpinfo_rtt             */  "# \"52\" " +
              /* $tcpinfo_rttvar          */  "# \"30\" " +
              /* $tcpinfo_snd_cwnd        */  "# \"10\" " +
              /* $tcpinfo_rcv_space       */  "# \"43690\" " +
              /* $uri                     */  "# \"/index.html\" " +
              /* $document_uri            */  "# \"/index.html\" " +
              /* $http_user_agent         */  "# \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36\" " +
              /* $http_referer            */  "# \"http://localhost/\" " +
                                              "#";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)

//            .printPossible()
            .printAllPossibleValues();
    }

    private static class SingleFieldTestcase {
        final String logformat;
        final String logline;
        final String fieldName;
        final String expectedValue;

        SingleFieldTestcase(String logformat, String logline, String fieldName, String expectedValue) {
            this.logformat = logformat;
            this.logline = logline;
            this.fieldName = fieldName;
            this.expectedValue = expectedValue;
        }
    }

    @Test
    public void validateAllFields() {
        List<SingleFieldTestcase> fieldsTests = new ArrayList<>();

        fieldsTests.add(new SingleFieldTestcase("$status",                   "200",                                 "STRING:request.status.last",         "200"));
        fieldsTests.add(new SingleFieldTestcase("$time_iso8601",             "2017-01-03T15:56:36+01:00",           "TIME.ISO8601:request.receive.time", "2017-01-03T15:56:36+01:00"));
        fieldsTests.add(new SingleFieldTestcase("$time_local",               "03/Jan/2017:15:56:36 +0100",          "TIME.STAMP:request.receive.time",    "03/Jan/2017:15:56:36 +0100"));

        fieldsTests.add(new SingleFieldTestcase("$time_iso8601",             "2017-01-03T15:56:36+01:00",           "TIME.EPOCH:request.receive.time.epoch",   "1483455396000"));
        fieldsTests.add(new SingleFieldTestcase("$time_local",               "03/Jan/2017:15:56:36 +0100",          "TIME.EPOCH:request.receive.time.epoch",   "1483455396000"));
        fieldsTests.add(new SingleFieldTestcase("$msec",                     "1483455396.639",                      "TIME.EPOCH:request.receive.time.epoch",   "1483455396639"));

        fieldsTests.add(new SingleFieldTestcase("$remote_addr",              "127.0.0.1",                           "IP:connection.client.host",            "127.0.0.1"));
        fieldsTests.add(new SingleFieldTestcase("$binary_remote_addr",       "\\x7F\\x00\\x00\\x01",                "IP_BINARY:connection.client.host",     "\\x7F\\x00\\x00\\x01"));
        fieldsTests.add(new SingleFieldTestcase("$binary_remote_addr",       "\\x7F\\x00\\x00\\x01",                "IP:connection.client.host",            "127.0.0.1"));

        fieldsTests.add(new SingleFieldTestcase("$remote_port",              "44448",                               "PORT:connection.client.port",        "44448"));
        fieldsTests.add(new SingleFieldTestcase("$remote_user",              "-",                                   "STRING:connection.client.user",      null));

        fieldsTests.add(new SingleFieldTestcase("$is_args",                  "?",                                   "STRING:request.firstline.uri.is_args",   "?"));
        fieldsTests.add(new SingleFieldTestcase("$query_string",             "aap&noot=&mies=wim",                  "HTTP.QUERYSTRING:request.firstline.uri.query", "aap&noot=&mies=wim"));
        fieldsTests.add(new SingleFieldTestcase("$args",                     "aap&noot=&mies=wim",                  "HTTP.QUERYSTRING:request.firstline.uri.query", "aap&noot=&mies=wim"));
        fieldsTests.add(new SingleFieldTestcase("$args",                     "aap&noot=&mies=wim",                  "STRING:request.firstline.uri.query.aap",           ""));
        fieldsTests.add(new SingleFieldTestcase("$args",                     "aap&noot=&mies=wim",                  "STRING:request.firstline.uri.query.noot",          ""));
        fieldsTests.add(new SingleFieldTestcase("$args",                     "aap&noot=&mies=wim",                  "STRING:request.firstline.uri.query.mies",          "wim"));
        fieldsTests.add(new SingleFieldTestcase("$arg_name",                 "foo",                                 "STRING:request.firstline.uri.query.name",          "foo"));

        fieldsTests.add(new SingleFieldTestcase("$bytes_sent",               "694",                                 "BYTES:response.bytes",             "694"));
        fieldsTests.add(new SingleFieldTestcase("$bytes_received",           "694",                                 "BYTES:request.bytes",             "694"));
        fieldsTests.add(new SingleFieldTestcase("$body_bytes_sent",          "436",                                 "BYTES:response.body.bytes",        "436"));
        fieldsTests.add(new SingleFieldTestcase("$connection",               "5",                                   "NUMBER:connection.serial_number",  "5"));
        fieldsTests.add(new SingleFieldTestcase("$connection_requests",      "4",                                   "NUMBER:connection.requestnr",      "4"));
        fieldsTests.add(new SingleFieldTestcase("$https",                    "",                                    "STRING:connection.https",          ""));
        fieldsTests.add(new SingleFieldTestcase("$content_length",           "-",                                   "HTTP.HEADER:request.header.content_length",                             null));
        fieldsTests.add(new SingleFieldTestcase("$content_type",             "-",                                   "HTTP.HEADER:request.header.content_type",                                 null));
        fieldsTests.add(new SingleFieldTestcase("$cookie_name",              "Something",                           "HTTP.COOKIE:request.cookies.name",                                       "Something"));

        fieldsTests.add(new SingleFieldTestcase("$document_root",            "/var/www/html",                       "STRING:request.firstline.document_root",                       "/var/www/html"));
        fieldsTests.add(new SingleFieldTestcase("$realpath_root",            "/var/www/html",                       "STRING:request.firstline.realpath_root",                       "/var/www/html"));

        fieldsTests.add(new SingleFieldTestcase("$host",                     "localhost",                           "STRING:connection.server.name",                                "localhost"));
        fieldsTests.add(new SingleFieldTestcase("$hostname",                 "hackbox",                             "STRING:connection.client.host",                                "hackbox"));
        fieldsTests.add(new SingleFieldTestcase("$http_foobar",              "Something",                           "HTTP.HEADER:request.header.foobar",                          "Something"));
        fieldsTests.add(new SingleFieldTestcase("$sent_http_foobar",         "Something",                           "HTTP.HEADER:response.header.foobar",                         "Something"));
        fieldsTests.add(new SingleFieldTestcase("$sent_trailer_foobar",      "Something",                           "HTTP.TRAILER:response.trailer.foobar",                        "Something"));
        fieldsTests.add(new SingleFieldTestcase("$nginx_version",            "1.10.0",                              "STRING:server.nginx.version",                                "1.10.0"));
        fieldsTests.add(new SingleFieldTestcase("$pid",                      "5137",                                "NUMBER:connection.server.child.processid",                   "5137"));
        fieldsTests.add(new SingleFieldTestcase("$pipe",                     ".",                                   "STRING:connection.nginx.pipe",                               "."));
        fieldsTests.add(new SingleFieldTestcase("$pipe",                     "p",                                   "STRING:connection.nginx.pipe",                               "p"));
        fieldsTests.add(new SingleFieldTestcase("$protocol",                 "TCP",                                 "STRING:connection.protocol",                                 "TCP"));
        fieldsTests.add(new SingleFieldTestcase("$proxy_protocol_addr",      "1.2.3.4",                             "IP:connection.client.proxy.host",                            "1.2.3.4"));
        fieldsTests.add(new SingleFieldTestcase("$proxy_protocol_port",      "1234",                                "PORT:connection.client.proxy.port",                            "1234"));
        fieldsTests.add(new SingleFieldTestcase("$request",                  "GET /?aap&noot=&mies=wim HTTP/1.1",   "HTTP.FIRSTLINE:request.firstline",                           "GET /?aap&noot=&mies=wim HTTP/1.1"));
        fieldsTests.add(new SingleFieldTestcase("$request_completion",       "OK",                                  "STRING:request.completion",                                  "OK"));
        fieldsTests.add(new SingleFieldTestcase("$request_filename",         "/var/www/html/index.html",            "FILENAME:server.filename",                                   "/var/www/html/index.html"));
        fieldsTests.add(new SingleFieldTestcase("$request_length",           "491",                                 "BYTES:request.bytes",                                        "491"));
        fieldsTests.add(new SingleFieldTestcase("$request_method",           "GET",                                 "HTTP.METHOD:request.firstline.method",                       "GET"));

        fieldsTests.add(new SingleFieldTestcase("$request_time",             "123.456",                             "SECOND_MILLIS:response.server.processing.time",              "123.456"));
        fieldsTests.add(new SingleFieldTestcase("$request_time",             "123.456",                             "MILLISECONDS:response.server.processing.time",               "123456"));
        fieldsTests.add(new SingleFieldTestcase("$request_time",             "123.456",                             "MICROSECONDS:response.server.processing.time",               "123456000"));

        fieldsTests.add(new SingleFieldTestcase("$request_uri",              "/?aap&noot=&mies=wim",                "HTTP.URI:request.firstline.uri",                             "/?aap&noot=&mies=wim"));
        fieldsTests.add(new SingleFieldTestcase("$scheme",                   "http",                                "HTTP.PROTOCOL:request.firstline.uri.protocol",               "http"));
        fieldsTests.add(new SingleFieldTestcase("$sent_http_etag",           "W/\\x22586bbb8b-29e\\x22",            "HTTP.HEADER:response.header.etag",                           "W/\\x22586bbb8b-29e\\x22"));
        fieldsTests.add(new SingleFieldTestcase("$sent_http_last_modified", "Tue, 03 Jan 2017 14:56:11 GMT",       "HTTP.HEADER:response.header.last_modified",                  "Tue, 03 Jan 2017 14:56:11 GMT"));
        fieldsTests.add(new SingleFieldTestcase("$server_addr",              "127.0.0.1",                           "IP:connection.server.ip",                                    "127.0.0.1"));
        fieldsTests.add(new SingleFieldTestcase("$server_name",              "_",                                   "STRING:connection.server.name",                              "_"));
        fieldsTests.add(new SingleFieldTestcase("$server_port",              "80",                                  "PORT:connection.server.port",                                "80"));
        fieldsTests.add(new SingleFieldTestcase("$server_protocol",          "HTTP/1.1",                            "HTTP.PROTOCOL_VERSION:request.firstline.protocol",           "HTTP/1.1"));
        fieldsTests.add(new SingleFieldTestcase("$server_protocol",          "HTTP/1.1",                            "HTTP.PROTOCOL:request.firstline.protocol",                   "HTTP"));
        fieldsTests.add(new SingleFieldTestcase("$server_protocol",          "HTTP/1.1",                            "HTTP.PROTOCOL.VERSION:request.firstline.protocol.version",   "1.1"));
        fieldsTests.add(new SingleFieldTestcase("$tcpinfo_rtt",              "52",                                  "MICROSECONDS:connection.tcpinfo.rtt",                        "52"));
        fieldsTests.add(new SingleFieldTestcase("$tcpinfo_rttvar",           "30",                                  "MICROSECONDS:connection.tcpinfo.rttvar",                     "30"));
        fieldsTests.add(new SingleFieldTestcase("$tcpinfo_snd_cwnd",         "10",                                  "BYTES:connection.tcpinfo.send.cwnd",                         "10"));
        fieldsTests.add(new SingleFieldTestcase("$tcpinfo_rcv_space",        "43690",                               "BYTES:connection.tcpinfo.receive.space",                     "43690"));
        fieldsTests.add(new SingleFieldTestcase("$uri",                      "/index.html",                         "HTTP.URI:request.firstline.uri.normalized",                  "/index.html"));
        fieldsTests.add(new SingleFieldTestcase("$document_uri",             "/index.html",                         "HTTP.URI:request.firstline.uri.normalized",                  "/index.html"));
        fieldsTests.add(new SingleFieldTestcase("$http_user_agent",          "Mozilla/5.0 (Foo)",                   "HTTP.USERAGENT:request.user-agent",                          "Mozilla/5.0 (Foo)"));
        fieldsTests.add(new SingleFieldTestcase("$http_foo_user_agent",      "Mozilla/5.0 (Foo)",                   "HTTP.HEADER:request.header.foo_user_agent",                  "Mozilla/5.0 (Foo)"));
        fieldsTests.add(new SingleFieldTestcase("$http_user_agent_foo",      "Mozilla/5.0 (Foo)",                   "HTTP.HEADER:request.header.user_agent_foo",                  "Mozilla/5.0 (Foo)"));
        fieldsTests.add(new SingleFieldTestcase("$http_referer",             "http://localhost/",                   "HTTP.URI:request.referer",                                   "http://localhost/"));

        // TODO: Check if these are REALLY "not intended for logging"
        fieldsTests.add(new SingleFieldTestcase("$request_body",             "-",                                   "NOT_IMPLEMENTED:nginx_parameter_not_intended_for_logging__request_body",         null));
        fieldsTests.add(new SingleFieldTestcase("$request_body_file",        "-",                                   "NOT_IMPLEMENTED:nginx_parameter_not_intended_for_logging__request_body_file",    null));
        fieldsTests.add(new SingleFieldTestcase("$limit_rate",               "0",                                   "NOT_IMPLEMENTED:nginx_parameter_not_intended_for_logging__limit_rate",             "0"));

        for (SingleFieldTestcase testCase: fieldsTests) {
            DissectorTester.create()
                .printSeparator()
                .verbose()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, testCase.logformat))
                .withInput(testCase.logline)
                .expect(testCase.fieldName, testCase.expectedValue)
                .printPossible()
                .printAllPossibleValues()
                .checkExpectations();
        }

    }

    @Test
    void validateAllFieldsPrefix() {
        List<SingleFieldTestcase> fieldsTests = new ArrayList<>();

        final String useragent = "Mozilla/5.0 (Foo)";
        fieldsTests.add(new SingleFieldTestcase("$http_user_agent",       useragent, "HTTP.USERAGENT:request.user-agent",         useragent));
        fieldsTests.add(new SingleFieldTestcase("$http_foo_user_agent",   useragent, "HTTP.HEADER:request.header.foo_user_agent", useragent));
        fieldsTests.add(new SingleFieldTestcase("$http_user_agent_foo",   useragent, "HTTP.HEADER:request.header.user_agent_foo", useragent));

        for (SingleFieldTestcase testCase: fieldsTests) {
            DissectorTester.create()
                .printSeparator()
                .verbose()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, testCase.logformat))
                .withInput(testCase.logline)
                .expect(testCase.fieldName, testCase.expectedValue)
                .printPossible()
                .printAllPossibleValues()
                .checkExpectations();
        }

    }


    @Test
    void bugReport60(){

        String logFormat = "$the_real_ip - [$the_real_ip] - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\" $request_length $request_time [$proxy_upstream_name] $upstream_addr $upstream_response_length $upstream_response_time $upstream_status $req_id";

        String logLine = "1.2.3.4 - [1.2.3.4] - - [07/Aug/2020:15:50:07 +0800] \"HEAD /ai/search/version HTTP/1.1\" 308 0 \"-\" \"curl/7.29.0\" 100 0.000 [default-ai-search-prod-svc-5009] - - - - bfb9417db656d95bcfdf3e2a7f47b1ec";

        DissectorTester.create()
            .verbose()
            .withParser(new HttpdLoglineParser<>(TestRecord.class, logFormat))
            .withInput(logLine)

            .expect("STRING:connection.client.user",                                  (String)null)
            .expect("BYTES:request.bytes",                                            "100")
            .expect("BYTESCLF:request.bytes",                                         "100")
            .expect("STRING:nginxmodule.kubernetes.req_id",                           "bfb9417db656d95bcfdf3e2a7f47b1ec")
            .expect("HTTP.URI:request.referer",                                       (String) null)
            .expectAbsentString("HTTP.PROTOCOL:request.referer.protocol")
            .expectAbsentString("HTTP.USERINFO:request.referer.userinfo")
            .expectAbsentString("HTTP.HOST:request.referer.host")
            .expectAbsentString("HTTP.PORT:request.referer.port")
            .expectAbsentString("HTTP.PATH:request.referer.path")
            .expectAbsentString("HTTP.QUERYSTRING:request.referer.query")
            .expectAbsentString("STRING:request.referer.query.*")
            .expectAbsentString("HTTP.REF:request.referer.ref")
            .expect("SECOND_MILLIS:response.server.processing.time",                  "0.000")
            .expect("MILLISECONDS:response.server.processing.time",                   "0")
            .expect("MICROSECONDS:response.server.processing.time",                   "0")
            .expect("TIME.STAMP:request.receive.time",                                "07/Aug/2020:15:50:07 +0800")
            .expect("TIME.DAY:request.receive.time.day",                              "7")
            .expect("TIME.MONTHNAME:request.receive.time.monthname",                  "August")
            .expect("TIME.MONTH:request.receive.time.month",                          "8")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear",                  "32")
            .expect("TIME.YEAR:request.receive.time.weekyear",                        "2020")
            .expect("TIME.YEAR:request.receive.time.year",                            "2020")
            .expect("TIME.HOUR:request.receive.time.hour",                            "15")
            .expect("TIME.MINUTE:request.receive.time.minute",                        "50")
            .expect("TIME.SECOND:request.receive.time.second",                        "7")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond",              "0")
            .expect("TIME.MICROSECOND:request.receive.time.microsecond",              "0")
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond",                "0")
            .expect("TIME.DATE:request.receive.time.date",                            "2020-08-07")
            .expect("TIME.TIME:request.receive.time.time",                            "15:50:07")
            .expect("TIME.ZONE:request.receive.time.timezone",                        "+08:00")
            .expect("TIME.EPOCH:request.receive.time.epoch",                          "1596786607000")
            .expect("TIME.DAY:request.receive.time.day_utc",                          "7")
            .expect("TIME.MONTHNAME:request.receive.time.monthname_utc",              "August")
            .expect("TIME.MONTH:request.receive.time.month_utc",                      "8")
            .expect("TIME.WEEK:request.receive.time.weekofweekyear_utc",              "32")
            .expect("TIME.YEAR:request.receive.time.weekyear_utc",                    "2020")
            .expect("TIME.YEAR:request.receive.time.year_utc",                        "2020")
            .expect("TIME.HOUR:request.receive.time.hour_utc",                        "7")
            .expect("TIME.MINUTE:request.receive.time.minute_utc",                    "50")
            .expect("TIME.SECOND:request.receive.time.second_utc",                    "7")
            .expect("TIME.MILLISECOND:request.receive.time.millisecond_utc",          "0")
            .expect("TIME.MICROSECOND:request.receive.time.microsecond_utc",          "0")
            .expect("TIME.NANOSECOND:request.receive.time.nanosecond_utc",            "0")
            .expect("TIME.DATE:request.receive.time.date_utc",                        "2020-08-07")
            .expect("TIME.TIME:request.receive.time.time_utc",                        "07:50:07")
            .expect("STRING:request.status.last",                                     "308")
            .expect("HTTP.USERAGENT:request.user-agent",                              "curl/7.29.0")
            .expect("IP:nginxmodule.kubernetes.the_real_ip",                          "1.2.3.4")
            .expect("HTTP.FIRSTLINE:request.firstline",                               "HEAD /ai/search/version HTTP/1.1")
            .expect("HTTP.METHOD:request.firstline.method",                           "HEAD")
            .expect("HTTP.URI:request.firstline.uri",                                 "/ai/search/version")
            .expectAbsentString("HTTP.PROTOCOL:request.firstline.uri.protocol")
            .expectAbsentString("HTTP.USERINFO:request.firstline.uri.userinfo")
            .expectAbsentString("HTTP.HOST:request.firstline.uri.host")
            .expectAbsentString("HTTP.PORT:request.firstline.uri.port")
            .expect("HTTP.PATH:request.firstline.uri.path",                           "/ai/search/version")
            .expectAbsentString("HTTP.QUERYSTRING:request.firstline.uri.query")
            .expectAbsentString("STRING:request.firstline.uri.query.*")
            .expectAbsentString("HTTP.REF:request.firstline.uri.ref")
            .expect("HTTP.PROTOCOL_VERSION:request.firstline.protocol",               "HTTP/1.1")
            .expect("HTTP.PROTOCOL:request.firstline.protocol",                       "HTTP")
            .expect("HTTP.PROTOCOL.VERSION:request.firstline.protocol.version",       "1.1")
            .expect("BYTES:response.body.bytes",                                      "0")
            .expect("BYTESCLF:response.body.bytes",                                   0L)
            .expect("STRING:nginxmodule.kubernetes.proxy_upstream_name",              "default-ai-search-prod-svc-5009")

            .expect("UPSTREAM_BYTES_LIST:nginxmodule.upstream.response.length",       (String)null)
            .expect("UPSTREAM_STATUS_LIST:nginxmodule.upstream.status",               (String)null)
            .expect("UPSTREAM_SECOND_MILLIS_LIST:nginxmodule.upstream.response.time", (String)null)
            .expect("UPSTREAM_ADDR_LIST:nginxmodule.upstream.addr",                   (String)null)
//            .printPossible()
//            .printAllPossibleValues()
            .checkExpectations();
    }
}
