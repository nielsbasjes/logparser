/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
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
package nl.basjes.parse.core.annotation;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.core.Parser.SetterPolicy.ALWAYS;
import static nl.basjes.parse.core.annotation.Utils.isPresent;

public class TestFieldSettersAlways {

    public static class TestRecordString {
        private Map<String, String> strings = new TreeMap<>();

        private void set(String name, String value) {
            strings.put(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = ALWAYS) public void setA(String n, String v) { set(n, v); }
        @Field(value = "STRING:string", setterPolicy = ALWAYS) public void setS(String n, String v) { set(n, v); }
        @Field(value = "INT:int",       setterPolicy = ALWAYS) public void setI(String n, String v) { set(n, v); }
        @Field(value = "LONG:long",     setterPolicy = ALWAYS) public void setL(String n, String v) { set(n, v); }
        @Field(value = "FLOAT:float",   setterPolicy = ALWAYS) public void setF(String n, String v) { set(n, v); }
        @Field(value = "DOUBLE:double", setterPolicy = ALWAYS) public void setD(String n, String v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    public static class TestRecordLong {
        private Map<String, Long> longs = new TreeMap<>();

        private void set(String name, Long value) {
            longs.put(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = ALWAYS) public void setA(String n, Long v) { set(n, v); }
        @Field(value = "INT:int",       setterPolicy = ALWAYS) public void setI(String n, Long v) { set(n, v); }
        @Field(value = "LONG:long",     setterPolicy = ALWAYS) public void setL(String n, Long v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    public static class TestRecordDouble {
        private Map<String, Double> doubles = new TreeMap<>();

        private void set(String name, Double value) {
            doubles.put(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = ALWAYS) public void setA(String n, Double v) { set(n, v); }
        @Field(value = "FLOAT:float",   setterPolicy = ALWAYS) public void setF(String n, Double v) { set(n, v); }
        @Field(value = "DOUBLE:double", setterPolicy = ALWAYS) public void setD(String n, Double v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }


    @Test
    public void testString() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        Parser<TestRecordString> parser = new Parser<>(TestRecordString.class);
        parser.setRootType("INPUT");
        parser.addDissector(new Utils.SetAllTypesNormalDissector());
        TestRecordString testRecord = parser.parse("Doesn't matter");

        isPresent(testRecord.strings,  "ANY:any",       "42");
        isPresent(testRecord.strings,  "STRING:string", "FortyTwo");
        isPresent(testRecord.strings,  "INT:int",       "42");
        isPresent(testRecord.strings,  "LONG:long",     "42");
        isPresent(testRecord.strings,  "FLOAT:float",   "42.0");
        isPresent(testRecord.strings,  "DOUBLE:double", "42.0");
    }

    @Test
    public void testLong() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        Parser<TestRecordLong> parser = new Parser<>(TestRecordLong.class);
        parser.setRootType("INPUT");
        parser.addDissector(new Utils.SetAllTypesNormalDissector());
        TestRecordLong testRecord = parser.parse("Doesn't matter");

        isPresent(testRecord.longs, "ANY:any",    42L);
        isPresent(testRecord.longs, "INT:int",    42L);
        isPresent(testRecord.longs, "LONG:long",  42L);
    }

    @Test
    public void testDouble() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        Parser<TestRecordDouble> parser = new Parser<>(TestRecordDouble.class);
        parser.setRootType("INPUT");
        parser.addDissector(new Utils.SetAllTypesNormalDissector());
        TestRecordDouble testRecord = parser.parse("Doesn't matter");

        isPresent(testRecord.doubles, "ANY:any",       42D);
        isPresent(testRecord.doubles, "FLOAT:float",   42D);
        isPresent(testRecord.doubles, "DOUBLE:double", 42D);
    }

}
