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

package nl.basjes.parse.httpdlog.beam.avro;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import nl.basjes.parse.httpdlog.beam.TestCase;
import nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCityDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPISPDissector;
import nl.basjes.parse.webevents.Click;
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

import static nl.basjes.parse.httpdlog.beam.TestCase.CITY_TEST_MMDB;
import static nl.basjes.parse.httpdlog.beam.TestCase.ISP_TEST_MMDB;

// CHECKSTYLE.OFF: LineLength
// CHECKSTYLE.OFF: LeftCurly
@RunWith(JUnit4.class)
public class TestParserDoFnAvroInline implements Serializable {

    public static class ClickSetter implements Builder<Click> {

        final Click.Builder builder = Click.newBuilder();

        @Field("TIME.EPOCH:request.receive.time.epoch")             public void setRequestReceiveTime(Long value)       { builder.setTimestamp(value);                          }

        @Field("SCREENWIDTH:request.firstline.uri.query.s.width")   public void setScreenWidth(Long value)              { builder.getDeviceBuilder().setScreenWidth(value);     }
        @Field("SCREENHEIGHT:request.firstline.uri.query.s.height") public void setScreenHeight(Long value)             { builder.getDeviceBuilder().setScreenHeight(value);    }

        @Field("HTTP.USERAGENT:request.user-agent")                 public void setUseragent(String value)              { builder.getBrowserBuilder().setUseragent(value);      }

        @Field("IP:connection.client.host")                         public void setConnectionClientHost(String value)   { builder.getVisitorBuilder().setIp(value);             }

        @Field("ASN:connection.client.host.asn.number")             public void setAsnNumber(String value)              { builder.getVisitorBuilder().getIspBuilder().setAsnNumber(value);  }
        @Field("STRING:connection.client.host.asn.organization")    public void setAsnOrganization(String value)        { builder.getVisitorBuilder().getIspBuilder().setAsnOrganization(value);  }
        @Field("STRING:connection.client.host.isp.name")            public void setIspName(String value)                { builder.getVisitorBuilder().getIspBuilder().setIspName(value);  }
        @Field("STRING:connection.client.host.isp.organization")    public void setIspOrganization(String value)        { builder.getVisitorBuilder().getIspBuilder().setIspOrganization(value);  }

        @Field("STRING:connection.client.host.continent.name")      public void setContinentName(String value)          { builder.getVisitorBuilder().getGeoLocationBuilder().setContinentName(value);  }
        @Field("STRING:connection.client.host.continent.code")      public void setContinentCode(String value)          { builder.getVisitorBuilder().getGeoLocationBuilder().setContinentCode(value);  }
        @Field("STRING:connection.client.host.country.name")        public void setCountryName(String value)            { builder.getVisitorBuilder().getGeoLocationBuilder().setCountryName(value);  }
        @Field("STRING:connection.client.host.country.iso")         public void setCountryIso(String value)             { builder.getVisitorBuilder().getGeoLocationBuilder().setCountryIso(value);  }
        @Field("STRING:connection.client.host.subdivision.name")    public void setSubdivisionName(String value)        { builder.getVisitorBuilder().getGeoLocationBuilder().setSubdivisionName(value);  }
        @Field("STRING:connection.client.host.subdivision.iso")     public void setSubdivisionIso(String value)         { builder.getVisitorBuilder().getGeoLocationBuilder().setSubdivisionIso(value);  }
        @Field("STRING:connection.client.host.city.name")           public void setCityName(String value)               { builder.getVisitorBuilder().getGeoLocationBuilder().setCityName(value);  }
        @Field("STRING:connection.client.host.postal.code")         public void setPostalCode(String value)             { builder.getVisitorBuilder().getGeoLocationBuilder().setPostalCode(value);  }
        @Field("STRING:connection.client.host.location.latitude")   public void setLocationLatitude(Double value)       { builder.getVisitorBuilder().getGeoLocationBuilder().setLocationLatitude(value);  }
        @Field("STRING:connection.client.host.location.longitude")  public void setLocationLongitude(Double value)      { builder.getVisitorBuilder().getGeoLocationBuilder().setLocationLongitude(value);  }

        @Override
        public Click build() {
            return builder.build();
        }
    }

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testClassDefinitionAvro() {
        List<String> logLines = Collections.singletonList(TestCase.getInputLine());

        // Apply Create, passing the list and the coder, to create the PCollection.
        PCollection<String> input = pipeline.apply(Create.of(logLines)).setCoder(StringUtf8Coder.of());

        PCollection<Click> filledTestRecords = input
            .apply("Extract Elements from logline",
                ParDo.of(new DoFn<String, Click>() {
                    private Parser<ClickSetter> parser;

                    @Setup
                    public void setup() {
                        parser = new HttpdLoglineParser<>(ClickSetter.class, TestCase.getLogFormat())
                                .addDissector(new ScreenResolutionDissector())
                                .addTypeRemapping("request.firstline.uri.query.g", "HTTP.URI")
                                .addTypeRemapping("request.firstline.uri.query.r", "HTTP.URI")
                                .addTypeRemapping("request.firstline.uri.query.s", "SCREENRESOLUTION")
                                .addDissector(new GeoIPISPDissector(ISP_TEST_MMDB))
                                .addDissector(new GeoIPCityDissector(CITY_TEST_MMDB));
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
