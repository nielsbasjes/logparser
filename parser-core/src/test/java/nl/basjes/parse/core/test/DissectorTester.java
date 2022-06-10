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
package nl.basjes.parse.core.test;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public final class DissectorTester implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(DissectorTester.class);

    private boolean verbose = false;
    private final List<String> inputValues = new ArrayList<>();
    private final Map<String, String> expectedStrings = new TreeMap<>();
    private final Map<String, Long> expectedLongs = new TreeMap<>();
    private final Map<String, Double> expectedDoubles = new TreeMap<>();
    private final List<String> expectedValuePresent = new ArrayList<>();
    private final List<String> expectedAbsentStrings = new ArrayList<>();
    private final List<String> expectedAbsentLongs = new ArrayList<>();
    private final List<String> expectedAbsentDoubles = new ArrayList<>();
    private final List<String> expectedPossible = new ArrayList<>();
    private Parser<TestRecord> parser = new Parser<>(TestRecord.class);
    private String pathPrefix = "";

    private DissectorTester() {
    }

    public static DissectorTester create() {
        return new DissectorTester();
    }

    public DissectorTester withParser(Parser<TestRecord> newParser) {
        this.parser = newParser;
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

    private void addStringSetter(String fieldname) {
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void addLongSetter(String fieldname) {
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setLongValue", String.class, Long.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void addDoubleSetter(String fieldname) {
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setDoubleValue", String.class, Double.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public DissectorTester expect(String fieldname, String expected) {
        fieldname = addPrefix(fieldname);
        expectedStrings.put(fieldname, expected);
        addStringSetter(fieldname);
        return this;
    }

    public DissectorTester expect(String fieldname, Long expected) {
        fieldname = addPrefix(fieldname);
        expectedLongs.put(fieldname, expected);
        addLongSetter(fieldname);
        return this;
    }

    public DissectorTester expect(String fieldname, Integer expected) {
        return expect(fieldname, Long.valueOf(expected));
    }

    public DissectorTester expect(String fieldname, Double expected) {
        fieldname = addPrefix(fieldname);
        expectedDoubles.put(fieldname, expected);
        addDoubleSetter(fieldname);
        return this;
    }

    public DissectorTester expect(String fieldname, Float expected) {
        return expect(fieldname, Double.valueOf(expected));
    }

    public DissectorTester expectNull(String fieldname) {
        fieldname = addPrefix(fieldname);
        expectedStrings.put(fieldname, null);
        addStringSetter(fieldname);
        return this;
    }

    public DissectorTester expectValuePresent(String fieldname) {
        fieldname = addPrefix(fieldname);
        expectedValuePresent.add(fieldname);
        try {
            parser.addParseTarget(TestRecord.class.getMethod("setStringValue", String.class, String.class), fieldname);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return this;
    }

    public DissectorTester expectAbsentString(String fieldname) {
        fieldname = addPrefix(fieldname);
        expectedAbsentStrings.add(fieldname);
        addStringSetter(fieldname);
        return this;
    }

    public DissectorTester expectAbsentLong(String fieldname) {
        fieldname = addPrefix(fieldname);
        expectedAbsentLongs.add(fieldname);
        addLongSetter(fieldname);
        return this;
    }

    public DissectorTester expectAbsentDouble(String fieldname) {
        fieldname = addPrefix(fieldname);
        expectedAbsentDoubles.add(fieldname);
        addDoubleSetter(fieldname);
        return this;
    }

    public DissectorTester expectPossible(String fieldname) {
        fieldname = addPrefix(fieldname);
        expectedPossible.add(fieldname);
        return this;
    }

    public DissectorTester verbose() {
        this.verbose = true;
        return this;
    }

    private static final Pattern PREFIX_INSERTER = Pattern.compile("([^:]+:)([^:]+)");

    public DissectorTester withPathPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            pathPrefix = "";
        } else {
            pathPrefix = "$1" + prefix + "$2";
        }
        return this;
    }

    String addPrefix(String field) {
        if (pathPrefix.isEmpty()) {
            return field;
        }
        return PREFIX_INSERTER.matcher(field).replaceAll(pathPrefix);
    }

    private static class ExpectationResult {
        final String expectation;
        final String field;
        final String value;
        final String failReason;

        ExpectationResult(String expectation, String field, Object value, String failReason) {
            this.expectation = expectation;
            this.field = field;
            if (value == null) {
                this.value = null;
            } else {
                this.value = value.toString();
            }
            this.failReason = failReason;
        }
    }

    private void expectEquals(List<ExpectationResult> expectationResults, String fieldName, String msg, Object left, Object right) {
        boolean expression = false;
        String expected = "<<<null>>>";
        if (left == null) {
            if (right == null) {
                expression = true;
            }
        } else {
            expected = left.toString();
            expression = left.equals(right);
        }
        if (expression) {
            expectationResults.add(new ExpectationResult(msg, fieldName, expected, null));
        } else {
            if (right == null) {
                expectationResults.add(new ExpectationResult(msg, fieldName, expected, "Wrong value: <<<null>>>"));
            } else {
                expectationResults.add(new ExpectationResult(msg, fieldName, expected, "Wrong value: "+right));
            }
        }
    }

    public DissectorTester checkExpectations() {
        DissectorTester tester = SerializationUtils.clone(this);
        try {
            return tester.checkExpectationsDirect();
        } catch (AssertionError ae) {
            throw new AssertionError(ae.getMessage());
        }
    }

    private DissectorTester checkExpectationsDirect() {
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
        List<ExpectationResult> results = new ArrayList<>(32);

        results.addAll(checkDissectors());
        results.addAll(checkExpectedValues());
        results.addAll(checkExpectedAbsent());
        results.addAll(checkExpectedPossible());

        summarizeResults(results);
        return this;
    }

    private void summarizeResults(List<ExpectationResult> results) {
        boolean success = true;
        final String headerField            = "Field";
        final String headerCheck            = "Check";
        final String headerExpectedValue    = "Expected Value";
        final String headerFailReason       = "Fail reason";
        int maxExpectation                  = headerField        .length();
        int maxFieldName                    = headerCheck        .length();
        int maxExpectedValue                = headerExpectedValue.length();
        int maxFailReason                   = headerFailReason   .length();

        for (ExpectationResult expectationResult: results) {
            maxExpectation = Math.max(maxExpectation, expectationResult.expectation.length());
            maxFieldName = Math.max(maxFieldName, expectationResult.field.length());
            if (expectationResult.value != null) {
                maxExpectedValue = Math.max(maxExpectedValue, expectationResult.value.length());
            }
            if (expectationResult.failReason != null) {
                success = false;
                maxFailReason = Math.max(maxFailReason, expectationResult.failReason.length());
            }
        }
        if (!success) {
            StringBuilder sb = new StringBuilder(1024);
            sb.append("\n[     ] /").append(padding("", maxExpectation+maxFieldName+maxExpectedValue+maxFailReason+11, '=')).append("\\\n");
            sb
                .append("[     ] | ")
                .append(headerField).append(padding(headerField, maxFieldName))
                .append(" | ")
                .append(headerCheck).append(padding(headerCheck, maxExpectation))
                .append(" | ")
                .append(headerExpectedValue).append(padding(headerExpectedValue, maxExpectedValue))
                .append(" | ")
                .append(headerFailReason).append(padding(headerFailReason, maxFailReason))
                .append(" |")
                .append("\n[     ] +")
                    .append(padding("", maxFieldName+2, '-')).append('+')
                    .append(padding("", maxExpectation+2, '-')).append('+')
                    .append(padding("", maxExpectedValue+2, '-')).append('+')
                    .append(padding("", maxFailReason+2, '-')).append('+')
                .append("\n");

            for (ExpectationResult expectationResult: results) {
                if (expectationResult.failReason == null) {
                    sb.append("[     ] ");
                } else {
                    sb.append("[ERROR] ");
                }
                sb
                    .append("| ")
                    .append(expectationResult.field).append(padding(expectationResult.field, maxFieldName))
                    .append(" | ")
                    .append(expectationResult.expectation).append(padding(expectationResult.expectation, maxExpectation))
                    .append(" | ");
                String value = expectationResult.value;
                if (expectationResult.value == null) {
                    value = " ";
                }
                sb
                    .append(padding(value, maxExpectedValue)).append(value).append(" | ");
                if (expectationResult.failReason == null) {
                    sb.append(padding("", maxFailReason));
                } else {
                    sb.append(expectationResult.failReason).append(padding(expectationResult.failReason, maxFailReason));
                }
                sb.append(" |\n");
            }
            sb.append("[     ] \\").append(padding("", maxExpectation+maxFieldName+maxExpectedValue+maxFailReason+11, '=')).append("/\n");
            fail(sb.toString());
        }
    }

    private List<ExpectationResult> checkExpectedValues() {
        if (expectedStrings.size() +
            expectedLongs.size() +
            expectedDoubles.size() +
            expectedValuePresent.size() == 0) {
            return Collections.emptyList(); // Nothing to do here
        }

        if (inputValues.isEmpty()) {
            fail("No inputvalues were specified");
        }

        List<ExpectationResult> expectationResults = new ArrayList<>(32);

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
                if (!result.hasStringValue(fieldName)) {
                    expectationResults.add(new ExpectationResult("String value", fieldName, expectation.getValue(), "Missing"));
                } else {
                    expectEquals(expectationResults, fieldName, "String value",
                        expectation.getValue(), result.getStringValue(fieldName));
                }
                if (verbose) {
                    LOG.info("Passed: String value for '{}'{} was correctly : {}",
                        fieldName, padding(fieldName, longestFieldName), result.getStringValue(fieldName));
                }
            }

            for (Map.Entry<String, Long> expectation : expectedLongs.entrySet()) {
                String fieldName = expectation.getKey();
                if (!result.hasLongValue(fieldName)) {
                    expectationResults.add(new ExpectationResult("Long value", fieldName, expectation.getValue(), "Missing"));
                } else {
                    expectEquals(expectationResults, fieldName, "Long value",
                        expectation.getValue(), result.getLongValue(fieldName));
                }
                if (verbose) {
                    LOG.info("Passed: Long   value for '{}'{} was correctly : {}",
                        fieldName, padding(fieldName, longestFieldName), result.getLongValue(fieldName));
                }
            }

            for (Map.Entry<String, Double> expectation : expectedDoubles.entrySet()) {
                String fieldName = expectation.getKey();
                if (!result.hasDoubleValue(fieldName)) {
                    expectationResults.add(new ExpectationResult("Double value", fieldName, expectation.getValue(), "Missing"));
                } else {
                    expectEquals(expectationResults, fieldName, "Double value",
                        expectation.getValue(), result.getDoubleValue(fieldName));
                }
                if (verbose) {
                    LOG.info("Passed: Double value for '{}'{} was correctly : {}",
                        fieldName, padding(fieldName, longestFieldName), result.getDoubleValue(fieldName));
                }
            }

            for (String fieldName: expectedValuePresent) {
                expectationResults.add(new ExpectationResult("String present", fieldName, null, result.hasStringValue(fieldName) ? null : "Missing"));

                if (verbose) {
                    LOG.info("Passed: A value for '{}'{} was present.", fieldName, padding(fieldName, longestFieldName));
                }
            }

        }
        return expectationResults;
    }

    private List<ExpectationResult> checkExpectedAbsent() {
        if (expectedAbsentStrings.size() +
            expectedAbsentLongs.size() +
            expectedAbsentDoubles.size() == 0) {
            return Collections.emptyList(); // Nothing to do here
        }

        if (inputValues.isEmpty()) {
            fail("No inputvalues were specified");
        }

        List<ExpectationResult> expectationResults = new ArrayList<>(32);

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
                expectationResults.add(new ExpectationResult("String absent", fieldName,
                    result.getStringValue(fieldName), result.hasStringValue(fieldName) ? "Present" : null));
                if (verbose) {
                    LOG.info("Passed: String value for '{}'{} was correctly absent", fieldName, padding(fieldName, longestFieldName));
                }
            }

            for (String fieldName: expectedAbsentLongs) {
                expectationResults.add(new ExpectationResult("Long absent", fieldName,
                    result.getLongValue(fieldName), result.hasLongValue(fieldName) ? "Present" : null));
                if (verbose) {
                    LOG.info("Passed: Long value for '{}'{} was correctly absent", fieldName, padding(fieldName, longestFieldName));
                }
            }

            for (String fieldName: expectedAbsentDoubles) {
                expectationResults.add(new ExpectationResult("Double absent", fieldName,
                    result.getDoubleValue(fieldName), result.hasDoubleValue(fieldName) ? "Present" : null));
                if (verbose) {
                    LOG.info("Passed: Double value for '{}'{} was correctly absent", fieldName, padding(fieldName, longestFieldName));
                }
            }
        }
        return expectationResults;
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

    private List<ExpectationResult> checkExpectedPossible() {
        if (expectedPossible.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExpectationResult> expectationResults = new ArrayList<>(32);

        int longestFieldName = 0;
        for (String fieldName : expectedPossible) {
            longestFieldName = Math.max(longestFieldName, fieldName.length());
        }

        List<String> allpossible = parser.getPossiblePaths();
        for (String fieldName: expectedPossible) {
            expectationResults.add(new ExpectationResult("Fieldname possible", fieldName,
                null, allpossible.contains(fieldName) ? null : "Not possible"));
            if (verbose) {
                LOG.info("Passed: Fieldname '{}'{} is possible.", fieldName, padding(fieldName, longestFieldName));
            }
        }
        return expectationResults;
    }

    private List<ExpectationResult> checkDissectors() {
        List<ExpectationResult> results = new ArrayList<>();
        Set<Dissector> dissectors = parser.getAllDissectors();
        for (Dissector dissector: dissectors) {
            for (String output: dissector.getPossibleOutput()) {
//                String baseMsg = "Dissector " + dissector.getClass().getSimpleName() + " outputs " + output;
                String[] splitOutput = output.split(":", 2);
                if (!splitOutput[0].toUpperCase(Locale.ENGLISH).equals(splitOutput[0])) {
                    results.add(new ExpectationResult("Dissector input type is UPPERcase",
                        dissector.getClass().getSimpleName() + " --> " + output,
                        splitOutput[0].toUpperCase(Locale.ENGLISH),
                        "\"" + splitOutput[0] + "\" is not fully uppercase."));
                }

                if (!splitOutput[1].toLowerCase(Locale.ENGLISH).equals(splitOutput[1])) {
                    results.add(new ExpectationResult("Dissector output name is lowercase",
                        dissector.getClass().getSimpleName() + " --> " + output,
                        splitOutput[1].toLowerCase(Locale.ENGLISH),
                        "\"" + splitOutput[1] + "\" is not fully uppercase."));
                }
            }
            assertNotNull(
                dissector.prepareForDissect("Checking for non-existing input handling",
                                            "Checking for non-existing output handling"),
                "Dissector::prepareForDissect may NEVER return null!!");
        }
        return results;
    }

    private String padding(String name, int longestFieldName) {
        return padding(name, longestFieldName, ' ');
    }

    private String padding(String name, int longestFieldName, char pad) {
        int length = longestFieldName - name.length();
        if (length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(pad);
        }
        return sb.toString();
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
                for (String fieldName : result.getAllNames()) {
                    longestFieldName = Math.max(longestFieldName, fieldName.length());
                }
                for (String fieldName : possibleFieldNames) {
                    String value = result.getStringValue(fieldName);
                    if (value == null) {
                        String wildcardEnd = ".*";
                        if (fieldName.endsWith(wildcardEnd)) {
                            String fieldNamePrefix = fieldName.substring(0, fieldName.length() - wildcardEnd.length());
                            List<String> allNames = result.getAllNames().stream()
                                .filter(f -> f.startsWith(fieldNamePrefix))
                                .filter(f -> !f.substring(fieldNamePrefix.length()+1).contains("."))
                                .sorted()
                                .collect(Collectors.toList());

                            if (allNames.isEmpty()) {
                                LOG.info("Found values for {}{} = []", fieldName, padding(fieldName, longestFieldName));
                            } else {
                                LOG.info("Found values for {}", fieldName);
                                for (String name : allNames) {
                                    LOG.info("             --> {}{} = {}", name, padding(name, longestFieldName), result.getStringValue(name));
                                }
                            }
                            continue;
                        } else {
                            value = "<<<null>>>";
                        }
                    }
                    LOG.info("Found value for {}{}  = {}", fieldName, padding(fieldName, longestFieldName), value);
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
            return STRING_ONLY;
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) {
            DummyDissector dummyDissector = (DummyDissector)newInstance;
            dummyDissector.fieldName = fieldName;
            dummyDissector.outputType = outputType;
        }
    }
}
