/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ParserTestNormal {

    public static class TestDisector extends Disector {
        private String      inputType;
        private String      outputType;
        private String      outputName;
        private final Set<String> outputNames = new HashSet<>();

        public TestDisector(String inputType, String outputType, String outputName) {
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

        protected void initializeNewInstance(Disector newInstance) {
            ((TestDisector)newInstance).init(inputType, outputType, outputName);
        }

        @Override
        public void disect(Parsable<?> parsable, String inputname) throws DisectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            for (String outputname : outputNames) {
                parsable.addDisection(inputname, outputType, outputname, field.getValue());
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
        public EnumSet<Casts> prepareForDisect(String inputname, String outputname) {
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

    public static class TestDisectorOne extends TestDisector {
        public TestDisectorOne() {
            super("INPUTTYPE", "SOMETYPE", "output1");
        }
    }

    public static class TestDisectorTwo extends TestDisector {
        public TestDisectorTwo() {
            super("INPUTTYPE", "OTHERTYPE", "output2");
        }
    }

    public static class TestDisectorThree extends TestDisector {
        public TestDisectorThree() {
            super("SOMETYPE", "FOO", "foo");
        }
    }

    public static class TestDisectorFour extends TestDisector {
        public TestDisectorFour() {
            super("SOMETYPE", "BAR", "bar");
        }
    }

    public static class TestDisectorWildCard extends TestDisector {
        public TestDisectorWildCard() {
            super("SOMETYPE", "WILD", "*");
        }

    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDisector(new TestDisectorOne());
            addDisector(new TestDisectorTwo());
            addDisector(new TestDisectorThree());
            addDisector(new TestDisectorFour());
            addDisector(new TestDisectorWildCard());
            setRootType("INPUTTYPE");
        }
    }

    @Test
    public void testParseString() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<ParserTestNormalTestRecord> parser = new TestParser<>(ParserTestNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(ParserTestNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        parser.dropDisector(TestDisectorWildCard.class);
        parser.addDisector(new TestDisectorWildCard());

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

    @Test(expected = MissingDisectorsException.class)
    public void testMissingDisector() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<ParserTestNormalTestRecord> parser = new TestParser<>(ParserTestNormalTestRecord.class);

        // Cripple the parser
        parser.dropDisector(TestDisectorTwo.class);

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
