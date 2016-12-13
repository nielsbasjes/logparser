/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DissectorTester {

    private static final Logger LOG = LoggerFactory.getLogger(DissectorTester.class);

    boolean verbose = false;
    private List<String> inputValues = new ArrayList<>();
    private Map<String, String> expectedStrings = new HashMap<>();
    private Map<String, Long> expectedLongs = new HashMap<>();
    private Map<String, Double> expectedDoubles = new HashMap<>();
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

    public DissectorTester verbose() {
        this.verbose = true;
        return this;
    }

    public void check() {
        if (inputValues.isEmpty()) {
            fail("No inputvalues were specified");
        }

        if (expectedStrings.isEmpty() && expectedLongs.isEmpty() && expectedDoubles.isEmpty()) {
            fail("No expected values were specified");
        }

        checkDissectors();
        checkExpectedValues();
    }

    private void checkExpectedValues() {
        for (String inputValue : inputValues) {
            if (verbose) {
                LOG.info("Checking for input: {}", inputValue);
            }

            TestRecord result = null;
            try {
                result = parser.parse(new TestRecord(), inputValue);
            } catch (DissectionFailure | InvalidDissectorException | MissingDissectorsException e) {
                fail(e.toString());
            }

            if (verbose) {
                LOG.info("Parse completed successfully");
            }

            int longestFieldName = 0;
            Set<String> allFieldNames = new HashSet<>();
            allFieldNames.addAll(expectedStrings.keySet());
            allFieldNames.addAll(expectedLongs.keySet());
            allFieldNames.addAll(expectedDoubles.keySet());
            for (String key : allFieldNames) {
                longestFieldName = Math.max(longestFieldName, key.length());
            }

            for (Map.Entry<String, String> expectation : expectedStrings.entrySet()) {
                String fieldName = expectation.getKey();
                assertEquals("The expected string value for '" + fieldName + "' was wrong.", expectation.getValue(), result.getStringValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: String value for '{}'{} was correctly : {}", fieldName, padding(fieldName, longestFieldName), result.getStringValue(fieldName));
                }
            }

            for (Map.Entry<String, Long> expectation : expectedLongs.entrySet()) {
                String fieldName = expectation.getKey();
                assertEquals("The expected string value for '" + fieldName + "' was wrong.", expectation.getValue(), result.getLongValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: Long   value for '{}'{} was correctly : {}", fieldName, padding(fieldName, longestFieldName), result.getLongValue(fieldName));
                }
            }

            for (Map.Entry<String, Double> expectation : expectedDoubles.entrySet()) {
                String fieldName = expectation.getKey();
                assertEquals("The expected string value for '" + fieldName + "' was wrong.", expectation.getValue(), result.getDoubleValue(fieldName));
                if (verbose) {
                    LOG.info("Passed: Double value for '{}'{} was correctly : {}", fieldName, padding(fieldName, longestFieldName), result.getDoubleValue(fieldName));
                }
            }
        }
    }

    private void checkDissectors() {
        Set<Dissector> dissectors = parser.getAllDissectors();
        for (Dissector dissector: dissectors) {
            for (String output: dissector.getPossibleOutput()) {
                String[] splitOutput = output.split(":",2);
                assertEquals(splitOutput[0], splitOutput[0].toUpperCase(Locale.ENGLISH));
                assertEquals(splitOutput[1], splitOutput[1].toLowerCase(Locale.ENGLISH));
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

}
