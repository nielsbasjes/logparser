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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

public class TestHttpUriDissector {

    @Test
    public void testFullUrl1() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("http://www.example.com/some/thing/else/index.html?foofoo=bar%20bar")

            .expect("HTTP.PROTOCOL:protocol",    "http")
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            "www.example.com")
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&foofoo=bar%20bar")
            .expect("HTTP.REF:ref",              (String) null)
            .checkExpectations();
    }

    @Test
    public void testFullUrl2() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("http://www.example.com/some/thing/else/index.html&aap=noot?foofoo=barbar&")

            .expect("HTTP.PROTOCOL:protocol",    "http")
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            "www.example.com")
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=barbar&")
            .expect("HTTP.REF:ref",              (String) null)
            .checkExpectations();
    }

    @Test
    public void testFullUrl3() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("http://www.example.com:8080/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla")

            .expect("HTTP.PROTOCOL:protocol",    "http")
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            "www.example.com")
            .expect("HTTP.PORT:port",            "8080")
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=barbar&")
            .expect("HTTP.REF:ref",              "blabla")
            .checkExpectations();
    }

    @Test
    public void testFullUrl4() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("/some/thing/else/index.html?foofoo=barbar#blabla")

            .expect("HTTP.PROTOCOL:protocol",    (String) null)
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            (String) null)
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&foofoo=barbar")
            .expect("HTTP.REF:ref",              "blabla")
            .checkExpectations();
    }

    @Test
    public void testFullUrl5() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("/some/thing/else/index.html&aap=noot?foofoo=bar%20bar&#bla%20bla")

            .expect("HTTP.PROTOCOL:protocol",    (String) null)
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            (String) null)
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=bar%20bar&")
            .expect("HTTP.REF:ref",              "bla bla")
            .checkExpectations();
    }

    @Test
    public void testAndroidApp1() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("android-app://com.google.android.googlequicksearchbox")

            .expect("HTTP.PROTOCOL:protocol",    "android-app")
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            "com.google.android.googlequicksearchbox")
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "")
            .expect("HTTP.QUERYSTRING:query",    "")
            .expect("HTTP.REF:ref",              (String) null)
            .checkExpectations();
    }

    @Test
    public void testAndroidApp2() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("android-app://com.google.android.googlequicksearchbox/https/www.google.com")

            .expect("HTTP.PROTOCOL:protocol",    "android-app")
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            "com.google.android.googlequicksearchbox")
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/https/www.google.com")
            .expect("HTTP.QUERYSTRING:query",    "")
            .expect("HTTP.REF:ref",              (String) null)
            .checkExpectations();
    }

    @Test
    public void testBadURI() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("/some/thing/else/[index.html&aap=noot?foofoo=bar%20bar #bla%20bla ")
            // Java URI parser fails on '[' here.

            .expect("HTTP.PROTOCOL:protocol",    (String) null)
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            (String) null)
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/some/thing/else/[index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=bar%20bar%20")
            .expect("HTTP.REF:ref",              "bla bla ")
            .checkExpectations();
    }

    @Test
    public void testBadURIEncoding() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            // Java URI parser fails on 'Malformed escape pair'
            .withInput("/index.html&promo=Give-50%-discount&promo=And-do-%Another-Wrong&last=also bad %#bla%20bla ")
            //                              here ^              and here ^                   and here ^

            .expect("HTTP.PROTOCOL:protocol",    (String) null)
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            (String) null)
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&promo=Give-50%25-discount&promo=And-do-%25Another-Wrong&last=also%20bad%20%25")
            .expect("HTTP.REF:ref",              "bla bla ")
            .checkExpectations();
    }

    @Test
    public void testBadURIMultiPercentEncoding() throws Exception {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())
            .withDissector(new QueryStringFieldDissector())

            .withInput("/index.html?Linkid=%%%3dv(%40Foo)%3d%%%&emcid=B%ar")

            .expect("HTTP.PROTOCOL:protocol",    (String) null)
            .expect("HTTP.USERINFO:userinfo",    (String) null)
            .expect("HTTP.HOST:host",            (String) null)
            .expect("HTTP.PORT:port",            (String) null)
            .expect("HTTP.PATH:path",            "/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&Linkid=%25%25%3dv(%40Foo)%3d%25%25%25&emcid=B%25ar")
            .expect("STRING:query.linkid",       "%%=v(@Foo)=%%%")
            .expect("HTTP.REF:ref",              (String) null)
            .checkExpectations();
    }

}
