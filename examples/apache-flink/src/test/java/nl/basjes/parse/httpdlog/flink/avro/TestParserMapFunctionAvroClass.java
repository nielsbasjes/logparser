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
import org.apache.flink.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TestParserMapFunctionAvroClass implements Serializable {

    public static class ClickSetter implements Builder<Click> {

        Click.Builder builder = Click.newBuilder();

        @Field("TIME.EPOCH:request.receive.time.epoch")             public void setRequestReceiveTime(Long value)       { builder.setTimestamp(value);                          }

        @Field("SCREENWIDTH:request.firstline.uri.query.s.width")   public void setScreenWidth(Long value)              { builder.getDeviceBuilder().setScreenWidth(value);     }
        @Field("SCREENHEIGHT:request.firstline.uri.query.s.height") public void setScreenHeight(Long value)             { builder.getDeviceBuilder().setScreenHeight(value);    }
        @Field("STRING:request.user-agent.device_class")            public void setDevice_class(String value)           { builder.getDeviceBuilder().setDeviceClass(value);     }
        @Field("STRING:request.user-agent.device_brand")            public void setDevice_brand(String value)           { builder.getDeviceBuilder().setDeviceBrand(value);     }

        @Field("STRING:request.user-agent.agent_class")             public void setAgent_class(String value)            { builder.getBrowserBuilder().setAgentClass(value);     }
        @Field("STRING:request.user-agent.agent_name")              public void setAgent_name(String value)             { builder.getBrowserBuilder().setAgentName(value);      }
        @Field("STRING:request.user-agent.agent_version")           public void setAgent_version(String value)          { builder.getBrowserBuilder().setAgentVersion(value);   }

        @Field("IP:connection.client.host")                         public void setConnectionClientHost(String value)   { builder.getVisitorBuilder().setIp(value);             }

        @Override
        public Click build() {
            return builder.build();
        }
    }

    public static class MyParserMapper extends RichMapFunction<String, Click> {
        private Parser<ClickSetter> parser;

        @Override
        public void open(Configuration parameters) throws NoSuchMethodException {
            parser = new HttpdLoglineParser<>(ClickSetter.class, TestCase.getLogFormat());

            parser.addDissector(new ScreenResolutionDissector());
            parser.addTypeRemapping("request.firstline.uri.query.s", "SCREENRESOLUTION");

            parser.addDissector(new UserAgentDissector());
        }

        @Override
        public Click map(String input) throws Exception {
            ClickSetter setter = parser.parse(new ClickSetter(), input);
            if (setter == null) {
                System.err.println("Something went terribly wrong");
                return null;
            }
            return setter.build();
        }
    }

    @Test
    public void testClassDefinitionAvro() throws Exception {
        // set up the execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSet<String> input = env.fromElements(TestCase.getInputLine());

        DataSet<Click> filledTestRecords = input
            .map(new MyParserMapper())
            .name("Extract Elements from logline");

        filledTestRecords.print();

        List<Click> result = filledTestRecords.collect();

        assertEquals(1, result.size());
        assertEquals(ExpectedClick.create(), result.get(0));
    }

}
