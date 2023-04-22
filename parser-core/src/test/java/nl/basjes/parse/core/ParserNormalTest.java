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
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.test.NormalValuesDissector;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ParserNormalTest {

    public static class MyDissector extends Dissector {
        private String      inputType;
        private String      outputType;
        private String      outputName;
        private final Set<String> outputNames = new HashSet<>();

        public MyDissector(String inputType, String outputType, String outputName) {
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
        protected void initializeNewInstance(Dissector newInstance) {
            ((MyDissector)newInstance).init(inputType, outputType, outputName);
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
            return STRING_ONLY;
        }
    }

    public static class MyDissectorOne extends MyDissector {
        public MyDissectorOne() {
            super("INPUTTYPE", "SOMETYPE", "output1");
        }
    }

    public static class MyDissectorTwo extends MyDissector {
        public MyDissectorTwo() {
            super("INPUTTYPE", "OTHERTYPE", "output2");
        }
    }

    public static class MyDissectorThree extends MyDissector {
        public MyDissectorThree() {
            super("SOMETYPE", "FOO", "foo");
        }
    }

    public static class MyDissectorFour extends MyDissector {
        public MyDissectorFour() {
            super("SOMETYPE", "BAR", "bar");
        }
    }

    public static class MyDissectorWildCard extends MyDissector {
        public MyDissectorWildCard() {
            super("SOMETYPE", "WILD", "*");
        }

    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new MyDissectorOne());
            addDissector(new MyDissectorTwo());
            addDissector(new MyDissectorThree());
            addDissector(new MyDissectorFour());
            addDissector(new MyDissectorWildCard());
            setRootType("INPUTTYPE");
        }
    }

    @Test
    void testParseString() throws Exception {
        Parser<ParserNormalTestRecord> parser = new TestParser<>(ParserNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser
            .addParseTarget(ParserNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params))
            .dropDissector(MyDissectorWildCard.class)
            .addDissector(new MyDissectorWildCard());

        ParserNormalTestRecord output = new ParserNormalTestRecord();
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
    void testParseStringInstantiate() throws Exception {
        Parser<ParserNormalTestRecord> parser = new TestParser<>(ParserNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(ParserNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        ParserNormalTestRecord output = parser.parse("Something");

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
    void testMissingDissector() {
        Parser<ParserNormalTestRecord> parser = new TestParser<>(ParserNormalTestRecord.class);

        // Cripple the parser
        parser.dropDissector(MyDissectorTwo.class);

        ParserNormalTestRecord output = new ParserNormalTestRecord();

        assertThrows(MissingDissectorsException.class, () -> {
            parser.parse(output, "Something"); // Should fail.
        });
    }

    @Test
    void testGetPossiblePaths() throws Exception {
        Parser<ParserNormalTestRecord> parser = new TestParser<>(ParserNormalTestRecord.class);

        String[] params = {"OTHERTYPE:output2"};
        parser.addParseTarget(ParserNormalTestRecord.class.getMethod("setValue2", String.class, String.class), Arrays.asList(params));

        List<String> paths = parser.getPossiblePaths(3);
        for (String path : paths) {
            System.out.println("XXX " + path);
        }

    }

    @Test
    void testAddTypeRemapping() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addTypeRemapping("string", "STRINGXX")
            .addParseTarget("setStringValue", "STRINGXX:string")
            .addTypeRemapping("string", "STRINGYY")
            .addParseTarget("setStringValue", "STRINGYY:string")
            .parse("Doesn't matter")
            .expectString("STRINGXX:string", "FortyTwo")
            .expectString("STRINGYY:string", "FortyTwo");
    }

    @Test
    void testAddTypeRemappings() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addTypeRemapping("string", "STRINGXX")
            .addParseTarget("setStringValue", "STRINGXX:string")
            .addTypeRemappings(Collections.singletonMap("string", Collections.singleton("STRINGYY")))
            .addParseTarget("setStringValue", "STRINGYY:string")
            .parse("Doesn't matter")
            .expectString("STRINGXX:string", "FortyTwo")
            .expectString("STRINGYY:string", "FortyTwo");
    }

    @Test
    void testSetTypeRemapping() throws NoSuchMethodException, InvalidDissectorException, DissectionFailure {
        try{
            new Parser<>(TestRecord.class)
                .setRootType("INPUT")
                .addDissector(new NormalValuesDissector())
                .addTypeRemapping("string", "STRINGXX")
                .addParseTarget("setStringValue", "STRINGXX:string")
                // Should wipe previous
                .setTypeRemappings(Collections.singletonMap("string", Collections.singleton("STRINGYY")))
                .addParseTarget("setStringValue", "STRINGYY:string")
                .parse("Doesn't matter");

            fail("We should get an exception because the mapping to STRINGXX:string wass removed.");
        } catch (MissingDissectorsException mde) {
            assertTrue(mde.getMessage().contains("STRINGXX:string"));
        }
    }

}
