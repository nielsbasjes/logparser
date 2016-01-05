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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ParserTestNormal {

    public static class TestDissector extends Dissector {
        private String      inputType;
        private String      outputType;
        private String      outputName;
        private final Set<String> outputNames = new HashSet<>();

        public TestDissector(String inputType, String outputType, String outputName) {
            this.inputType = inputType;
            this.outputType = outputType;
            this.outputName = outputName;
            this.outputNames.add(outputName);
        }

        public void init(String inputtype, String outputtype, String outputname) {
            this.inputType = inputtype;
            this.outputType = outputtype;
            this.outputName = outputname;
            this.outputNames.add(outputname);
        }

        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            return true; // Everything went right
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) {
            ((TestDissector)newInstance).init(inputType, outputType, outputName);
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            for (String outputname : outputNames) {
                parsable.addDissection(inputname, outputType, outputname, field.getValue());
            }
        }

        @Override
        public String getInputType() {
            return inputType;
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add(outputType + ":" + outputName);
            return result;
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            String name = outputname;
            String prefix = inputname + '.';
            if (outputname.startsWith(prefix)) {
                name = outputname.substring(prefix.length());
            }
            outputNames.add(name);
            return Casts.STRING_ONLY;
        }

        @Override
        public void prepareForRun() {
        }
    }

    public static class TestDissectorOne extends TestDissector {
        public TestDissectorOne() {
            super("INPUTTYPE", "SOMETYPE", "output1");
        }
    }

    public static class TestDissectorTwo extends TestDissector {
        public TestDissectorTwo() {
            super("INPUTTYPE", "OTHERTYPE", "output2");
        }
    }

    public static class TestDissectorThree extends TestDissector {
        public TestDissectorThree() {
            super("SOMETYPE", "FOO", "foo");
        }
    }

    public static class TestDissectorFour extends TestDissector {
        public TestDissectorFour() {
            super("SOMETYPE", "BAR", "bar");
        }
    }

    public static class TestDissectorWildCard extends TestDissector {
        public TestDissectorWildCard() {
            super("SOMETYPE", "WILD", "*");
        }

    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new TestDissectorOne());
            addDissector(new TestDissectorTwo());
            addDissector(new TestDissectorThree());
            addDissector(new TestDissectorFour());
            addDissector(new TestDissectorWildCard());
            setRootType("INPUTTYPE");
        }
    }

    @Test
    public void testParseString() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<ParserTestNormalTestRecord> parser = new TestParser<>(ParserTestNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(ParserTestNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        parser.dropDissector(TestDissectorWildCard.class);
        parser.addDissector(new TestDissectorWildCard());

        ParserTestNormalTestRecord output = new ParserTestNormalTestRecord();
        parser.parse(output, "Something");
        assertEquals("SOMETYPE1:SOMETYPE:output1:Something", output.getOutput1());
        assertEquals("OTHERTYPE2:OTHERTYPE:output2:Something", output.getOutput2());
        assertEquals("SOMETYPE3:SOMETYPE:output1:Something", output.getOutput3a());
        assertEquals("OTHERTYPE3:OTHERTYPE:output2:Something", output.getOutput3b());
        assertEquals("X=SOMETYPE:SOMETYPE:output1:Something", output.getOutput4a());
        assertEquals("Y=OTHERTYPE:OTHERTYPE:output2:Something", output.getOutput4b());
        assertEquals("X=SOMETYPE:SOMETYPE:output1:Something=SOMETYPE:SOMETYPE:output1:Something", output.getOutput5a());
        assertEquals("Y=OTHERTYPE:OTHERTYPE:output2:Something=OTHERTYPE:OTHERTYPE:output2:Something", output.getOutput5b());
        assertEquals("Z=FOO:FOO:output1.foo:Something", output.getOutput6());
        assertEquals("Z=BAR:BAR:output1.bar:Something", output.getOutput7());
        assertEquals("Z=WILD:WILD:output1.wild:Something", output.getOutput8());
    }

    // ---------------------------------------------
    @Test
    public void testParseStringInstantiate() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<ParserTestNormalTestRecord> parser = new TestParser<>(ParserTestNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(ParserTestNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        ParserTestNormalTestRecord output = parser.parse("Something");

        assertEquals("SOMETYPE1:SOMETYPE:output1:Something", output.getOutput1());
        assertEquals("OTHERTYPE2:OTHERTYPE:output2:Something", output.getOutput2());
        assertEquals("SOMETYPE3:SOMETYPE:output1:Something", output.getOutput3a());
        assertEquals("OTHERTYPE3:OTHERTYPE:output2:Something", output.getOutput3b());
        assertEquals("X=SOMETYPE:SOMETYPE:output1:Something", output.getOutput4a());
        assertEquals("Y=OTHERTYPE:OTHERTYPE:output2:Something", output.getOutput4b());
        assertEquals("X=SOMETYPE:SOMETYPE:output1:Something=SOMETYPE:SOMETYPE:output1:Something", output.getOutput5a());
        assertEquals("Y=OTHERTYPE:OTHERTYPE:output2:Something=OTHERTYPE:OTHERTYPE:output2:Something", output.getOutput5b());
        assertEquals("Z=FOO:FOO:output1.foo:Something", output.getOutput6());
        assertEquals("Z=BAR:BAR:output1.bar:Something", output.getOutput7());
        assertEquals("Z=WILD:WILD:output1.wild:Something", output.getOutput8());
    }

    // ---------------------------------------------

    @Test(expected = MissingDissectorsException.class)
    public void testMissingDissector() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<ParserTestNormalTestRecord> parser = new TestParser<>(ParserTestNormalTestRecord.class);

        // Cripple the parser
        parser.dropDissector(TestDissectorTwo.class);

        ParserTestNormalTestRecord output = new ParserTestNormalTestRecord();
        parser.parse(output, "Something"); // Should fail.
    }

    @Test
    public void testGetPossiblePaths() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<ParserTestNormalTestRecord> parser = new TestParser<>(ParserTestNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(ParserTestNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        List<String> paths = parser.getPossiblePaths(3);
        for (String path : paths) {
            System.out.println("XXX " + path);
        }

    }

}
