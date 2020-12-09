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

package nl.basjes.parse.httpdlog.beam.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;

import java.io.Serializable;

import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAsnNumber;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAsnOrganization;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedBui;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedCityName;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedContinentCode;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedContinentName;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedCountryIso;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedCountryName;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedGoogleQuery;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedIspName;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedIspOrganization;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedLocationLatitude;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedLocationLongitude;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedPostalCode;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedReferrer;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedRequestReceiveTime;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenResolution;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenWidth;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedSubdivisionIso;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedSubdivisionName;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedUseragent;
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

    @Getter @Setter private String asnNumber            = null;
    @Getter @Setter private String asnOrganization      = null;
    @Getter @Setter private String ispName              = null;
    @Getter @Setter private String ispOrganization      = null;

    @Getter @Setter private String continentName        = null;
    @Getter @Setter private String continentCode        = null;
    @Getter @Setter private String countryName          = null;
    @Getter @Setter private String countryIso           = null;
    @Getter @Setter private String subdivisionName      = null;
    @Getter @Setter private String subdivisionIso       = null;
    @Getter @Setter private String cityName             = null;
    @Getter @Setter private String postalCode           = null;
    @Getter @Setter private Double locationLatitude     = null;
    @Getter @Setter private Double locationLongitude    = null;

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

        assertEquals(getExpectedAsnNumber(),            getAsnNumber());
        assertEquals(getExpectedAsnOrganization(),      getAsnOrganization());
        assertEquals(getExpectedIspName(),              getIspName());
        assertEquals(getExpectedIspOrganization(),      getIspOrganization());

        assertEquals(getExpectedContinentName(),        getContinentName());
        assertEquals(getExpectedContinentCode(),        getContinentCode());
        assertEquals(getExpectedSubdivisionName(),      getSubdivisionName());
        assertEquals(getExpectedSubdivisionIso(),       getSubdivisionIso());
        assertEquals(getExpectedCountryName(),          getCountryName());
        assertEquals(getExpectedCountryIso(),           getCountryIso());
        assertEquals(getExpectedCityName(),             getCityName());
        assertEquals(getExpectedPostalCode(),           getPostalCode());
        assertEquals(getExpectedLocationLatitude(),     getLocationLatitude());
        assertEquals(getExpectedLocationLongitude(),    getLocationLongitude());
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

        setAsnNumber            (getExpectedAsnNumber());
        setAsnOrganization      (getExpectedAsnOrganization());
        setIspName              (getExpectedIspName());
        setIspOrganization      (getExpectedIspOrganization());

        setContinentName        (getExpectedContinentName());
        setContinentCode        (getExpectedContinentCode());
        setCountryName          (getExpectedCountryName());
        setCountryIso           (getExpectedCountryIso());
        setSubdivisionName      (getExpectedSubdivisionName());
        setSubdivisionIso       (getExpectedSubdivisionIso());
        setCityName             (getExpectedCityName());
        setPostalCode           (getExpectedPostalCode());
        setLocationLatitude     (getExpectedLocationLatitude());
        setLocationLongitude    (getExpectedLocationLongitude());

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
