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

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCookieDissector {

    @Test
    public void testRequestCookies() throws Exception {
        DissectorTester.create()
            .withDissectorUnderDummyRoot("cookies", new RequestCookieListDissector())

            .withInput("" +
                "NBA-0; " +
                "NBA-1=; " +
                "NBA-2=1234; ")

            .expect("HTTP.COOKIES:cookies", "NBA-0; NBA-1=; NBA-2=1234; ")
            .expect("HTTP.COOKIE:cookies.nba-0", "")
            .expect("HTTP.COOKIE:cookies.nba-1", "")
            .expect("HTTP.COOKIE:cookies.nba-2", "1234")

            .checkExpectations();
    }

    @Test
    public void testResponseSetCookies() throws Exception {

        DissectorTester.create()
            .withDissectorUnderDummyRoot("cookies", new ResponseSetCookieListDissector())
            .withDissector(new ResponseSetCookieDissector())

            .withInput("" +
                "NBA-0=, " +
                "NBA-1=1234, " +
                "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT, " +
                "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/xx, " +
                "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/xx; domain=.basj.es, " +
                "NBA-5=1234; path=/xx; domain=.basj.es, " +
                "NBA-6=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; domain=.basj.es, "
            )

            .expect("HTTP.SETCOOKIES:cookies",
                "NBA-0=, " +
                "NBA-1=1234, " +
                "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT, " +
                "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/xx, " +
                "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/xx; domain=.basj.es, " +
                "NBA-5=1234; path=/xx; domain=.basj.es, " +
                "NBA-6=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; domain=.basj.es, ")

            .expect("HTTP.SETCOOKIE:cookies.nba-0",     "NBA-0=")
            .expect("STRING:cookies.nba-0.value",       "")
            .expect("STRING:cookies.nba-0.expires",     (Long)null)
            .expect("STRING:cookies.nba-0.path"   ,     (String)null)
            .expect("STRING:cookies.nba-0.domain" ,     (String)null)

            .expect("HTTP.SETCOOKIE:cookies.nba-1",     "NBA-1=1234")
            .expect("STRING:cookies.nba-1.value",       "1234")
            .expect("STRING:cookies.nba-1.expires",     (Long) null)
            .expect("STRING:cookies.nba-1.path"   ,     (String)null)
            .expect("STRING:cookies.nba-1.domain" ,     (String)null)

            .expect("HTTP.SETCOOKIE:cookies.nba-2",     "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT")
            .expect("STRING:cookies.nba-2.value",       "1234")
            .expect("STRING:cookies.nba-2.expires",     "1577836810")
            .expect("STRING:cookies.nba-2.expires",     1577836810L)
            .expect("TIME.EPOCH:cookies.nba-2.expires", 1577836810000L)
            .expect("STRING:cookies.nba-2.path"   ,     (String)null)
            .expect("STRING:cookies.nba-2.domain" ,     (String)null)

            .expect("HTTP.SETCOOKIE:cookies.nba-3",     "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/xx")
            .expect("STRING:cookies.nba-3.value",       "1234")
            .expect("STRING:cookies.nba-3.expires",     "1577836810")
            .expect("STRING:cookies.nba-3.expires",     1577836810L)
            .expect("TIME.EPOCH:cookies.nba-3.expires", 1577836810000L)
            .expect("STRING:cookies.nba-3.path"   ,     "/xx")
            .expect("STRING:cookies.nba-3.domain" ,     (String)null)

            .expect("HTTP.SETCOOKIE:cookies.nba-4",     "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/xx; domain=.basj.es")
            .expect("STRING:cookies.nba-4.value",       "1234")
            .expect("STRING:cookies.nba-4.expires",     "1577836810")
            .expect("STRING:cookies.nba-4.expires",     1577836810L)
            .expect("TIME.EPOCH:cookies.nba-4.expires", 1577836810000L)
            .expect("STRING:cookies.nba-4.path"   ,     "/xx")
            .expect("STRING:cookies.nba-4.domain" ,     ".basj.es")

            .expect("HTTP.SETCOOKIE:cookies.nba-5",     "NBA-5=1234; path=/xx; domain=.basj.es")
            .expect("STRING:cookies.nba-5.value",       "1234")
            .expect("STRING:cookies.nba-5.expires",     (String)null)
            .expect("STRING:cookies.nba-5.expires",     (Long)null)
            .expect("TIME.EPOCH:cookies.nba-5.expires", (Long)null)
            .expect("STRING:cookies.nba-5.path"   ,     "/xx")
            .expect("STRING:cookies.nba-5.domain" ,     ".basj.es")

            .expect("HTTP.SETCOOKIE:cookies.nba-6",     "NBA-6=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; domain=.basj.es")
            .expect("STRING:cookies.nba-6.value",       "1234")
            .expect("STRING:cookies.nba-6.expires",     "1577836810")
            .expect("STRING:cookies.nba-6.expires",     1577836810L)
            .expect("TIME.EPOCH:cookies.nba-6.expires", 1577836810000L)
            .expect("STRING:cookies.nba-6.path"   ,     (String)null)
            .expect("STRING:cookies.nba-6.domain" ,     ".basj.es")

            .checkExpectations();
    }


}
