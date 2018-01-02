/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
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
package nl.basjes.parse.httpdlog.beam.avro;

import nl.basjes.parse.record.Click;

import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAgentClass;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAgentName;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedAgentVersion;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedDeviceBrand;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedDeviceClass;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedRequestReceiveTimeEpoch;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.beam.TestCase.getExpectedScreenWidth;

// CHECKSTYLE.OFF: HideUtilityClassConstructor
public class ExpectedClick {

    public static Click create(){
        Click.Builder builder = Click.newBuilder();

        builder
            .setTimestamp(getExpectedRequestReceiveTimeEpoch())
            .getDeviceBuilder()
                .setScreenWidth(getExpectedScreenWidth())
                .setScreenHeight(getExpectedScreenHeight())
                .setDeviceClass(getExpectedDeviceClass())
                .setDeviceBrand(getExpectedDeviceBrand());

        builder
            .getBrowserBuilder()
                .setAgentClass(getExpectedAgentClass())
                .setAgentName(getExpectedAgentName())
                .setAgentVersion(getExpectedAgentVersion());

        builder
            .getVisitorBuilder()
                .setIp(getExpectedConnectionClientHost());
        return builder.build();
    }

}
