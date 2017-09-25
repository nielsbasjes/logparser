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

package nl.basjes.parse.httpdlog.beam.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;

import java.io.Serializable;

import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAgent_class;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAgent_name;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAgent_version;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedBui;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedDevice_brand;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedDevice_class;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedGoogleQuery;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedReferrer;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedRequestReceiveTime;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedRequestUseragent;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenResolution;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenWidth;
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


    public static void assertTestRecordIsValid(TestRecord record) {
        assertEquals(getExpectedConnectionClientHost() ,record.getConnectionClientHost());
        assertEquals(getExpectedRequestReceiveTime()   ,record.getRequestReceiveTime());
        assertEquals(getExpectedReferrer()             ,record.getReferrer());
        assertEquals(getExpectedScreenResolution()     ,record.getScreenResolution());
        assertEquals(getExpectedScreenWidth()          ,record.getScreenWidth());
        assertEquals(getExpectedScreenHeight()         ,record.getScreenHeight());
        assertEquals(getExpectedGoogleQuery()          ,record.getGoogleQuery());
        assertEquals(getExpectedBui()                  ,record.getBui());
        assertEquals(getExpectedRequestUseragent()     ,record.getRequestUseragent());
        assertEquals(getExpectedDevice_class()         ,record.getDevice_class());
        assertEquals(getExpectedDevice_brand()         ,record.getDevice_brand());
        assertEquals(getExpectedAgent_class()          ,record.getAgent_class());
        assertEquals(getExpectedAgent_name()           ,record.getAgent_name());
        assertEquals(getExpectedAgent_version()        ,record.getAgent_version());
    }

    public static TestRecord setFullValid(TestRecord testRecord) {
        testRecord.setConnectionClientHost (getExpectedConnectionClientHost() );
        testRecord.setRequestReceiveTime   (getExpectedRequestReceiveTime()   );
        testRecord.setReferrer             (getExpectedReferrer()             );
        testRecord.setScreenResolution     (getExpectedScreenResolution()     );
        testRecord.setScreenWidth          (getExpectedScreenWidth()          );
        testRecord.setScreenHeight         (getExpectedScreenHeight()         );
        testRecord.setGoogleQuery          (getExpectedGoogleQuery()          );
        testRecord.setBui                  (getExpectedBui()                  );
        testRecord.setRequestUseragent     (getExpectedRequestUseragent()     );
        testRecord.setDevice_class         (getExpectedDevice_class()         );
        testRecord.setDevice_brand         (getExpectedDevice_brand()         );
        testRecord.setAgent_class          (getExpectedAgent_class()          );
        testRecord.setAgent_name           (getExpectedAgent_name()           );
        testRecord.setAgent_version        (getExpectedAgent_version()        );
        return testRecord;
    }


    @Test
    public void checkTestMethodsPass() {
        TestRecord testRecord = new TestRecord();
        setFullValid(testRecord);
        assertTestRecordIsValid(testRecord);
    }

    @Test(expected = AssertionError.class)
    public void checkTestMethodsFail() {
        TestRecord testRecord = new TestRecord();
        assertTestRecordIsValid(testRecord);
    }

}
