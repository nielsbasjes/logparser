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

import nl.basjes.parse.core.exceptions.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTestExceptions {

    public static class TestDissector extends Dissector {
        private String inputType;
        private String outputType;
        private String outputName;

        public TestDissector(String inputType, String outputType, String outputName) {
            init(inputType, outputType, outputName);
        }

        public final void init(String inputtype, String outputtype, String outputname) {
            this.inputType  = inputtype;
            this.outputType = outputtype;
            this.outputName = outputname;
        }

        protected void initializeNewInstance(Dissector newInstance) {
            ((TestDissector)newInstance).init(inputType, outputType, outputName);
        }

        @Override
        public void dissect(Parsable<?> parsable, final String inputname) throws DissectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            parsable.addDissection(inputname, outputType, outputName, field.getValue());
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

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new TestDissectorOne());
            addDissector(new TestDissectorTwo());
            addDissector(new TestDissectorThree());
            addDissector(new TestDissectorFour());
            setRootType("INPUTTYPE");
        }
    }

    public static class TestRecord {
        private String output1 = "xxx";
        @Field("SOMETYPE:output1")
        public void setValue1(String value) {
            output1 = "SOMETYPE1:SOMETYPE:output1:" + value;
        }

        private String output2 = "yyy";
//        @Field("OTHERTYPE:output") --> Set via direct method
        public void setValue2(String name, String value) {
            output2 = "OTHERTYPE2:" + name + ":" + value;
        }

        private String output3a = "xxx";
        private String output3b = "yyy";

        @Field({ "SOMETYPE:output1", "OTHERTYPE:output2" })
        public void setValue3(String name, String value) {
            if (name.startsWith("SOMETYPE:")) {
                output3a = "SOMETYPE3:" + name + ":" + value;
            } else {
                output3b = "OTHERTYPE3:" + name + ":" + value;
            }
        }

        private String output4a = "X";
        private String output4b = "Y";

        @Field({ "SOMETYPE:output1", "OTHERTYPE:output2", "SOMETYPE:output1", "OTHERTYPE:output2" })
        public void setValue4(String name, String value) {
            if (name.startsWith("SOMETYPE:")) {
                output4a = output4a + "=SOMETYPE:" + name + ":" + value;
            } else {
                output4b = output4b + "=OTHERTYPE:" + name + ":" + value;
            }
        }

        private String output5a = "X";
        private String output5b = "Y";

        @Field({ "SOMETYPE:output1", "OTHERTYPE:output2", "SOMETYPE:*", "OTHERTYPE:*" })
        public void setValue5(String name, String value) {
            if (name.startsWith("SOMETYPE:")) {
                output5a = output5a + "=SOMETYPE:" + name + ":" + value;
            } else {
                output5b = output5b + "=OTHERTYPE:" + name + ":" + value;
            }
        }

        private String output6 = "Z";
        @Field({ "FOO:output1.foo"})
        public void setValue6(String name, String value) {
            output6 = output6 + "=FOO:" + name + ":" + value;
        }

        private String output7 = "Z";
        @Field({ "BAR:output1.bar"})
        public void setValue7(String name, String value) {
            output7 = output7 + "=BAR:" + name + ":" + value;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void badSetter1() {
        }

        @SuppressWarnings("UnusedDeclaration")
        public void badSetter2(String name, Float value) {
        }
    }

    @Test
    public void testParseString() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        TestRecord output = new TestRecord();
        parser.parse(output, "Something");
        assertEquals("SOMETYPE1:SOMETYPE:output1:Something", output.output1);
        assertEquals("OTHERTYPE2:OTHERTYPE:output2:Something", output.output2);
        assertEquals("SOMETYPE3:SOMETYPE:output1:Something", output.output3a);
        assertEquals("OTHERTYPE3:OTHERTYPE:output2:Something", output.output3b);
        assertEquals("X=SOMETYPE:SOMETYPE:output1:Something", output.output4a);
        assertEquals("Y=OTHERTYPE:OTHERTYPE:output2:Something", output.output4b);
        assertEquals("X=SOMETYPE:SOMETYPE:output1:Something=SOMETYPE:SOMETYPE:output1:Something", output.output5a);
        assertEquals("Y=OTHERTYPE:OTHERTYPE:output2:Something=OTHERTYPE:OTHERTYPE:output2:Something", output.output5b);
        assertEquals("Z=FOO:FOO:output1.foo:Something", output.output6);
        assertEquals("Z=BAR:BAR:output1.bar:Something", output.output7);
    }

    @Test
    public void testGetPossiblePaths() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        List<String> paths = parser.getPossiblePaths(3);
        for (String path:paths){
            System.out.println("XXX "+path);
        }

//        assertEquals(true, paths.contains("SOMETYPE:output1"));
//        assertEquals(true, paths.contains("FOO:output1.foo"));
//        assertEquals(true, paths.contains("BAR:output1.bar"));
    }



    @Test(expected=InvalidFieldMethodSignature.class)
    public void testBadSetter1() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("badSetter1"), Arrays.asList(params));

        parser.getPossiblePaths(3);
    }

    @Test(expected=InvalidFieldMethodSignature.class)
    public void testBadSetter2() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("badSetter2", String.class, Float.class), Arrays.asList(params));

        parser.getPossiblePaths(3);
    }

    public class BrokenTestDissector extends Dissector {

        public BrokenTestDissector() {
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        }

        @Override
        public String getInputType() {
            return "foo";
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("foo:bar");
            return result;
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return Casts.STRING_ONLY;
        }

        @Override
        public void prepareForRun() throws InvalidDissectorException {
            throw new InvalidDissectorException();
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) {
        }
    }

//    @Test(expected=InvalidDissectorException.class)
//    public void testBrokenDissector() throws Exception {
//        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);
//        parser.addDissector(new BrokenTestDissector());
//
//        parser.getPossiblePaths(3);
//    }

    @Test(expected=CannotChangeDissectorsAfterConstructionException.class)
    public void testChangeAfterStart() throws Exception {
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);
        parser.parse("Something");
        parser.addDissector(new BrokenTestDissector());
    }

    @Test(expected=MissingDissectorsException.class)
    public void testDropDissector1() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        parser.dropDissector(TestDissectorOne.class);
        parser.parse("Something");
    }

    @Test
    public void testDropDissector2() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        parser.dropDissector(TestDissectorOne.class);
        parser.addDissector(new TestDissectorOne());
        parser.getPossiblePaths();
    }

    @Test(expected=CannotChangeDissectorsAfterConstructionException.class)
    public void testDropDissector3() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        parser.parse("Something");
        parser.dropDissector(TestDissectorOne.class);
    }

}
