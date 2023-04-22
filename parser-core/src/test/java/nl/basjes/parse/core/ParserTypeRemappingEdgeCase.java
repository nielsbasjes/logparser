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

import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.core.Casts.LONG_ONLY;
import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTypeRemappingEdgeCase {

    public static class TestDissectorLongAsString extends SimpleDissector {

        private static final Map<String, EnumSet<Casts>> OUTPUT = new TreeMap<>();
        static {
            OUTPUT.put("LONG_AS_STRING:long_as_string", STRING_ONLY);
        }

        public TestDissectorLongAsString() {
            super("INPUTTYPE", OUTPUT);
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            // This happens for example if you extract a query string parameter you know to be a number
            parsable.addDissection(inputname, "LONG_AS_STRING", "long_as_string", "42");
        }
    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new TestDissectorLongAsString());
            setRootType("INPUTTYPE");
        }
    }

    public static class Record {
        String stringName1 = "empty";
        String stringValue1 = "empty";
        public void set1(String name, String value){
            stringName1 = name;
            stringValue1 = value;
        }

        String stringName2 = "empty";
        String stringValue2 = "empty";
        public void set2(String name, String value){
            stringName2 = name;
            stringValue2 = value;
        }

        String longName = "empty";
        long longValue = 0;
        public void set(String name, Long value){
            longName = name;
            longValue = value;
        }
    }

    @Test
    void testParseString() throws Exception {
        Parser<Record> parser = new TestParser<>(Record.class);
        parser
            .addParseTarget(Record.class.getMethod("set1", String.class, String.class), "LONG_AS_STRING:long_as_string")

            // If we add a type remapping with a cast that does not include STRING it should still work.
            .addTypeRemapping("long_as_string", "SOMETHING", LONG_ONLY)
            .addParseTarget(Record.class.getMethod("set", String.class, Long.class), "SOMETHING:long_as_string")
            // And this one should NOT be called
            .addParseTarget(Record.class.getMethod("set2", String.class, String.class), "SOMETHING:long_as_string");

        Record output = new Record();
        parser.parse(output, "An input that does not matter");
        assertEquals("42", output.stringValue1);
        assertEquals("empty", output.stringValue2);
        assertEquals(42, output.longValue);
    }

}
