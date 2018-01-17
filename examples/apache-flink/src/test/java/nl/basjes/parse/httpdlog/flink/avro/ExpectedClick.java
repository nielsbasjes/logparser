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
package nl.basjes.parse.httpdlog.flink.avro;

import nl.basjes.parse.record.Click;

import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedConnectionClientHost;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedRequestReceiveTimeEpoch;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenHeight;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedScreenWidth;
import static nl.basjes.parse.httpdlog.flink.TestCase.getExpectedUseragent;


// CHECKSTYLE.OFF: HideUtilityClassConstructor
// CHECKSTYLE.OFF: ParenPad
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
        return builder.build();
    }

}
