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

import nl.basjes.parse.httpdlog.flink.TestCase;
import nl.basjes.parse.record.Click;


// CHECKSTYLE.OFF: HideUtilityClassConstructor
// CHECKSTYLE.OFF: ParenPad
public class ExpectedClick {

    public static Click create(){
        Click.Builder builder = Click.newBuilder();

        builder.setTimestamp(                        TestCase.getExpectedRequestReceiveTimeEpoch());
        builder.getDeviceBuilder().setScreenWidth(   TestCase.getExpectedScreenWidth());
        builder.getDeviceBuilder().setScreenHeight(  TestCase.getExpectedScreenHeight());
        builder.getDeviceBuilder().setDeviceClass(   TestCase.getExpectedDeviceClass());
        builder.getDeviceBuilder().setDeviceBrand(   TestCase.getExpectedDeviceBrand());
        builder.getBrowserBuilder().setAgentClass(   TestCase.getExpectedAgentClass());
        builder.getBrowserBuilder().setAgentName(    TestCase.getExpectedAgentName());
        builder.getBrowserBuilder().setAgentVersion( TestCase.getExpectedAgentVersion());
        builder.getVisitorBuilder().setIp(           TestCase.getExpectedConnectionClientHost());
        return builder.build();
    }

}
