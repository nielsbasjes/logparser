/*
 * Apache HTTPD & NGINX Access log parsing made easy
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

import nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ApacheHttpdAllFieldsTest {

    private void verifyFieldAvailability(String logformat, String ... expectedFields) {

        List<String> possible = DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector(logformat))
            .getPossible();

        for (String expectedField:expectedFields) {
            assertTrue("Logformat >>>" + logformat + "<<< should produce " + expectedField + " instead we found: " + possible, possible.contains(expectedField));
        }
    }

    @Test
    public void checkDeprecationMessage() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpdLogFormatDissector("%b %D Deprecated"))
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .withInput("1 2 Deprecated")
            .expect("BYTES:response.body.bytesclf",       "1")
            .expect("MICROSECONDS:server.process.time",   "2")
            .checkExpectations();
    }

    @Test
    public void testAllFieldsAvailability() throws Exception {
        verifyFieldAvailability("%a",                   "IP:connection.client.ip",
                                                        "IP:connection.client.ip.last");
        verifyFieldAvailability("%<a",                  "IP:connection.client.ip.original" );
        verifyFieldAvailability("%>a",                  "IP:connection.client.ip.last" );

        verifyFieldAvailability("%{c}a",                "IP:connection.client.peerip",
                                                        "IP:connection.client.peerip.last");
        verifyFieldAvailability("%<{c}a",               "IP:connection.client.peerip.original" );
        verifyFieldAvailability("%>{c}a",               "IP:connection.client.peerip.last" );

        verifyFieldAvailability("%A",                   "IP:connection.server.ip",
                                                        "IP:connection.server.ip.last");
        verifyFieldAvailability("%<A",                  "IP:connection.server.ip.original" );
        verifyFieldAvailability("%>A",                  "IP:connection.server.ip.last" );

        verifyFieldAvailability("%B",                   "BYTES:response.body.bytes",
                                                        "BYTES:response.body.bytes.last");
        verifyFieldAvailability("%<B",                  "BYTES:response.body.bytes.original" );
        verifyFieldAvailability("%>B",                  "BYTES:response.body.bytes.last" );

        verifyFieldAvailability("%b Deprecated",        "BYTES:response.body.bytesclf");
        verifyFieldAvailability("%b",                   "BYTESCLF:response.body.bytes",
                                                        "BYTESCLF:response.body.bytes.last");
        verifyFieldAvailability("%<b",                  "BYTESCLF:response.body.bytes.original" );
        verifyFieldAvailability("%>b",                  "BYTESCLF:response.body.bytes.last" );

        verifyFieldAvailability("%{FooBar}C",           "HTTP.COOKIE:request.cookies.foobar");
        verifyFieldAvailability("%{FooBar}e",           "VARIABLE:server.environment.foobar");

        verifyFieldAvailability("%f",                   "FILENAME:server.filename",
                                                        "FILENAME:server.filename.last");
        verifyFieldAvailability("%<f",                  "FILENAME:server.filename.original" );
        verifyFieldAvailability("%>f",                  "FILENAME:server.filename.last" );

        verifyFieldAvailability("%h",                   "IP:connection.client.host",
                                                        "IP:connection.client.host.last");
        verifyFieldAvailability("%<h",                  "IP:connection.client.host.original" );
        verifyFieldAvailability("%>h",                  "IP:connection.client.host.last" );

        verifyFieldAvailability("%H",                   "PROTOCOL:request.protocol",
                                                        "PROTOCOL:request.protocol.last");
        verifyFieldAvailability("%<H",                  "PROTOCOL:request.protocol.original" );
        verifyFieldAvailability("%>H",                  "PROTOCOL:request.protocol.last" );

        verifyFieldAvailability("%{FooBar}i",           "HTTP.HEADER:request.header.foobar");
        verifyFieldAvailability("%{FooBar}^ti",         "HTTP.TRAILER:request.trailer.foobar");

        verifyFieldAvailability("%k",                   "NUMBER:connection.keepalivecount",
                                                        "NUMBER:connection.keepalivecount.last");
        verifyFieldAvailability("%<k",                  "NUMBER:connection.keepalivecount.original" );
        verifyFieldAvailability("%>k",                  "NUMBER:connection.keepalivecount.last" );

        verifyFieldAvailability("%l",                   "NUMBER:connection.client.logname",
                                                        "NUMBER:connection.client.logname.last");
        verifyFieldAvailability("%<l",                  "NUMBER:connection.client.logname.original" );
        verifyFieldAvailability("%>l",                  "NUMBER:connection.client.logname.last" );

        verifyFieldAvailability("%L",                   "STRING:request.errorlogid",
                                                        "STRING:request.errorlogid.last");
        verifyFieldAvailability("%<L",                  "STRING:request.errorlogid.original" );
        verifyFieldAvailability("%>L",                  "STRING:request.errorlogid.last" );

        verifyFieldAvailability("%m",                   "HTTP.METHOD:request.method",
                                                        "HTTP.METHOD:request.method.last");
        verifyFieldAvailability("%<m",                  "HTTP.METHOD:request.method.original" );
        verifyFieldAvailability("%>m",                  "HTTP.METHOD:request.method.last" );

        verifyFieldAvailability("%{FooBar}n",           "STRING:server.module_note.foobar");
        verifyFieldAvailability("%{FooBar}o",           "HTTP.HEADER:response.header.foobar");
        verifyFieldAvailability("%{FooBar}^to",         "HTTP.TRAILER:response.trailer.foobar");

        verifyFieldAvailability("%p",                   "PORT:request.server.port.canonical",
                                                        "PORT:request.server.port.canonical.last");
        verifyFieldAvailability("%<p",                  "PORT:request.server.port.canonical.original" );
        verifyFieldAvailability("%>p",                  "PORT:request.server.port.canonical.last" );

        verifyFieldAvailability("%{canonical}p",        "PORT:connection.server.port.canonical",
                                                        "PORT:connection.server.port.canonical.last");
        verifyFieldAvailability("%<{canonical}p",       "PORT:connection.server.port.canonical.original" );
        verifyFieldAvailability("%>{canonical}p",       "PORT:connection.server.port.canonical.last" );

        verifyFieldAvailability("%{local}p",            "PORT:connection.server.port",
                                                        "PORT:connection.server.port.last");
        verifyFieldAvailability("%<{local}p",           "PORT:connection.server.port.original" );
        verifyFieldAvailability("%>{local}p",           "PORT:connection.server.port.last" );

        verifyFieldAvailability("%{remote}p",           "PORT:connection.client.port",
                                                        "PORT:connection.client.port.last");
        verifyFieldAvailability("%<{remote}p",          "PORT:connection.client.port.original" );
        verifyFieldAvailability("%>{remote}p",          "PORT:connection.client.port.last" );

        verifyFieldAvailability("%P",                   "NUMBER:connection.server.child.processid",
                                                        "NUMBER:connection.server.child.processid.last");
        verifyFieldAvailability("%<P",                  "NUMBER:connection.server.child.processid.original" );
        verifyFieldAvailability("%>P",                  "NUMBER:connection.server.child.processid.last" );

        verifyFieldAvailability("%{pid}P",              "NUMBER:connection.server.child.processid",
                                                        "NUMBER:connection.server.child.processid.last");
        verifyFieldAvailability("%<{pid}P",             "NUMBER:connection.server.child.processid.original" );
        verifyFieldAvailability("%>{pid}P",             "NUMBER:connection.server.child.processid.last" );

        verifyFieldAvailability("%{tid}P",              "NUMBER:connection.server.child.threadid",
                                                        "NUMBER:connection.server.child.threadid.last");
        verifyFieldAvailability("%<{tid}P",             "NUMBER:connection.server.child.threadid.original" );
        verifyFieldAvailability("%>{tid}P",             "NUMBER:connection.server.child.threadid.last" );

        verifyFieldAvailability("%{hextid}P",           "NUMBER:connection.server.child.hexthreadid",
                                                        "NUMBER:connection.server.child.hexthreadid.last");
        verifyFieldAvailability("%<{hextid}P",          "NUMBER:connection.server.child.hexthreadid.original" );
        verifyFieldAvailability("%>{hextid}P",          "NUMBER:connection.server.child.hexthreadid.last" );

        verifyFieldAvailability("%q",                   "HTTP.QUERYSTRING:request.querystring",
                                                        "HTTP.QUERYSTRING:request.querystring.last");
        verifyFieldAvailability("%<q",                  "HTTP.QUERYSTRING:request.querystring.original" );
        verifyFieldAvailability("%>q",                  "HTTP.QUERYSTRING:request.querystring.last" );

        verifyFieldAvailability("%r",                   "HTTP.FIRSTLINE:request.firstline",
                                                        "HTTP.FIRSTLINE:request.firstline.original");
        verifyFieldAvailability("%<r",                  "HTTP.FIRSTLINE:request.firstline.original" );
        verifyFieldAvailability("%>r",                  "HTTP.FIRSTLINE:request.firstline.last" );

        verifyFieldAvailability("%R",                   "STRING:request.handler",
                                                        "STRING:request.handler.last");
        verifyFieldAvailability("%<R",                  "STRING:request.handler.original" );
        verifyFieldAvailability("%>R",                  "STRING:request.handler.last" );

        verifyFieldAvailability("%s",                   "STRING:request.status",
                                                        "STRING:request.status.original");
        verifyFieldAvailability("%<s",                  "STRING:request.status.original" );
        verifyFieldAvailability("%>s",                  "STRING:request.status.last");

        verifyFieldAvailability("%t",                   "TIME.STAMP:request.receive.time",
                                                        "TIME.STAMP:request.receive.time.last");
        verifyFieldAvailability("%<t",                  "TIME.STAMP:request.receive.time.original" );
        verifyFieldAvailability("%>t",                  "TIME.STAMP:request.receive.time.last" );

        verifyFieldAvailability("%{%Y}t",               "TIME.YEAR:request.receive.time.year");
        verifyFieldAvailability("%{begin:%Y}t",         "TIME.YEAR:request.receive.time.begin.year");
        verifyFieldAvailability("%{end:%Y}t",           "TIME.YEAR:request.receive.time.end.year");

        verifyFieldAvailability("%{sec}t",              "TIME.SECONDS:request.receive.time.sec",
                                                        "TIME.SECONDS:request.receive.time.sec");
        verifyFieldAvailability("%<{sec}t",             "TIME.SECONDS:request.receive.time.sec.original" );
        verifyFieldAvailability("%>{sec}t",             "TIME.SECONDS:request.receive.time.sec.last" );

        verifyFieldAvailability("%{begin:sec}t",        "TIME.SECONDS:request.receive.time.begin.sec",
                                                        "TIME.SECONDS:request.receive.time.begin.sec.last");
        verifyFieldAvailability("%<{begin:sec}t",       "TIME.SECONDS:request.receive.time.begin.sec.original" );
        verifyFieldAvailability("%>{begin:sec}t",       "TIME.SECONDS:request.receive.time.begin.sec.last" );

        verifyFieldAvailability("%{end:sec}t",          "TIME.SECONDS:request.receive.time.end.sec",
                                                        "TIME.SECONDS:request.receive.time.end.sec.last");
        verifyFieldAvailability("%<{end:sec}t",         "TIME.SECONDS:request.receive.time.end.sec.original" );
        verifyFieldAvailability("%>{end:sec}t",         "TIME.SECONDS:request.receive.time.end.sec.last" );

        verifyFieldAvailability("%{msec}t Deprecated",  "TIME.EPOCH:request.receive.time.begin.msec"      );
        verifyFieldAvailability("%{msec}t",             "TIME.EPOCH:request.receive.time.msec",
                                                        "TIME.EPOCH:request.receive.time.msec.last");
        verifyFieldAvailability("%<{msec}t",            "TIME.EPOCH:request.receive.time.msec.original" );
        verifyFieldAvailability("%>{msec}t",            "TIME.EPOCH:request.receive.time.msec.last" );

        verifyFieldAvailability("%{begin:msec}t",       "TIME.EPOCH:request.receive.time.begin.msec",
                                                        "TIME.EPOCH:request.receive.time.begin.msec.last");
        verifyFieldAvailability("%<{begin:msec}t",      "TIME.EPOCH:request.receive.time.begin.msec.original" );
        verifyFieldAvailability("%>{begin:msec}t",      "TIME.EPOCH:request.receive.time.begin.msec.last" );

        verifyFieldAvailability("%{end:msec}t",         "TIME.EPOCH:request.receive.time.end.msec",
                                                        "TIME.EPOCH:request.receive.time.end.msec.last");
        verifyFieldAvailability("%<{end:msec}t",        "TIME.EPOCH:request.receive.time.end.msec.original" );
        verifyFieldAvailability("%>{end:msec}t",        "TIME.EPOCH:request.receive.time.end.msec.last" );

        verifyFieldAvailability("%{usec}t Deprecated",  "TIME.EPOCH.USEC:request.receive.time.begin.usec");
        verifyFieldAvailability("%{usec}t",             "TIME.EPOCH.USEC:request.receive.time.usec",
                                                        "TIME.EPOCH.USEC:request.receive.time.usec.last");
        verifyFieldAvailability("%<{usec}t",            "TIME.EPOCH.USEC:request.receive.time.usec.original" );
        verifyFieldAvailability("%>{usec}t",            "TIME.EPOCH.USEC:request.receive.time.usec.last" );

        verifyFieldAvailability("%{begin:usec}t",       "TIME.EPOCH.USEC:request.receive.time.begin.usec",
                                                        "TIME.EPOCH.USEC:request.receive.time.begin.usec.last");
        verifyFieldAvailability("%<{begin:usec}t",      "TIME.EPOCH.USEC:request.receive.time.begin.usec.original" );
        verifyFieldAvailability("%>{begin:usec}t",      "TIME.EPOCH.USEC:request.receive.time.begin.usec.last" );

        verifyFieldAvailability("%{end:usec}t",         "TIME.EPOCH.USEC:request.receive.time.end.usec",
                                                        "TIME.EPOCH.USEC:request.receive.time.end.usec.last");
        verifyFieldAvailability("%<{end:usec}t",        "TIME.EPOCH.USEC:request.receive.time.end.usec.original" );
        verifyFieldAvailability("%>{end:usec}t",        "TIME.EPOCH.USEC:request.receive.time.end.usec.last" );

        verifyFieldAvailability("%{msec_frac}t Deprecated", "TIME.EPOCH:request.receive.time.begin.msec_frac" );
        verifyFieldAvailability("%{msec_frac}t",        "TIME.EPOCH:request.receive.time.msec_frac",
                                                        "TIME.EPOCH:request.receive.time.msec_frac.last");
        verifyFieldAvailability("%<{msec_frac}t",       "TIME.EPOCH:request.receive.time.msec_frac.original" );
        verifyFieldAvailability("%>{msec_frac}t",       "TIME.EPOCH:request.receive.time.msec_frac.last" );

        verifyFieldAvailability("%{begin:msec_frac}t",  "TIME.EPOCH:request.receive.time.begin.msec_frac",
                                                        "TIME.EPOCH:request.receive.time.begin.msec_frac.last");
        verifyFieldAvailability("%<{begin:msec_frac}t", "TIME.EPOCH:request.receive.time.begin.msec_frac.original" );
        verifyFieldAvailability("%>{begin:msec_frac}t", "TIME.EPOCH:request.receive.time.begin.msec_frac.last" );

        verifyFieldAvailability("%{end:msec_frac}t",    "TIME.EPOCH:request.receive.time.end.msec_frac",
                                                        "TIME.EPOCH:request.receive.time.end.msec_frac.last");
        verifyFieldAvailability("%<{end:msec_frac}t",   "TIME.EPOCH:request.receive.time.end.msec_frac.original" );
        verifyFieldAvailability("%>{end:msec_frac}t",   "TIME.EPOCH:request.receive.time.end.msec_frac.last" );

        verifyFieldAvailability("%{usec_frac}t Deprecated", "TIME.EPOCH.USEC_FRAC:request.receive.time.begin.usec_frac" );
        verifyFieldAvailability("%{usec_frac}t",        "TIME.EPOCH.USEC_FRAC:request.receive.time.usec_frac",
                                                        "TIME.EPOCH.USEC_FRAC:request.receive.time.usec_frac.last");
        verifyFieldAvailability("%<{usec_frac}t",       "TIME.EPOCH.USEC_FRAC:request.receive.time.usec_frac.original" );
        verifyFieldAvailability("%>{usec_frac}t",       "TIME.EPOCH.USEC_FRAC:request.receive.time.usec_frac.last" );

        verifyFieldAvailability("%{begin:usec_frac}t",  "TIME.EPOCH.USEC_FRAC:request.receive.time.begin.usec_frac",
                                                        "TIME.EPOCH.USEC_FRAC:request.receive.time.begin.usec_frac.last");
        verifyFieldAvailability("%<{begin:usec_frac}t", "TIME.EPOCH.USEC_FRAC:request.receive.time.begin.usec_frac.original" );
        verifyFieldAvailability("%>{begin:usec_frac}t", "TIME.EPOCH.USEC_FRAC:request.receive.time.begin.usec_frac.last" );

        verifyFieldAvailability("%{end:usec_frac}t",    "TIME.EPOCH.USEC_FRAC:request.receive.time.end.usec_frac",
                                                        "TIME.EPOCH.USEC_FRAC:request.receive.time.end.usec_frac.last");
        verifyFieldAvailability("%<{end:usec_frac}t",   "TIME.EPOCH.USEC_FRAC:request.receive.time.end.usec_frac.original" );
        verifyFieldAvailability("%>{end:usec_frac}t",   "TIME.EPOCH.USEC_FRAC:request.receive.time.end.usec_frac.last" );

        verifyFieldAvailability("%T",                   "SECONDS:response.server.processing.time",
                                                        "SECONDS:response.server.processing.time.original");
        verifyFieldAvailability("%<T",                  "SECONDS:response.server.processing.time.original" );
        verifyFieldAvailability("%>T",                  "SECONDS:response.server.processing.time.last" );

        verifyFieldAvailability("%D Deprecated",        "MICROSECONDS:server.process.time");
        verifyFieldAvailability("%D",                   "MICROSECONDS:response.server.processing.time",
                                                        "MICROSECONDS:response.server.processing.time.original");
        verifyFieldAvailability("%<D",                  "MICROSECONDS:response.server.processing.time.original" );
        verifyFieldAvailability("%>D",                  "MICROSECONDS:response.server.processing.time.last" );

        verifyFieldAvailability("%{us}T",               "MICROSECONDS:response.server.processing.time",
                                                        "MICROSECONDS:response.server.processing.time.original");
        verifyFieldAvailability("%<{us}T",              "MICROSECONDS:response.server.processing.time.original" );
        verifyFieldAvailability("%>{us}T",              "MICROSECONDS:response.server.processing.time.last" );

        verifyFieldAvailability("%{ms}T",               "MILLISECONDS:response.server.processing.time",
                                                        "MILLISECONDS:response.server.processing.time.original");
        verifyFieldAvailability("%<{ms}T",              "MILLISECONDS:response.server.processing.time.original" );
        verifyFieldAvailability("%>{ms}T",              "MILLISECONDS:response.server.processing.time.last" );

        verifyFieldAvailability("%{s}T",                "SECONDS:response.server.processing.time",
                                                        "SECONDS:response.server.processing.time.original");
        verifyFieldAvailability("%<{s}T",               "SECONDS:response.server.processing.time.original" );
        verifyFieldAvailability("%>{s}T",               "SECONDS:response.server.processing.time.last" );

        verifyFieldAvailability("%u",                   "STRING:connection.client.user",
                                                        "STRING:connection.client.user.last");
        verifyFieldAvailability("%<u",                  "STRING:connection.client.user.original" );
        verifyFieldAvailability("%>u",                  "STRING:connection.client.user.last" );

        verifyFieldAvailability("%U",                   "URI:request.urlpath",
                                                        "URI:request.urlpath.original");
        verifyFieldAvailability("%<U",                  "URI:request.urlpath.original" );
        verifyFieldAvailability("%>U",                  "URI:request.urlpath.last" );

        verifyFieldAvailability("%v",                   "STRING:connection.server.name.canonical",
                                                        "STRING:connection.server.name.canonical.last");
        verifyFieldAvailability("%<v",                  "STRING:connection.server.name.canonical.original" );
        verifyFieldAvailability("%>v",                  "STRING:connection.server.name.canonical.last" );

        verifyFieldAvailability("%V",                   "STRING:connection.server.name",
                                                        "STRING:connection.server.name.last");
        verifyFieldAvailability("%<V",                  "STRING:connection.server.name.original" );
        verifyFieldAvailability("%>V",                  "STRING:connection.server.name.last" );

        verifyFieldAvailability("%X",                   "HTTP.CONNECTSTATUS:response.connection.status",
                                                        "HTTP.CONNECTSTATUS:response.connection.status.last");
        verifyFieldAvailability("%<X",                  "HTTP.CONNECTSTATUS:response.connection.status.original" );
        verifyFieldAvailability("%>X",                  "HTTP.CONNECTSTATUS:response.connection.status.last" );

        verifyFieldAvailability("%I",                   "BYTES:request.bytes",
                                                        "BYTES:request.bytes.last");
        verifyFieldAvailability("%<I",                  "BYTES:request.bytes.original" );
        verifyFieldAvailability("%>I",                  "BYTES:request.bytes.last" );

        verifyFieldAvailability("%O",                   "BYTES:response.bytes",
                                                        "BYTES:response.bytes.last");
        verifyFieldAvailability("%<O",                  "BYTES:response.bytes.original" );
        verifyFieldAvailability("%>O",                  "BYTES:response.bytes.last" );

        verifyFieldAvailability("%S",                   "BYTES:total.bytes",
                                                        "BYTES:total.bytes.last");
        verifyFieldAvailability("%<S",                  "BYTES:total.bytes.original" );
        verifyFieldAvailability("%>S",                  "BYTES:total.bytes.last" );

        verifyFieldAvailability("%{cookie}i",           "HTTP.COOKIES:request.cookies",
                                                        "HTTP.COOKIES:request.cookies.last");
        verifyFieldAvailability("%<{cookie}i",          "HTTP.COOKIES:request.cookies.original" );
        verifyFieldAvailability("%>{cookie}i",          "HTTP.COOKIES:request.cookies.last" );

        verifyFieldAvailability("%{set-cookie}o",       "HTTP.SETCOOKIES:response.cookies",
                                                        "HTTP.SETCOOKIES:response.cookies.last");
        verifyFieldAvailability("%<{set-cookie}o",      "HTTP.SETCOOKIES:response.cookies.original" );
        verifyFieldAvailability("%>{set-cookie}o",      "HTTP.SETCOOKIES:response.cookies.last" );

        verifyFieldAvailability("%{user-agent}i",       "HTTP.USERAGENT:request.user-agent",
                                                        "HTTP.USERAGENT:request.user-agent.last");
        verifyFieldAvailability("%<{user-agent}i",      "HTTP.USERAGENT:request.user-agent.original" );
        verifyFieldAvailability("%>{user-agent}i",      "HTTP.USERAGENT:request.user-agent.last" );

        verifyFieldAvailability("%{referer}i",          "HTTP.URI:request.referer",
                                                        "HTTP.URI:request.referer.last");
        verifyFieldAvailability("%<{referer}i",         "HTTP.URI:request.referer.original" );
        verifyFieldAvailability("%>{referer}i",         "HTTP.URI:request.referer.last" );
    }

}
