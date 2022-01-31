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
import org.junit.jupiter.api.Test;

class TestHttpUriDissector {

    @Test
    void testFullUrl1() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("http://www.example.com/some/thing/else/index.html?foofoo=bar%20bar")

            .expect("HTTP.PROTOCOL:protocol",    "http")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expect("HTTP.HOST:host",            "www.example.com")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&foofoo=bar%20bar")
            .expectAbsentString("HTTP.REF:ref")
            .checkExpectations();
    }

    @Test
    void testFullUrl2() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("http://www.example.com/some/thing/else/index.html&aap=noot?foofoo=barbar&")

            .expect("HTTP.PROTOCOL:protocol",    "http")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expect("HTTP.HOST:host",            "www.example.com")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=barbar&")
            .expectAbsentString("HTTP.REF:ref")
            .checkExpectations();
    }

    @Test
    void testFullUrl3() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("http://www.example.com:8080/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla")

            .expect("HTTP.PROTOCOL:protocol",    "http")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expect("HTTP.HOST:host",            "www.example.com")
            .expect("HTTP.PORT:port",            "8080")
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=barbar&")
            .expect("HTTP.REF:ref",              "blabla")
            .checkExpectations();
    }

    @Test
    void testFullUrl4() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("/some/thing/else/index.html?foofoo=barbar#blabla")

            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expectAbsentString("HTTP.HOST:host")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&foofoo=barbar")
            .expect("HTTP.REF:ref",              "blabla")
            .checkExpectations();
    }

    @Test
    void testFullUrl5() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("/some/thing/else/index.html&aap=noot?foofoo=bar%20bar&#bla%20bla")

            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expectAbsentString("HTTP.HOST:host")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/some/thing/else/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=bar%20bar&")
            .expect("HTTP.REF:ref",              "bla bla")
            .checkExpectations();
    }

    @Test
    void testAndroidApp1() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("android-app://com.google.android.googlequicksearchbox")

            .expect("HTTP.PROTOCOL:protocol",    "android-app")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expect("HTTP.HOST:host",            "com.google.android.googlequicksearchbox")
            .expectAbsentString("HTTP.PORT:port")
            .expectAbsentString("HTTP.PATH:path")
            .expectAbsentString("HTTP.QUERYSTRING:query")
            .expectAbsentString("HTTP.REF:ref")
            .checkExpectations();
    }

    @Test
    void testAndroidApp2() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("android-app://com.google.android.googlequicksearchbox/https/www.google.com")

            .expect("HTTP.PROTOCOL:protocol",    "android-app")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expect("HTTP.HOST:host",            "com.google.android.googlequicksearchbox")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/https/www.google.com")
            .expectAbsentString("HTTP.QUERYSTRING:query")
            .expectAbsentString("HTTP.REF:ref")
            .checkExpectations();
    }

    @Test
    void testBadURI() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("/some/thing/else/[index.html&aap=noot?foofoo=bar%20bar #bla%20bla ")
            // Java URI parser fails on '[' here.

            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expectAbsentString("HTTP.HOST:host")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/some/thing/else/[index.html")
            .expect("HTTP.QUERYSTRING:query",    "&aap=noot&foofoo=bar%20bar%20")
            .expect("HTTP.REF:ref",              "bla bla ")
            .checkExpectations();
    }

    @Test
    void testBadURIEncoding() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            // Java URI parser fails on 'Malformed escape pair'
            .withInput("/index.html&promo=Give-50%-discount&promo=And-do-%Another-Wrong&last=also bad %#bla%20bla ")
            //                              here ^              and here ^                   and here ^

            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expectAbsentString("HTTP.HOST:host")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&promo=Give-50%25-discount&promo=And-do-%25Another-Wrong&last=also%20bad%20%25")
            .expect("HTTP.REF:ref",              "bla bla ")
            .checkExpectations();
    }

    @Test
    void testBadURIMultiPercentEncoding() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())
            .withDissector(new QueryStringFieldDissector())

            .withInput("/index.html?Linkid=%%%3dv(%40Foo)%3d%%%&emcid=B%ar")

            .expectAbsentString("HTTP.PROTOCOL:protocol")
            .expectAbsentString("HTTP.USERINFO:userinfo")
            .expectAbsentString("HTTP.HOST:host")
            .expectAbsentString("HTTP.PORT:port")
            .expect("HTTP.PATH:path",            "/index.html")
            .expect("HTTP.QUERYSTRING:query",    "&Linkid=%25%25%3dv(%40Foo)%3d%25%25%25&emcid=B%25ar")
            .expect("STRING:query.linkid",       "%%=v(@Foo)=%%%")
            .expectAbsentString("HTTP.REF:ref")
            .checkExpectations();
    }

    @Test
    void testDoubleHashes() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())

            .withInput("https://www.basjes.nl/#foo#bar#bazz#bla#bla#")
            .withInput("https://www.basjes.nl/path/?s2a=&Referrer=ADV1234#product_title&f=API&subid=?s2a=#product_title&name=12341234")
            .withInput("https://www.basjes.nl/path/?Referrer=ADV1234#&f=API&subid=#&name=12341234")
            .withInput("https://www.basjes.nl/path?sort&#x3D;price&filter&#x3D;new&sortOrder&#x3D;asc")
            .withInput("https://www.basjes.nl/login.html?redirectUrl=https%3A%2F%2Fwww.basjes.nl%2Faccount%2Findex.html" +
                       "&_requestid=1234#x3D;12341234&Referrer&#x3D;ENTblablabla")
            .expect("HTTP.HOST:host", "www.basjes.nl")
            .checkExpectations();
    }

    @Test
    void testHTMLEntities() {
        DissectorTester.create()
            .withDissector(new HttpUriDissector())
            .withDissector(new QueryStringFieldDissector())

            .withInput("https://www.basjes.nl/?utm_campaign=aaaa&utm_source=bbbb&utm_medium=email&utm_content=&gt;&euro;&foo;%2010x%20foo%20bar")

            .expect("HTTP.HOST:host",               "www.basjes.nl")
            // Note that the bad HTML entities have been converted into something "less bad"
            .expect("HTTP.QUERYSTRING:query",       "&utm_campaign=aaaa&utm_source=bbbb&utm_medium=email&utm_content=%3E%E2%82%AC*foo;%2010x%20foo%20bar")
            .expect("STRING:query.utm_campaign",    "aaaa")
            .expect("STRING:query.utm_source",      "bbbb")
            .expect("STRING:query.utm_medium",      "email")
            .expect("STRING:query.utm_content",     ">€*foo; 10x foo bar")
            .checkExpectations();
    }

}
