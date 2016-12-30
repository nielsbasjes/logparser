/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserCastsTest {

    public static class TestDissector extends Dissector {

        public TestDissector() {
            // Empty
        }

        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            return true; // Everything went right
        }

        protected void initializeNewInstance(Dissector newInstance) {
            // Empty
        }

        @Override
        public void dissect(Parsable<?> parsable, final String inputname) throws DissectionFailure {
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_null", (String)null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_good", "123");

            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_null", (String)null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_bad", "Something");
            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_good", "123");

            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_null", (String)null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_bad", "Something");
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_good", "123");

            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_long_null", (String)null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_double_null", (String)null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "multi_null", (String)null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_long_good", "123");
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_double_good", "123");
            parsable.addDissection(inputname, "OUTPUT_TYPE", "multi_good", "123");

        }

        @Override
        public String getInputType() {
            return "INPUT_TYPE";
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("OUTPUT_TYPE:string_null");
            result.add("OUTPUT_TYPE:string_good");
            result.add("OUTPUT_TYPE:long_null");
            result.add("OUTPUT_TYPE:long_bad");
            result.add("OUTPUT_TYPE:long_good");
            result.add("OUTPUT_TYPE:double_null");
            result.add("OUTPUT_TYPE:double_bad");
            result.add("OUTPUT_TYPE:double_good");
            result.add("OUTPUT_TYPE:string_long_null");
            result.add("OUTPUT_TYPE:string_double_null");
            result.add("OUTPUT_TYPE:multi_null");
            result.add("OUTPUT_TYPE:string_long_good");
            result.add("OUTPUT_TYPE:string_double_good");
            result.add("OUTPUT_TYPE:multi_good");
            return result;
        }


        private static final Map<String, EnumSet<Casts>> PREPARE_FOR_DISSECT_MAP = new HashMap<>();
        static {
            PREPARE_FOR_DISSECT_MAP.put("string_null",          Casts.STRING_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("string_good",          Casts.STRING_ONLY);

            PREPARE_FOR_DISSECT_MAP.put("long_null",            Casts.LONG_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("long_bad",             Casts.LONG_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("long_good",            Casts.LONG_ONLY);

            PREPARE_FOR_DISSECT_MAP.put("double_null",          Casts.DOUBLE_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("double_bad",           Casts.DOUBLE_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("double_good",          Casts.DOUBLE_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("string_long_null",     Casts.STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("string_double_null",   Casts.STRING_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("multi_null",           Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("string_long_good",     Casts.STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("string_double_good",   Casts.STRING_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("multi_good",           Casts.STRING_OR_LONG_OR_DOUBLE);
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return PREPARE_FOR_DISSECT_MAP.get(outputname);
        }

        @Override
        public void prepareForRun() {
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
        private int count = 0;
        @Field({"OUTPUT_TYPE:string_null",
                "OUTPUT_TYPE:string_long_null",
                "OUTPUT_TYPE:string_double_null",
                "OUTPUT_TYPE:multi_null"})
        public void setStringNull(String value) {
            assertEquals(null, value);
            count++;
        }

        @Field({"OUTPUT_TYPE:string_good",
                "OUTPUT_TYPE:string_long_good",
                "OUTPUT_TYPE:string_double_good",
                "OUTPUT_TYPE:multi_good"})
        public void setStringGood(String value) {
            assertEquals("123", value);
            count++;
        }

        @Field({"OUTPUT_TYPE:long_null",
                "OUTPUT_TYPE:long_bad",
                "OUTPUT_TYPE:string_long_null",
                "OUTPUT_TYPE:multi_null"})
        public void setLongNull(Long value) {
            assertEquals(null, value);
            count++;
        }

        @Field({"OUTPUT_TYPE:long_good",
                "OUTPUT_TYPE:string_long_good",
                "OUTPUT_TYPE:multi_good"})
        public void setLongGood(Long value) {
            assertEquals(Long.valueOf(123L), value);
            count++;
        }

        @Field({"OUTPUT_TYPE:double_null",
                "OUTPUT_TYPE:double_bad",
                "OUTPUT_TYPE:string_double_null",
                "OUTPUT_TYPE:multi_null"})
        public void setDoubleNull(Double value) {
            assertEquals(null, value);
            count++;
        }

        @Field({"OUTPUT_TYPE:double_good",
                "OUTPUT_TYPE:string_double_good",
                "OUTPUT_TYPE:multi_good"})
        public void setDoubleGood(Double value) {
            assertEquals(123D, value, 0.0001D);
            count++;
        }

        @SuppressWarnings("UnusedParameters")
        @Field({"OUTPUT_TYPE:long_null",
                "OUTPUT_TYPE:long_bad",
                "OUTPUT_TYPE:long_good",
                "OUTPUT_TYPE:string_long_null",
                "OUTPUT_TYPE:string_long_good"})
        public void setLongWrongSignature(String name, Double value) {
            fail("This setter uses Double but that is not allowed for \""+name+"\" ");
        }

        @SuppressWarnings("UnusedParameters")
        @Field({"OUTPUT_TYPE:double_null",
                "OUTPUT_TYPE:double_bad",
                "OUTPUT_TYPE:double_good",
                "OUTPUT_TYPE:string_double_null",
                "OUTPUT_TYPE:string_double_good"})
        public void setDoubleWrongSignature(String name, Long value) {
            fail("This setter uses Long but that is not allowed for \""+name+"\" ");
        }
    }

    @Test
    public void testValidCasting() throws Exception {
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);
        TestRecord output = new TestRecord();
        parser.parse(output, "Something");
        assertEquals(22, output.count);
    }

}
