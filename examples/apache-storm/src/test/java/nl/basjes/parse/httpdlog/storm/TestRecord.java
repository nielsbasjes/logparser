/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

package nl.basjes.parse.httpdlog.storm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;

import java.io.Serializable;

import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedBui;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedGoogleQuery;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedReferrer;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedRequestReceiveTime;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedScreenResolution;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedScreenWidth;
import static nl.basjes.parse.httpdlog.storm.TestCase.getExpectedUseragent;
import static org.junit.Assert.assertEquals;

// CHECKSTYLE.OFF: ParamPad
@ToString
@EqualsAndHashCode
public class TestRecord implements Serializable {

    @Getter @Setter private String connectionClientHost = null;
    @Getter @Setter private String requestReceiveTime   = null;
    @Getter @Setter private String referrer             = null;
    @Getter @Setter private String screenResolution     = null;
    @Getter @Setter private Long   screenWidth          = null;
    @Getter @Setter private Long   screenHeight         = null;
    @Getter @Setter private String googleQuery          = null;
    @Getter @Setter private String bui                  = null;
    @Getter @Setter private String useragent            = null;


    public void assertIsValid() {
        assertEquals(getExpectedConnectionClientHost(), getConnectionClientHost());
        assertEquals(getExpectedRequestReceiveTime(),   getRequestReceiveTime());
        assertEquals(getExpectedReferrer(),             getReferrer());
        assertEquals(getExpectedScreenResolution(),     getScreenResolution());
        assertEquals(getExpectedScreenWidth(),          getScreenWidth());
        assertEquals(getExpectedScreenHeight(),         getScreenHeight());
        assertEquals(getExpectedGoogleQuery(),          getGoogleQuery());
        assertEquals(getExpectedBui(),                  getBui());
        assertEquals(getExpectedUseragent(),            getUseragent());
    }

    public TestRecord setFullValid() {
        setConnectionClientHost (getExpectedConnectionClientHost());
        setRequestReceiveTime   (getExpectedRequestReceiveTime());
        setReferrer             (getExpectedReferrer());
        setScreenResolution     (getExpectedScreenResolution());
        setScreenWidth          (getExpectedScreenWidth());
        setScreenHeight         (getExpectedScreenHeight());
        setGoogleQuery          (getExpectedGoogleQuery());
        setBui                  (getExpectedBui());
        setUseragent            (getExpectedUseragent());
        return this;
    }


    @Test
    public void checkTestMethodsPass() {
        TestRecord testRecord = new TestRecord().setFullValid();
        testRecord.assertIsValid();
    }

    @Test(expected = AssertionError.class)
    public void checkTestMethodsFail() {
        TestRecord testRecord = new TestRecord();
        testRecord.assertIsValid();
    }

}
