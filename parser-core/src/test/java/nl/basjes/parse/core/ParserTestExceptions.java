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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import nl.basjes.parse.core.exceptions.CannotChangeDisectorsAfterConstructionException;
import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.InvalidFieldMethodSignature;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;

import org.junit.Test;

import ch.qos.logback.classic.Level;

public class ParserTestExceptions {

    public static class TestDisector extends Disector {
        private String inputType;
        private String outputType;
        private String outputName;

        public TestDisector(String inputType, String outputType, String outputName) {
            this.inputType  = inputType;
            this.outputType = outputType;
            this.outputName = outputName;
        }

        @Override
        public void disect(Parsable<?> parsable, final String inputname) throws DisectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            parsable.addDisection(inputname, outputType, outputName, field.getValue());
        }

        @Override
        public String getInputType() {
            return inputType;
        }

        @Override
        public String[] getPossibleOutput() {
            String[] result = new String[1];
            result[0] = outputType + ":" + outputName;
            return result;
        }

        @Override
        public void prepareForDisect(String inputname, String outputname) {
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

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) throws IOException, MissingDisectorsException, InvalidDisectorException {
            super(clazz);
            addDisector(new TestDisectorOne());
            addDisector(new TestDisectorTwo());
            addDisector(new TestDisectorThree());
            addDisector(new TestDisectorFour());
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

        public void badSetter1() {
        }

        public void badSetter2(String name, Long value) {
        }
    }

    @Test
    public void testParseString() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("setValue2", String.class, String.class), params);

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
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("setValue2", String.class, String.class), params);

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
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("badSetter1"), params);

        parser.getPossiblePaths(3);
    }

    @Test(expected=InvalidFieldMethodSignature.class)
    public void testBadSetter2() throws Exception {
//        setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(TestRecord.class.getMethod("badSetter2", String.class, Long.class), params);

        parser.getPossiblePaths(3);
    }

    public class BrokenTestDisector extends Disector {

        public BrokenTestDisector() {
        }

        @Override
        public void disect(Parsable<?> parsable, String inputname) throws DisectionFailure {
        }

        @Override
        public String getInputType() {
            return "foo";
        }

        @Override
        public String[] getPossibleOutput() {
            String[] result = new String[1];
            result[0] = "foo:bar";
            return result;
        }

        @Override
        public void prepareForDisect(String inputname, String outputname) {
        }

        @Override
        public void prepareForRun() throws InvalidDisectorException {
            throw new InvalidDisectorException();
        }
    }

//    @Test(expected=InvalidDisectorException.class)
//    public void testBrokenDisector() throws Exception {
//        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);
//        parser.addDisector(new BrokenTestDisector());
//
//        parser.getPossiblePaths(3);
//    }

    @Test(expected=CannotChangeDisectorsAfterConstructionException.class)
    public void testChangeAfterStart() throws Exception {
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);
        parser.getPossiblePaths(3);
        parser.addDisector(new BrokenTestDisector());
    }

    @Test(expected=MissingDisectorsException.class)
    public void testDropDisector1() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        parser.dropDisector(TestDisectorOne.class);
        parser.getPossiblePaths();
    }

    @Test
    public void testDropDisector2() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        parser.dropDisector(TestDisectorOne.class);
        parser.addDisector(new TestDisectorOne());
        parser.getPossiblePaths();
    }

    @Test(expected=CannotChangeDisectorsAfterConstructionException.class)
    public void testDropDisector3() throws Exception {
        // setLoggingLevel(Level.ALL);
        Parser<TestRecord> parser = new TestParser<TestRecord>(TestRecord.class);

        parser.getPossiblePaths(0);
        parser.dropDisector(TestDisectorOne.class);
    }
    
}
