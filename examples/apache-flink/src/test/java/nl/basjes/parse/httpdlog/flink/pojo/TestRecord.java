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

import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedAgent_class;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedAgent_name;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedAgent_version;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedBui;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedDevice_brand;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedDevice_class;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedGoogleQuery;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedReferrer;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedRequestReceiveTime;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedRequestUseragent;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedScreenResolution;
import static nl.basjes.parse.httpdlog.flink.TestUtils.getExpectedScreenWidth;
import static org.junit.Assert.assertEquals;

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
    @Getter @Setter private String device_class         = null;
    @Getter @Setter private String device_brand         = null;
    @Getter @Setter private String agent_class          = null;
    @Getter @Setter private String agent_name           = null;
    @Getter @Setter private String agent_version        = null;


    public void assertIsValid() {
        assertEquals(getExpectedConnectionClientHost() ,getConnectionClientHost());
        assertEquals(getExpectedRequestReceiveTime()   ,getRequestReceiveTime());
        assertEquals(getExpectedReferrer()             ,getReferrer());
        assertEquals(getExpectedScreenResolution()     ,getScreenResolution());
        assertEquals(getExpectedScreenWidth()          ,getScreenWidth());
        assertEquals(getExpectedScreenHeight()         ,getScreenHeight());
        assertEquals(getExpectedGoogleQuery()          ,getGoogleQuery());
        assertEquals(getExpectedBui()                  ,getBui());
        assertEquals(getExpectedRequestUseragent()     ,getRequestUseragent());
        assertEquals(getExpectedDevice_class()         ,getDevice_class());
        assertEquals(getExpectedDevice_brand()         ,getDevice_brand());
        assertEquals(getExpectedAgent_class()          ,getAgent_class());
        assertEquals(getExpectedAgent_name()           ,getAgent_name());
        assertEquals(getExpectedAgent_version()        ,getAgent_version());
    }

    public TestRecord setFullValid() {
        setConnectionClientHost (getExpectedConnectionClientHost() );
        setRequestReceiveTime   (getExpectedRequestReceiveTime()   );
        setReferrer             (getExpectedReferrer()             );
        setScreenResolution     (getExpectedScreenResolution()     );
        setScreenWidth          (getExpectedScreenWidth()          );
        setScreenHeight         (getExpectedScreenHeight()         );
        setGoogleQuery          (getExpectedGoogleQuery()          );
        setBui                  (getExpectedBui()                  );
        setRequestUseragent     (getExpectedRequestUseragent()     );
        setDevice_class         (getExpectedDevice_class()         );
        setDevice_brand         (getExpectedDevice_brand()         );
        setAgent_class          (getExpectedAgent_class()          );
        setAgent_name           (getExpectedAgent_name()           );
        setAgent_version        (getExpectedAgent_version()        );
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
