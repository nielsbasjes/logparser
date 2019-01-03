/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ParserInfiniteLoopTest {

    public static class TestDissector extends Dissector {

        public TestDissector() {
            // Empty
        }

        @Override
        public void dissect(Parsable<?> parsable, final String inputname) throws DissectionFailure {
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string", "123");
        }

        @Override
        public String getInputType() {
            return "INPUT_TYPE";
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("OUTPUT_TYPE:string");
            return result;
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return Casts.STRING_ONLY;
        }
    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new TestDissector());
            setRootType("INPUT_TYPE");
        }
    }

    public static class TestRecord {
        @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
        @Field({"OUTPUT_TYPE:string"})
        public void set(String name, String value) {
            // Do nothing
        }
    }


    /**
     * This creates a dissector and a type remap that causes it to (effectively)
     * be an infinite recursive loop. This is the reproduction situation for a problem
     * that is now fixed.
     * @throws Exception in case of error
     */
    @Test
    public void testInfiniteRecursionAvoidance() throws Exception {
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);
        parser.addTypeRemapping("string", "INPUT_TYPE");
        TestRecord output = new TestRecord();
        parser.parse(output, "Something");
        // If this works then it will cleanly end (instead of a stack overflow)
    }

}
