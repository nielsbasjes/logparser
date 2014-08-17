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
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserTestCasts {

    public static class TestDisector extends Disector {

        public TestDisector() {
            // Empty
        }

        protected void initializeNewInstance(Disector newInstance) {
            // Empty
        }

        @Override
        public void disect(Parsable<?> parsable, final String inputname) throws DisectionFailure {
            parsable.addDisection(inputname, "OUTPUT_TYPE", "string_null", null       , Casts.STRING_ONLY);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "string_good", "123"      , Casts.STRING_ONLY);

            parsable.addDisection(inputname, "OUTPUT_TYPE", "long_null"  , null       , Casts.LONG_ONLY);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "long_bad"   , "Something"      , Casts.LONG_ONLY);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "long_good"  , "123"      , Casts.LONG_ONLY);

            parsable.addDisection(inputname, "OUTPUT_TYPE", "double_null", null       , Casts.DOUBLE_ONLY);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "double_bad" , "Something", Casts.DOUBLE_ONLY);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "double_good", "123"      , Casts.DOUBLE_ONLY);

            parsable.addDisection(inputname, "OUTPUT_TYPE", "string_long_null"  , null  , Casts.STRING_OR_LONG);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "string_double_null" ,null  , Casts.STRING_OR_DOUBLE);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "multi_null"        , null  , Casts.STRING_OR_LONG_OR_DOUBLE);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "string_long_good"  , "123" , Casts.STRING_OR_LONG);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "string_double_good", "123" , Casts.STRING_OR_DOUBLE);
            parsable.addDisection(inputname, "OUTPUT_TYPE", "multi_good"        , "123" , Casts.STRING_OR_LONG_OR_DOUBLE);

        }

        @Override
        public String getInputType() {
            return "INPUT_TYPE";
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("OUTPUT_TYPE:string_null");
            result.add("OUTPUT_TYPE:string_good");
            result.add("OUTPUT_TYPE:long_null");
            result.add("OUTPUT_TYPE:long_bad");
            result.add("OUTPUT_TYPE:long_good");
            result.add("OUTPUT_TYPE:double_null");
            result.add("OUTPUT_TYPE:double_bad");
            result.add("OUTPUT_TYPE:double_good");
            result.add("OUTPUT_TYPE:string_long_null");
            result.add("OUTPUT_TYPE:string_double_null");
            result.add("OUTPUT_TYPE:multi_null");
            result.add("OUTPUT_TYPE:string_long_good");
            result.add("OUTPUT_TYPE:string_double_good");
            result.add("OUTPUT_TYPE:multi_good");
            return result;
        }

        @Override
        public void prepareForDisect(String inputname, String outputname) {
        }

        @Override
        public void prepareForRun() {
        }
    }

    public static class TestParser<RECORD> extends Parser<RECORD> {
        public TestParser(final Class<RECORD> clazz) {
            super(clazz);
            addDisector(new TestDisector());
            setRootType("INPUT_TYPE");
        }
    }

    public static class TestRecord {
        public int count = 0;
        @Field({"OUTPUT_TYPE:string_null",
                "OUTPUT_TYPE:string_long_null",
                "OUTPUT_TYPE:string_double_null",
                "OUTPUT_TYPE:multi_null",
        })
        public void setStringNull(String name, String value) {
            assertEquals(null, value);
            count++;
        }

        @Field({"OUTPUT_TYPE:string_good",
                "OUTPUT_TYPE:string_long_good",
                "OUTPUT_TYPE:string_double_good",
                "OUTPUT_TYPE:multi_good",
        })
        public void setStringGood(String name, String value) {
            assertEquals("123", value);
            count++;
        }

        @Field({"OUTPUT_TYPE:long_null",
                "OUTPUT_TYPE:long_bad",
                "OUTPUT_TYPE:string_long_null",
                "OUTPUT_TYPE:multi_null",
        })
        public void setLongNull(String name, Long value) {
            assertEquals(null, value);
            count++;
        }

        @Field({"OUTPUT_TYPE:long_good",
                "OUTPUT_TYPE:string_long_good",
                "OUTPUT_TYPE:multi_good",
        })
        public void setLongGood(String name, Long value) {
            assertEquals(new Long(123L), value);
            count++;
        }

        @Field({"OUTPUT_TYPE:double_null",
                "OUTPUT_TYPE:double_bad",
                "OUTPUT_TYPE:string_double_null",
                "OUTPUT_TYPE:multi_null",
        })
        public void setDoubleNull(String name, Double value) {
            assertEquals(null, value);
            count++;
        }

        @Field({"OUTPUT_TYPE:double_good",
                "OUTPUT_TYPE:string_double_good",
                "OUTPUT_TYPE:multi_good",
        })
        public void setDoubleGood(String name, Double value) {
            assertEquals(123D, value, 0.0001D);
            count++;
        }

        @Field({"OUTPUT_TYPE:long_null",
                "OUTPUT_TYPE:long_bad",
                "OUTPUT_TYPE:long_good",
                "OUTPUT_TYPE:string_long_null",
                "OUTPUT_TYPE:string_long_good",
        })
        public void setLongWrongSignature(String name, Double value) {
            fail("This setter uses Double but that is not allowed for \""+name+"\" ");
        }

        @Field({"OUTPUT_TYPE:double_null",
                "OUTPUT_TYPE:double_bad",
                "OUTPUT_TYPE:double_good",
                "OUTPUT_TYPE:string_double_null",
                "OUTPUT_TYPE:string_double_good",
        })
        public void setDoubleWrongSignature(String name, Long value) {
            fail("This setter uses Long but that is not allowed for \""+name+"\" ");
        }
    }

    @Test
    public void testValidCasting() throws Exception {
        Parser<TestRecord> parser = new TestParser<>(TestRecord.class);
        TestRecord output = new TestRecord();
        parser.parse(output, "Something");
        assertEquals(22, output.count);
    }

}
