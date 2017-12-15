/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

package nl.basjes.parse.httpdlog.flink.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;

import java.io.Serializable;

import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedAgentClass;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedAgentName;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedAgentVersion;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedBui;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedDeviceBrand;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedDeviceClass;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedGoogleQuery;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedReferrer;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedRequestReceiveTime;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedRequestUseragent;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenResolution;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenWidth;
import static org.junit.Assert.assertEquals;

// CHECKSTYLE.OFF: LineLength
// CHECKSTYLE.OFF: LeftCurly
// CHECKSTYLE.OFF: ParamPad
// CHECKSTYLE.OFF: MethodParamPad
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
    @Getter @Setter private String requestUseragent     = null;
    @Getter @Setter private String deviceClass          = null;
    @Getter @Setter private String deviceBrand          = null;
    @Getter @Setter private String agentClass           = null;
    @Getter @Setter private String agentName            = null;
    @Getter @Setter private String agentVersion         = null;


    public void assertIsValid() {
        assertEquals(getExpectedConnectionClientHost(), getConnectionClientHost());
        assertEquals(getExpectedRequestReceiveTime(),   getRequestReceiveTime());
        assertEquals(getExpectedReferrer(),             getReferrer());
        assertEquals(getExpectedScreenResolution(),     getScreenResolution());
        assertEquals(getExpectedScreenWidth(),          getScreenWidth());
        assertEquals(getExpectedScreenHeight(),         getScreenHeight());
        assertEquals(getExpectedGoogleQuery(),          getGoogleQuery());
        assertEquals(getExpectedBui(),                  getBui());
        assertEquals(getExpectedRequestUseragent(),     getRequestUseragent());
        assertEquals(getExpectedDeviceClass(),          getDeviceClass());
        assertEquals(getExpectedDeviceBrand(),          getDeviceBrand());
        assertEquals(getExpectedAgentClass(),           getAgentClass());
        assertEquals(getExpectedAgentName(),            getAgentName());
        assertEquals(getExpectedAgentVersion(),         getAgentVersion());
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
        setRequestUseragent     (getExpectedRequestUseragent());
        setDeviceClass          (getExpectedDeviceClass());
        setDeviceBrand          (getExpectedDeviceBrand());
        setAgentClass           (getExpectedAgentClass());
        setAgentName            (getExpectedAgentName());
        setAgentVersion         (getExpectedAgentVersion());
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
