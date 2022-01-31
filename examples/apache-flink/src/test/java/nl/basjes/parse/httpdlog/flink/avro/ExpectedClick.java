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
package nl.basjes.parse.httpdlog.flink.avro;

import nl.basjes.parse.webevents.Click;

import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedAsnNumber;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedAsnOrganization;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedCityName;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedContinentCode;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedContinentName;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedCountryIso;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedCountryName;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedIspName;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedIspOrganization;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedLocationLatitude;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedLocationLongitude;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedPostalCode;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedRequestReceiveTimeEpoch;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenWidth;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedSubdivisionIso;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedSubdivisionName;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedUseragent;

// CHECKSTYLE.OFF: HideUtilityClassConstructor
public class ExpectedClick {

    public static Click create(){
        Click.Builder builder = Click.newBuilder();

        builder
            .setTimestamp(getExpectedRequestReceiveTimeEpoch())
            .getDeviceBuilder()
                .setScreenWidth(getExpectedScreenWidth())
                .setScreenHeight(getExpectedScreenHeight());

        builder
            .getBrowserBuilder()
                .setUseragent(getExpectedUseragent());

        builder
            .getVisitorBuilder()
                .setIp(getExpectedConnectionClientHost());

        builder
            .getVisitorBuilder()
                .getIspBuilder()
                    .setAsnNumber(getExpectedAsnNumber())
                    .setAsnOrganization(getExpectedAsnOrganization())
                    .setIspName(getExpectedIspName())
                    .setIspOrganization(getExpectedIspOrganization());

        builder
            .getVisitorBuilder()
                .getGeoLocationBuilder()
                    .setContinentName(getExpectedContinentName())
                    .setContinentCode(getExpectedContinentCode())
                    .setCountryName(getExpectedCountryName())
                    .setCountryIso(getExpectedCountryIso())
                    .setSubdivisionName(getExpectedSubdivisionName())
                    .setSubdivisionIso(getExpectedSubdivisionIso())
                    .setCityName(getExpectedCityName())
                    .setPostalCode(getExpectedPostalCode())
                    .setLocationLatitude(getExpectedLocationLatitude())
                    .setLocationLongitude(getExpectedLocationLongitude());

        return builder.build();
    }

}
