/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
package nl.basjes.parse.core.annotation;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.test.NormalValuesDissector;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;

import static nl.basjes.parse.core.Parser.SetterPolicy.ALWAYS;

public class TestFieldSettersAlwaysSeparate {

    public static class TestRecordString extends TestRecord {
        private void set(String name, String value) {
            setStringValue(name, value);
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

    public static class TestRecordLong extends TestRecord {
        private void set(String name, Long value) {
            setLongValue(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = ALWAYS) public void setA(String n, Long v) { set(n, v); }
        @Field(value = "INT:int",       setterPolicy = ALWAYS) public void setI(String n, Long v) { set(n, v); }
        @Field(value = "LONG:long",     setterPolicy = ALWAYS) public void setL(String n, Long v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    public static class TestRecordDouble extends TestRecord {
        private void set(String name, Double value) {
            setDoubleValue(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = ALWAYS) public void setA(String n, Double v) { set(n, v); }
        @Field(value = "FLOAT:float",   setterPolicy = ALWAYS) public void setF(String n, Double v) { set(n, v); }
        @Field(value = "DOUBLE:double", setterPolicy = ALWAYS) public void setD(String n, Double v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    @Test
    public void testString() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecordString.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .parse("Doesn't matter")

            .expectString("ANY:any",       "42")
            .expectString("STRING:string", "FortyTwo")
            .expectString("INT:int",       "42")
            .expectString("LONG:long",     "42")
            .expectString("FLOAT:float",   "42.0")
            .expectString("DOUBLE:double", "42.0");
    }

    @Test
    public void testLong() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecordLong.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .parse("Doesn't matter")

            .expectLong("ANY:any",    42L)
            .expectLong("INT:int",    42L)
            .expectLong("LONG:long",  42L);
    }

    @Test
    public void testDouble() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecordDouble.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .parse("Doesn't matter")

            .expectDouble("ANY:any",       42D)
            .expectDouble("FLOAT:float",   42D)
            .expectDouble("DOUBLE:double", 42D);
    }

}
