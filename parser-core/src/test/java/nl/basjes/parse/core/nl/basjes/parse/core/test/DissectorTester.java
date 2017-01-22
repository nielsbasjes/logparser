/*
 * Apache HTTPD & NGINX Access log parsing made easy
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
package nl.basjes.parse.core.nl.basjes.parse.core.test;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DissectorTester {

    private static final Logger LOG = LoggerFactory.getLogger(DissectorTester.class);

    boolean verbose = false;
    private List<String> inputValues = new ArrayList<>();
    private Map<String, String> expectedStrings = new TreeMap<>();
    private Map<String, Long> expectedLongs = new TreeMap<>();
    private Map<String, Double> expectedDoubles = new TreeMap<>();
    private List<String> expectedValuePresent = new ArrayList<>();
    private List<String> expectedAbsentStrings = new ArrayList<>();
    private List<String> expectedAbsentLongs = new ArrayList<>();
    private List<String> expectedAbsentDoubles = new ArrayList<>();
    private List<String> expectedPossible = new ArrayList<>();
    private Parser<TestRecord> parser = new Parser<>(TestRecord.class);

    private DissectorTester() {
    }

    public static DissectorTester create() {
        return new DissectorTester();
    }

    public DissectorTester withParser(Parser<TestRecord> parser) {
        this.parser = parser;
        return this;
    }

    /**
     * Wildcard dissectors at the root doesn't work (yet)
     * So to test these we can create a dummy root dissector that simply inserts a "first level dissector that does nothing"
     * @param fieldName The fieldName of the first level path
     * @param dissector The first REAL dissector for this test
     * @return This DissectorTester
     */
    public DissectorTester withDissector(String fieldName, Dissector dissector) {
        return withDissector(new DummyDissector(dissector.getInputType(), fieldName))
                .withDissector(dissector);
    }

    public DissectorTester withDissector(Dissector dissector) {
        parser.addDissector(dissector);
        if (parser.getAllDissectors().size() == 1) {
            parser.setRootType(dissector.getInputType());
        }
        return this;
    }

    public DissectorTester withInput(String inputValue) {
        this.inputValues.add(inputValue);
        return this;
    }

    public DissectorTester expect(String fieldname, String expected) {
        expectedStrings.put(fieldname, expected);
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return this;
    }

    public DissectorTester expect(String fieldname, Long expected) {
        expectedLongs.put(fieldname, expected);
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setLongValue", String.class, Long.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return this;
    }

    public DissectorTester expect(String fieldname, Double expected) {
        expectedDoubles.put(fieldname, expected);
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setDoubleValue", String.class, Double.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return this;
    }

    public DissectorTester expectValuePresent(String fieldname) {
        expectedValuePresent.add(fieldname);
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return this;
    }

    public DissectorTester expectAbsentString(String fieldname) {
        expectedAbsentStrings.add(fieldname);
        return this;
    }

    public DissectorTester expectAbsentLong(String fieldname) {
        expectedAbsentLongs.add(fieldname);
        return this;
    }

    public DissectorTester expectAbsentDouble(String fieldname) {
        expectedAbsentDoubles.add(fieldname);
        return this;
    }

    public DissectorTester expectPossible(String fieldname) {
        expectedPossible.add(fieldname);
        return this;
    }

    public DissectorTester verbose() {
        this.verbose = true;
        return this;
    }

    public DissectorTester checkExpectations() {
        if (expectedStrings.isEmpty() &&
            expectedLongs.isEmpty() &&
            expectedDoubles.isEmpty() &&

            expectedValuePresent.isEmpty() &&

            expectedAbsentStrings.isEmpty() &&
            expectedAbsentLongs.isEmpty() &&
            expectedAbsentDoubles.isEmpty() &&

            expectedPossible.isEmpty()) {
            fail("No expected values were specified");
        }

        checkDissectors();
        checkExpectedValues();
        checkExpectedAbsent();
        checkExpectedPossible();
        return this;
    }

    private void checkExpectedValues() {
        if (expectedStrings.size() +
            expectedLongs.size() +
            expectedDoubles.size() +
            expectedValuePresent.size() == 0) {
            return; // Nothing to do here
        }

        if (inputValues.isEmpty()) {
            fail("No inputvalues were specified");
        }

        for (String inputValue : inputValues) {
            if (verbose) {
                LOG.info("Checking for input: {}", inputValue);
            }

            TestRecord result = parse(inputValue);

            if (verbose) {
                LOG.info("Parse completed successfully");
            }

            int longestFieldName = 0;
            Set<String> allFieldNames = new HashSet<>();
            allFieldNames.addAll(expectedStrings.keySet());
            allFieldNames.addAll(expectedLongs.keySet());
            allFieldNames.addAll(expectedDoubles.keySet());
            allFieldNames.addAll(expectedValuePresent);
            for (String key : allFieldNames) {
                longestFieldName = Math.max(longestFieldName, key.length());
            }

            for (Map.Entry<String, String> expectation : expectedStrings.entrySet()) {
                String fieldName = expectation.getKey();
                assertTrue("The expected String value for '" + fieldName + "' was missing.", result.hasStringValue(fieldName));
                assertEquals("The expected String value for '" + fieldName + "' was wrong.", expectation.getValue(), result.getStringValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: String value for '{}'{} was correctly : {}", fieldName, padding(fieldName, longestFieldName), result.getStringValue(fieldName));
                }
            }

            for (Map.Entry<String, Long> expectation : expectedLongs.entrySet()) {
                String fieldName = expectation.getKey();
                assertTrue("The expected Long value for '" + fieldName + "' was missing.", result.hasLongValue(fieldName));
                assertEquals("The expected Long value for '" + fieldName + "' was wrong.", expectation.getValue(), result.getLongValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: Long   value for '{}'{} was correctly : {}", fieldName, padding(fieldName, longestFieldName), result.getLongValue(fieldName));
                }
            }

            for (Map.Entry<String, Double> expectation : expectedDoubles.entrySet()) {
                String fieldName = expectation.getKey();
                assertTrue("The expected Double value for '" + fieldName + "' was missing.", result.hasDoubleValue(fieldName));
                assertEquals("The expected Double value for '" + fieldName + "' was wrong.", expectation.getValue(), result.getDoubleValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: Double value for '{}'{} was correctly : {}", fieldName, padding(fieldName, longestFieldName), result.getDoubleValue(fieldName));
                }
            }

            for (String fieldName: expectedValuePresent) {
                assertTrue("The string value for '" + fieldName + "' was missing.", result.hasStringValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: A value for '{}'{} was present.", fieldName, padding(fieldName, longestFieldName));
                }
            }

        }
    }

    private void checkExpectedAbsent() {
        if (expectedAbsentStrings.size() +
            expectedAbsentLongs.size() +
            expectedAbsentDoubles.size() == 0) {
            return; // Nothing to do here
        }

        if (inputValues.isEmpty()) {
            fail("No inputvalues were specified");
        }

        for (String inputValue : inputValues) {
            if (verbose) {
                LOG.info("Checking for input: {}", inputValue);
            }

            TestRecord result = parse(inputValue);

            if (verbose) {
                LOG.info("Parse completed successfully");
            }

            int longestFieldName = 0;
            Set<String> allFieldNames = new HashSet<>();
            allFieldNames.addAll(expectedAbsentStrings);
            allFieldNames.addAll(expectedAbsentLongs);
            allFieldNames.addAll(expectedAbsentDoubles);
            allFieldNames.addAll(expectedValuePresent);
            for (String key : allFieldNames) {
                longestFieldName = Math.max(longestFieldName, key.length());
            }

            for (String fieldName: expectedAbsentStrings) {
                assertFalse("The String value for '" + fieldName + "' should have been absent. It was :." + result.getStringValue(fieldName), result.hasStringValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: String value for '{}'{} was correctly absent", fieldName, padding(fieldName, longestFieldName));
                }
            }

            for (String fieldName: expectedAbsentLongs) {
                assertFalse("The Long value for '" + fieldName + "' should have been absent. It was :." + result.getLongValue(fieldName), result.hasLongValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: Long value for '{}'{} was correctly absent", fieldName, padding(fieldName, longestFieldName));
                }
            }

            for (String fieldName: expectedAbsentDoubles) {
                assertFalse("The Double value for '" + fieldName + "' should have been absent. It was :." + result.getDoubleValue(fieldName), result.hasDoubleValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: Double value for '{}'{} was correctly absent", fieldName, padding(fieldName, longestFieldName));
                }
            }
        }
    }

    private TestRecord parse(String inputValue) {
        TestRecord testRecord = new TestRecord();
        if (verbose) {
            testRecord.setVerbose();
        }
        try {
            return parser.parse(testRecord, inputValue);
        } catch (DissectionFailure | InvalidDissectorException | MissingDissectorsException e) {
            fail(e.toString());
        }
        return testRecord; // This will never happen
    }

    private void checkExpectedPossible() {
        int longestFieldName = 0;
        for (String fieldName : expectedValuePresent) {
            longestFieldName = Math.max(longestFieldName, fieldName.length());
        }

        List<String> allpossible = parser.getPossiblePaths();
        for (String fieldName: expectedPossible) {
            assertTrue("The fieldName '" + fieldName + "' is not possible.", allpossible.contains(fieldName));
            if (verbose) {
                LOG.info("Passed: Fieldname '{}'{} is possible.", fieldName, padding(fieldName, longestFieldName));
            }
        }
    }

    private void checkDissectors() {
        Set<Dissector> dissectors = parser.getAllDissectors();
        for (Dissector dissector: dissectors) {
            for (String output: dissector.getPossibleOutput()) {
                String baseMsg = "Dissector " + dissector.getClass().getSimpleName() + " outputs " + output;
                String[] splitOutput = output.split(":",2);
                assertEquals(baseMsg + " which is not fully uppercase", splitOutput[0].toUpperCase(Locale.ENGLISH), splitOutput[0]);
                assertEquals(baseMsg + " which is not fully lowercase", splitOutput[1].toLowerCase(Locale.ENGLISH), splitOutput[1]);
            }
        }
    }

    private String padding(String name, int longestFieldName) {
        int i = longestFieldName - name.length();
        if (i == 0) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%" + i + "s", "");
    }

    public DissectorTester printDissectors() {
        LOG.info("=====================================================");
        LOG.info("Dissectors:");
        LOG.info("=====================================================");

        Set<Dissector> dissectors = parser.getAllDissectors();
        for (Dissector dissector: dissectors) {
            LOG.info("-----------------------------------------------------");
            LOG.info("{} --> {}", dissector.getInputType(), dissector.getClass().getSimpleName());
            for (String output: dissector.getPossibleOutput()) {
                LOG.info(">> {}", output);
            }
        }
        LOG.info("=====================================================");
        return this;
    }

    public DissectorTester printPossible() {
        LOG.info("=====================================================");
        LOG.info("Possible:");
        LOG.info("----------");
        for (String path: parser.getPossiblePaths()) {
            LOG.info("---> {}", path);
        }
        LOG.info("=====================================================");
        return this;
    }

    public List<String> getPossible() {
        return parser.getPossiblePaths();
    }

    public DissectorTester printAllPossibleValues() {
        if (inputValues.isEmpty()) {
            fail("No inputvalues were specified");
        }

        try {
            List<String> possibleFieldNames = parser.getPossiblePaths();
            for (String path: possibleFieldNames) {
                parser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), path);
            }

            for (String inputValue : inputValues) {
                LOG.info("=====================================================");
                LOG.info("All values (except wildcards) for input:{}", inputValue);
                LOG.info("=====================================================");
                for (String path : possibleFieldNames) {
                    parser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), path);
                }
                TestRecord result = parser.parse(inputValue);

                int longestFieldName = 0;
                for (String fieldName : possibleFieldNames) {
                    longestFieldName = Math.max(longestFieldName, fieldName.length());
                }
                for (String fieldName : possibleFieldNames) {
                    String value = result.getStringValue(fieldName);
                    if (value == null) {
                        value = "<<<null>>>";
                    }
                    LOG.info("Found value for {}{} = {}", fieldName, padding(fieldName, longestFieldName), value);
                }

            }
            LOG.info("=====================================================");
        } catch (NoSuchMethodException | InvalidDissectorException | MissingDissectorsException | DissectionFailure e) {
            e.printStackTrace();
            fail("Shouldn't have any exceptions");
        }
        return this;
    }

    public DissectorTester printSeparator() {
        LOG.info("");
        LOG.info("--------------------------------------------------------------------------------");
        LOG.info("");
        return this;
    }

    public static class DummyDissector extends Dissector {

        private String outputType;
        private String fieldName;

        public DummyDissector() {
        }

        public DummyDissector(String newOutputType, String newFieldName) {
            fieldName = newFieldName;
            outputType = newOutputType;
        }

        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            return true;
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            final ParsedField field = parsable.getParsableField("DUMMYROOT", inputname);
            parsable.addDissection(inputname, outputType, fieldName, field.getValue());
        }

        @Override
        public String getInputType() {
            return "DUMMYROOT";
        }

        @Override
        public List<String> getPossibleOutput() {
            return Collections.singletonList(outputType + ":" + fieldName);
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return Casts.STRING_ONLY;
        }

        @Override
        public void prepareForRun() throws InvalidDissectorException {

        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
            DummyDissector dummyDissector = (DummyDissector)newInstance;
            dummyDissector.fieldName = fieldName;
            dummyDissector.outputType = outputType;
        }
    }
}
