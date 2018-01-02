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

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector;
import nl.basjes.parse.httpdlog.flink.TestCase;
import nl.basjes.parse.record.Click;
import nl.basjes.parse.useragent.dissector.UserAgentDissector;
import org.apache.commons.lang3.builder.Builder;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.assertEquals;

// CHECKSTYLE.OFF: LineLength
// CHECKSTYLE.OFF: LeftCurly
@RunWith(JUnit4.class)
public class TestParserMapFunctionAvroInline implements Serializable {

    public static class ClickSetter implements Builder<Click> {

        Click.Builder builder = Click.newBuilder();

        @Field("TIME.EPOCH:request.receive.time.epoch")             public void setRequestReceiveTime(Long value)       { builder.setTimestamp(value);                          }

        @Field("SCREENWIDTH:request.firstline.uri.query.s.width")   public void setScreenWidth(Long value)              { builder.getDeviceBuilder().setScreenWidth(value);     }
        @Field("SCREENHEIGHT:request.firstline.uri.query.s.height") public void setScreenHeight(Long value)             { builder.getDeviceBuilder().setScreenHeight(value);    }
        @Field("STRING:request.user-agent.device_class")            public void setDeviceClass(String value)            { builder.getDeviceBuilder().setDeviceClass(value);     }
        @Field("STRING:request.user-agent.device_brand")            public void setDeviceBrand(String value)            { builder.getDeviceBuilder().setDeviceBrand(value);     }

        @Field("STRING:request.user-agent.agent_class")             public void setAgentClass(String value)             { builder.getBrowserBuilder().setAgentClass(value);     }
        @Field("STRING:request.user-agent.agent_name")              public void setAgentName(String value)              { builder.getBrowserBuilder().setAgentName(value);      }
        @Field("STRING:request.user-agent.agent_version")           public void setAgentVersion(String value)           { builder.getBrowserBuilder().setAgentVersion(value);   }

        @Field("IP:connection.client.host")                         public void setConnectionClientHost(String value)   { builder.getVisitorBuilder().setIp(value);             }

        @Override
        public Click build() {
            return builder.build();
        }
    }

    @Test
    public void testInlineDefinitionAvro() throws Exception {
        // set up the execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSet<String> input = env.fromElements(TestCase.getInputLine());

        DataSet<Click> filledTestRecords = input
            .map(new RichMapFunction<String, Click>() {
                private Parser<ClickSetter> parser;

                @Override
                public void open(org.apache.flink.configuration.Configuration parameters) {
                    parser = new HttpdLoglineParser<>(ClickSetter.class, TestCase.getLogFormat());

                    parser.addDissector(new ScreenResolutionDissector());
                    parser.addTypeRemapping("request.firstline.uri.query.s", "SCREENRESOLUTION");

                    parser.addDissector(new UserAgentDissector());
                }

                @Override
                public Click map(String line) throws Exception {
                    return parser.parse(line).build();
                }
            }).name("Extract Elements from logline");

        filledTestRecords.print();

        List<Click> result = filledTestRecords.collect();

        assertEquals(1, result.size());
        assertEquals(ExpectedClick.create(), result.get(0));
    }

}
