/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.core.Casts.DOUBLE_ONLY;
import static nl.basjes.parse.core.Casts.LONG_ONLY;
import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_DOUBLE;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG_OR_DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ParserCastsTest {

    public static class MyDissector extends Dissector {

        public MyDissector() {
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
            PREPARE_FOR_DISSECT_MAP.put("string_null",          STRING_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("string_good",          STRING_ONLY);

            PREPARE_FOR_DISSECT_MAP.put("long_null",            LONG_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("long_bad",             LONG_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("long_good",            LONG_ONLY);

            PREPARE_FOR_DISSECT_MAP.put("double_null",          DOUBLE_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("double_bad",           DOUBLE_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("double_good",          DOUBLE_ONLY);
            PREPARE_FOR_DISSECT_MAP.put("string_long_null",     STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("string_double_null",   STRING_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("multi_null",           STRING_OR_LONG_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("string_long_good",     STRING_OR_LONG);
            PREPARE_FOR_DISSECT_MAP.put("string_double_good",   STRING_OR_DOUBLE);
            PREPARE_FOR_DISSECT_MAP.put("multi_good",           STRING_OR_LONG_OR_DOUBLE);
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return PREPARE_FOR_DISSECT_MAP.getOrDefault(outputname, NO_CASTS);
        }
    }

    public static class MyParser<RECORD> extends Parser<RECORD> {
        public MyParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new MyDissector());
            setRootType("INPUT_TYPE");
        }
    }

    public static class MyRecord {
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
    void testValidCasting() throws Exception {
        Parser<MyRecord> parser = new MyParser<>(MyRecord.class);
        MyRecord output = new MyRecord();
        parser.parse(output, "Something");
        assertEquals(22, output.count);

        Map<String, EnumSet<Casts>> allCasts = parser.getAllCasts();
        assertEquals(STRING_ONLY,               allCasts.get("OUTPUT_TYPE:string_good"));
        assertEquals(LONG_ONLY,                 allCasts.get("OUTPUT_TYPE:long_good"));
        assertEquals(DOUBLE_ONLY,               allCasts.get("OUTPUT_TYPE:double_good"));
        assertEquals(STRING_OR_LONG,            allCasts.get("OUTPUT_TYPE:string_long_good"));
        assertEquals(STRING_OR_DOUBLE,          allCasts.get("OUTPUT_TYPE:string_double_good"));
        assertEquals(STRING_OR_LONG_OR_DOUBLE,  allCasts.get("OUTPUT_TYPE:multi_good"));

        assertEquals(STRING_ONLY,               parser.getCasts("OUTPUT_TYPE:string_good"));
        assertEquals(LONG_ONLY,                 parser.getCasts("OUTPUT_TYPE:long_good"));
        assertEquals(DOUBLE_ONLY,               parser.getCasts("OUTPUT_TYPE:double_good"));
        assertEquals(STRING_OR_LONG,            parser.getCasts("OUTPUT_TYPE:string_long_good"));
        assertEquals(STRING_OR_DOUBLE,          parser.getCasts("OUTPUT_TYPE:string_double_good"));
        assertEquals(STRING_OR_LONG_OR_DOUBLE,  parser.getCasts("OUTPUT_TYPE:multi_good"));
    }

}
