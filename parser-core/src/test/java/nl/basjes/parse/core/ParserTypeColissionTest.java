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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTypeColissionTest {

    public static class TestDissector extends Dissector {
        private String inputType;
        private String outputType;
        private String outputName;
        private String salt; // Each value that comes in is appended with this "salt"

        public TestDissector(String inputType, String outputType, String outputName, String salt) {
            this.inputType = inputType;
            this.outputType = outputType;
            this.outputName = outputName;
            this.salt = salt;
        }

        public void init(String inputtype, String outputtype, String outputname, String saltt) {
            this.inputType = inputtype;
            this.outputType = outputtype;
            this.outputName = outputname;
            this.salt = saltt;
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) {
            ((TestDissector)newInstance).init(inputType, outputType, outputName, salt);
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname)
            throws DissectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            parsable.addDissection(inputname, outputType, outputName, field.getValue().getString() + salt);
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
            return STRING_ONLY;
        }
    }

    public static class TestDissectorOne extends TestDissector {
        public TestDissectorOne() {
            super("INPUTTYPE", "SOMETYPE", "output", "+1");
        }
    }

    public static class TestDissectorTwo extends TestDissector {
        public TestDissectorTwo() {
            super("INPUTTYPE", "OTHERTYPE", "output", "+2");
        }
    }

    public static class TestDissectorSubOne extends TestDissector {
        public TestDissectorSubOne() {
            super("SOMETYPE", "SOMESUBTYPE", "output", "+S1");
        }
    }

    public static class TestDissectorSubTwo extends TestDissector {
        public TestDissectorSubTwo() {
            super("OTHERTYPE", "OTHERSUBTYPE", "output", "+S2");
        }
    }

    public static class TestDissectorSubSubOne extends TestDissector {
        public TestDissectorSubSubOne() {
            super("SOMESUBTYPE", "SOMESUBSUBTYPE", "output", "+SS1");
        }
    }

    public static class TestDissectorSubSubTwo extends TestDissector {
        public TestDissectorSubSubTwo() {
            super("OTHERSUBTYPE", "OTHERSUBSUBTYPE", "output", "+SS2");
        }
    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDissector(new TestDissectorOne());
            addDissector(new TestDissectorTwo());
            addDissector(new TestDissectorSubOne());
            addDissector(new TestDissectorSubTwo());
            addDissector(new TestDissectorSubSubOne());
            addDissector(new TestDissectorSubSubTwo());
            setRootType("INPUTTYPE");
        }
    }

    public static class TestRecord {
        private String output1 = "xxx";

        @Field("SOMETYPE:output")
        public void setValue1(String name, String value) {
            output1 = name + ":" + value;
        }

        private String output2 = "xxx";

        @Field("OTHERTYPE:output")
        public void setValue2(String name, String value) {
            output2 = name + ":" + value;
        }

        private String output3 = "xxx";

        @Field("SOMESUBSUBTYPE:output.output.output")
        public void setValue3(String name, String value) {
            output3 = name + ":" + value;
        }

        private String output4 = "xxx";

        @Field("OTHERSUBSUBTYPE:output.output.output")
        public void setValue4(String name, String value) {
            output4 = name + ":" + value;
        }

    }

    @Test
    void testParseString() throws Exception {
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);

        TestRecord output = new TestRecord();
        parser.parse(output, "Something");
        assertEquals("SOMETYPE:output:Something+1", output.output1);
        assertEquals("OTHERTYPE:output:Something+2", output.output2);
        assertEquals("SOMESUBSUBTYPE:output.output.output:Something+1+S1+SS1", output.output3);
        assertEquals("OTHERSUBSUBTYPE:output.output.output:Something+2+S2+SS2", output.output4);
    }

}
