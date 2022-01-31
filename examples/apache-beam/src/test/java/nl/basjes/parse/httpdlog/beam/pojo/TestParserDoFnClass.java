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

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.beam.TestCase;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


@RunWith(JUnit4.class)
public class TestParserDoFnClass implements Serializable {

    public static class MyParserDoFn extends DoFn<String, MyRecord> {
        private final Parser<MyRecord> parser;

        public MyParserDoFn() throws NoSuchMethodException {
            super();
            parser = TestCase.createTestParser();
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
            c.output(parser.parse(c.element()));
        }
    }

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testClassDefinition() throws Exception {
        List<String> logLines = Collections.singletonList(TestCase.getInputLine());

        // Apply Create, passing the list and the coder, to create the PCollection.
        PCollection<String> input = pipeline.apply(Create.of(logLines)).setCoder(StringUtf8Coder.of());

        PCollection<MyRecord> filledTestRecords = input
            .apply("Extract Elements from logline",
                ParDo.of(new MyParserDoFn()));

        MyRecord expected = new MyRecord().setFullValid();

        PAssert.that(filledTestRecords).containsInAnyOrder(expected);

        pipeline.run().waitUntilFinish();
    }

}
