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

public class ParserDissectionOutputTypesTest {

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
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_set_null", (String) null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "string_set_string", "42");

            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_set_string_null", (String) null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_set_string", "42");

            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_set_longclass_null", (Long) null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_set_longclass", Long.valueOf(42));
            parsable.addDissection(inputname, "OUTPUT_TYPE", "long_set_longprimitive", 42L);

            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_string_null", (String) null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_string", "42");

            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_longclass_null", (Long) null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_longclass", Long.valueOf(42));
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_longprimitive", 42L);

            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_doubleclass_null", (Double) null);
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_doubleclass", Double.valueOf(42));
            parsable.addDissection(inputname, "OUTPUT_TYPE", "double_set_doubleprimitive", 42D);

        }

        @Override
        public String getInputType() {
            return "INPUT_TYPE";
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("OUTPUT_TYPE:string_set_null");
            result.add("OUTPUT_TYPE:string_set_string");

            result.add("OUTPUT_TYPE:long_set_longclass_null");
            result.add("OUTPUT_TYPE:long_set_longclass");
            result.add("OUTPUT_TYPE:long_set_longprimitive");
            result.add("OUTPUT_TYPE:long_set_string_null");
            result.add("OUTPUT_TYPE:long_set_string");

            result.add("OUTPUT_TYPE:double_set_doubleclass_null");
            result.add("OUTPUT_TYPE:double_set_doubleclass");
            result.add("OUTPUT_TYPE:double_set_doubleprimitive");
            result.add("OUTPUT_TYPE:double_set_longclass_null");
            result.add("OUTPUT_TYPE:double_set_longclass");
            result.add("OUTPUT_TYPE:double_set_longprimitive");
            result.add("OUTPUT_TYPE:double_set_string_null");
            result.add("OUTPUT_TYPE:double_set_string");
            return result;
        }

        private static final Map<String, EnumSet<Casts>> PREPARE_FOR_DISSECT_MAP = new HashMap<>();

        static {
            PREPARE_FOR_DISSECT_MAP.put("string_set_null",              Casts.STRING_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("string_set_string",            Casts.STRING_ONLY);

            PREPARE_FOR_DISSECT_MAP.put("long_set_longclass_null",      Casts.STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("long_set_longclass",           Casts.STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("long_set_longprimitive",       Casts.STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("long_set_string_null",         Casts.STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("long_set_string",              Casts.STRING_OR_LONG);

            PREPARE_FOR_DISSECT_MAP.put("double_set_doubleclass_null",  Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_doubleclass",       Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_doubleprimitive",   Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_longclass_null",    Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_longclass",         Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_longprimitive",     Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_string_null",       Casts.STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("double_set_string",            Casts.STRING_OR_LONG_OR_DOUBLE);
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

    @SuppressWarnings("unused")
    public static class TestRecord {
        private int count = 0;

        @Field({
            "OUTPUT_TYPE:string_set_null",
            "OUTPUT_TYPE:long_set_longclass_null",
            "OUTPUT_TYPE:long_set_string_null",
            "OUTPUT_TYPE:double_set_doubleclass_null",
            "OUTPUT_TYPE:double_set_longclass_null",
            "OUTPUT_TYPE:double_set_string_null"
        })
        public void setStringNull(String value) {
            count++;
            assertEquals(null, value);
        }

        @Field({
            "OUTPUT_TYPE:long_set_longclass_null",
            "OUTPUT_TYPE:long_set_string_null",
            "OUTPUT_TYPE:double_set_doubleclass_null",
            "OUTPUT_TYPE:double_set_longclass_null",
            "OUTPUT_TYPE:double_set_string_null"
        })
        public void setLongNull(Long value) {
            count++;
            assertEquals(null, value);
        }

        @Field({
            "OUTPUT_TYPE:double_set_doubleclass_null",
            "OUTPUT_TYPE:double_set_longclass_null",
            "OUTPUT_TYPE:double_set_string_null"
        })
        public void setDoubleNull(Double value) {
            count++;
            assertEquals(null, value);
        }

        @Field({
            "OUTPUT_TYPE:string_set_string",
            "OUTPUT_TYPE:long_set_longclass",
            "OUTPUT_TYPE:long_set_longprimitive",
            "OUTPUT_TYPE:long_set_string",
            "OUTPUT_TYPE:double_set_longclass",
            "OUTPUT_TYPE:double_set_longprimitive",
            "OUTPUT_TYPE:double_set_string"
        })
        public void setString(String value) {
            count++;
            assertEquals("42", value);
        }

        @Field({
            "OUTPUT_TYPE:double_set_doubleclass",
            "OUTPUT_TYPE:double_set_doubleprimitive"
        })
        public void setStringFromDouble(String value) {
            count++;
            assertEquals("42.0", value);
        }

        @Field({
            "OUTPUT_TYPE:long_set_longclass",
            "OUTPUT_TYPE:long_set_longprimitive",
            "OUTPUT_TYPE:long_set_string",
            "OUTPUT_TYPE:double_set_doubleclass",
            "OUTPUT_TYPE:double_set_doubleprimitive",
            "OUTPUT_TYPE:double_set_longclass",
            "OUTPUT_TYPE:double_set_longprimitive",
            "OUTPUT_TYPE:double_set_string"
        })
        public void setLong(Long value) {
            count++;
            assertEquals(Long.valueOf(42L), value);
        }

        @Field({
            "OUTPUT_TYPE:double_set_doubleclass",
            "OUTPUT_TYPE:double_set_doubleprimitive",
            "OUTPUT_TYPE:double_set_longclass",
            "OUTPUT_TYPE:double_set_longprimitive",
            "OUTPUT_TYPE:double_set_string"
        })
        public void setDouble(Double value) {
            count++;
            assertEquals(42D, value, 0.01);
        }
    }

    @Test
    public void testSetterTypes() throws Exception {
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);
        TestRecord output = new TestRecord();
        parser.parse(output, "Something");
        assertEquals(36, output.count);
    }

}
