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
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.test.NormalValuesDissector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static org.junit.Assert.assertEquals;

public class ParserResetTest {

    public static class WildCardDissector extends SimpleDissector {

        // Deliberate Dissector bug: We use a List instead of a Set so any duplicates are retained.
        protected final List<String> outputNames = new ArrayList<>();

        private static final Map<String, EnumSet<Casts>> OUTPUT_TYPES = new HashMap<>();
        static {
            OUTPUT_TYPES.put("EXTRA:*", STRING_ONLY);
        }
        public WildCardDissector() {
            super("STRING", OUTPUT_TYPES);
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            for (String outputName : outputNames) {
                parsable.addDissection(inputname, "EXTRA", outputName, outputName);
            }
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            String name = outputname;
            String prefix = inputname + '.';
            if (outputname.startsWith(prefix)) {
                name = outputname.substring(prefix.length());
            }
            outputNames.add(name);
            return STRING_ONLY;
        }
    }

    public static class DuplicateTestRecord {
        private final Map<String, List<String>>  stringMap   = new HashMap<>(32);

        public void setStringValue(final String name, final String value) {
            stringMap.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
        }
        public List<String> getStringValues(final String name) {
            return stringMap.get(name);
        }
    }


    @Test
    public void testParserReset() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        Parser<DuplicateTestRecord> parser = new Parser<>(DuplicateTestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addDissector(new WildCardDissector())
            .addParseTarget("setStringValue", "STRING:string")
            .addParseTarget("setStringValue", "EXTRA:string.one")
            .addParseTarget("setStringValue", "EXTRA:string.two");

        // This causes the parser to initialize
        parser.getPossiblePaths();

        // This should reset the parser completely
        parser.addDissector(null);

        DuplicateTestRecord testRecord = new DuplicateTestRecord();
        parser.parse(testRecord, "Doesn't matter");

        assertEquals("FortyTwo", testRecord.getStringValues("STRING:string").get(0));
        assertEquals("one",      testRecord.getStringValues("EXTRA:string.one").get(0));
        assertEquals("two",      testRecord.getStringValues("EXTRA:string.two").get(0));

        // There used to be a bug that would make some values arrive twice.
        assertEquals(1,          testRecord.getStringValues("STRING:string").size());
        assertEquals(1,          testRecord.getStringValues("EXTRA:string.one").size());
        assertEquals(1,          testRecord.getStringValues("EXTRA:string.two").size());
    }
}
