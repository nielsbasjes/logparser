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
package nl.basjes.parse.core.annotation;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.test.NullValuesDissector;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;

import static nl.basjes.parse.core.Parser.SetterPolicy.NOT_NULL;

public class TestFieldSettersNotNull {

    public static class TestRecordString extends TestRecord {
        @Field(value = {
            "ANY:any",
            "STRING:string",
            "INT:int",
            "LONG:long",
            "FLOAT:float",
            "DOUBLE:double" },
            setterPolicy = NOT_NULL)
        public void set(String name, String value) {
            setStringValue(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = NOT_NULL) public void setA(String n, String v) { set(n, v); }
        @Field(value = "STRING:string", setterPolicy = NOT_NULL) public void setS(String n, String v) { set(n, v); }
        @Field(value = "INT:int",       setterPolicy = NOT_NULL) public void setI(String n, String v) { set(n, v); }
        @Field(value = "LONG:long",     setterPolicy = NOT_NULL) public void setL(String n, String v) { set(n, v); }
        @Field(value = "FLOAT:float",   setterPolicy = NOT_NULL) public void setF(String n, String v) { set(n, v); }
        @Field(value = "DOUBLE:double", setterPolicy = NOT_NULL) public void setD(String n, String v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    public static class TestRecordLong extends TestRecord {
        @Field(value = {
            "ANY:any",
            "INT:int",
            "LONG:long" },
            setterPolicy = NOT_NULL)
        public void set(String name, Long value) {
            setLongValue(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = NOT_NULL) public void setA(String n, Long v) { set(n, v); }
        @Field(value = "INT:int",       setterPolicy = NOT_NULL) public void setI(String n, Long v) { set(n, v); }
        @Field(value = "LONG:long",     setterPolicy = NOT_NULL) public void setL(String n, Long v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    public static class TestRecordDouble extends TestRecord {
        @Field(value = {
            "ANY:any",
            "FLOAT:float",
            "DOUBLE:double" },
            setterPolicy = NOT_NULL)
        public void set(String name, Double value) {
            setDoubleValue(name, value);
        }

        // CHECKSTYLE.OFF: LeftCurly
        @Field(value = "ANY:any",       setterPolicy = NOT_NULL) public void setA(String n, Double v) { set(n, v); }
        @Field(value = "FLOAT:float",   setterPolicy = NOT_NULL) public void setF(String n, Double v) { set(n, v); }
        @Field(value = "DOUBLE:double", setterPolicy = NOT_NULL) public void setD(String n, Double v) { set(n, v); }
        // CHECKSTYLE.ON: LeftCurly
    }

    @Test
    public void testString() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecordString.class)
            .setRootType("INPUT")
            .addDissector(new NullValuesDissector())
            .parse("Doesn't matter")

            .noString("ANY:any")
            .noString("STRING:string")
            .noString("INT:int")
            .noString("LONG:long")
            .noString("FLOAT:float")
            .noString("DOUBLE:double");
    }

    @Test
    public void testLong() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecordLong.class)
            .setRootType("INPUT")
            .addDissector(new NullValuesDissector())
            .parse("Doesn't matter")

            .noLong("ANY:any")
            .noLong("INT:int")
            .noLong("LONG:long");
    }

    @Test
    public void testDouble() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecordDouble.class)
            .setRootType("INPUT")
            .addDissector(new NullValuesDissector())
            .parse("Doesn't matter")

            .noDouble("ANY:any")
            .noDouble("FLOAT:float")
            .noDouble("DOUBLE:double");
    }

}
