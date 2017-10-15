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

package nl.basjes.parse.httpdlog.beam.avro;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import nl.basjes.parse.httpdlog.beam.TestCase;
import nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector;
import nl.basjes.parse.record.Click;
import nl.basjes.parse.useragent.dissector.UserAgentDissector;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.lang3.builder.Builder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class TestParserDoFnAvroInline implements Serializable {

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

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testClassDefinitionAvro() throws Exception {
        List<String> logLines = Collections.singletonList(TestCase.getInputLine());

        // Apply Create, passing the list and the coder, to create the PCollection.
        PCollection<String> input = pipeline.apply(Create.of(logLines)).setCoder(StringUtf8Coder.of());

        PCollection<Click> filledTestRecords = input
            .apply("Extract Elements from logline",
                ParDo.of(new DoFn<String, Click>() {
                    private Parser<ClickSetter> parser;

                    @Setup
                    public void setup() {
                        parser = new HttpdLoglineParser<>(ClickSetter.class, TestCase.getLogFormat());

                        parser.addDissector(new ScreenResolutionDissector());
                        parser.addTypeRemapping("request.firstline.uri.query.s", "SCREENRESOLUTION");

                        parser.addDissector(new UserAgentDissector());
                    }

                    @ProcessElement
                    public void processElement(ProcessContext c) throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
                        String input = c.element();
                        ClickSetter setter = parser.parse(new ClickSetter(), input);
                        Click click = setter.build();
                        c.output(click);
                    }
                }));

        PAssert.that(filledTestRecords).containsInAnyOrder(ExpectedClick.create());

        pipeline.run().waitUntilFinish();
    }

}
