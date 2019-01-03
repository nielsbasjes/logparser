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
import nl.basjes.parse.core.test.EmptyValuesDissector;
import nl.basjes.parse.core.test.NormalValuesDissector;
import nl.basjes.parse.core.test.NullValuesDissector;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;

import static nl.basjes.parse.core.Parser.SetterPolicy.ALWAYS;
import static nl.basjes.parse.core.Parser.SetterPolicy.NOT_EMPTY;
import static nl.basjes.parse.core.Parser.SetterPolicy.NOT_NULL;

// CHECKSTYLE.OFF: ParenPad
// CHECKSTYLE.OFF: LeftCurly
public class TestFieldSetters {

    public static class TestFieldSettersRecord extends TestRecord {
        private void setS(String prefix, String name, String value) {
            setStringValue(prefix + "-" + name, value);
        }
        private void setL(String prefix, String name, Long value) {
            setLongValue(prefix + "-" + name, value);
        }
        private void setD(String prefix, String name, Double value) {
            setDoubleValue(prefix + "-" + name, value);
        }

        @Field(value = "ANY:any"                                   ) public void setAD(String n, String v){ setS("D", n, v); }
        @Field(value = "STRING:string"                             ) public void setSD(String n, String v){ setS("D", n, v); }
        @Field(value = "INT:int"                                   ) public void setID(String n, String v){ setS("D", n, v); }
        @Field(value = "LONG:long"                                 ) public void setLD(String n, String v){ setS("D", n, v); }
        @Field(value = "FLOAT:float"                               ) public void setFD(String n, String v){ setS("D", n, v); }
        @Field(value = "DOUBLE:double"                             ) public void setDD(String n, String v){ setS("D", n, v); }
        @Field(value = "ANY:any"                                   ) public void setAD(String n, Long   v){ setL("D", n, v); }
        @Field(value = "STRING:string"                             ) public void setSD(String n, Long   v){ setL("D", n, v); }
        @Field(value = "INT:int"                                   ) public void setID(String n, Long   v){ setL("D", n, v); }
        @Field(value = "LONG:long"                                 ) public void setLD(String n, Long   v){ setL("D", n, v); }
        @Field(value = "FLOAT:float"                               ) public void setFD(String n, Long   v){ setL("D", n, v); }
        @Field(value = "DOUBLE:double"                             ) public void setDD(String n, Long   v){ setL("D", n, v); }
        @Field(value = "ANY:any"                                   ) public void setAD(String n, Double v){ setD("D", n, v); }
        @Field(value = "STRING:string"                             ) public void setSD(String n, Double v){ setD("D", n, v); }
        @Field(value = "INT:int"                                   ) public void setID(String n, Double v){ setD("D", n, v); }
        @Field(value = "LONG:long"                                 ) public void setLD(String n, Double v){ setD("D", n, v); }
        @Field(value = "FLOAT:float"                               ) public void setFD(String n, Double v){ setD("D", n, v); }
        @Field(value = "DOUBLE:double"                             ) public void setDD(String n, Double v){ setD("D", n, v); }

        @Field(value = "ANY:any",          setterPolicy = ALWAYS   ) public void setAA(String n, String v){ setS("A", n, v); }
        @Field(value = "STRING:string",    setterPolicy = ALWAYS   ) public void setSA(String n, String v){ setS("A", n, v); }
        @Field(value = "INT:int",          setterPolicy = ALWAYS   ) public void setIA(String n, String v){ setS("A", n, v); }
        @Field(value = "LONG:long",        setterPolicy = ALWAYS   ) public void setLA(String n, String v){ setS("A", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = ALWAYS   ) public void setFA(String n, String v){ setS("A", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = ALWAYS   ) public void setDA(String n, String v){ setS("A", n, v); }
        @Field(value = "ANY:any",          setterPolicy = ALWAYS   ) public void setAA(String n, Long   v){ setL("A", n, v); }
        @Field(value = "STRING:string",    setterPolicy = ALWAYS   ) public void setSA(String n, Long   v){ setL("A", n, v); }
        @Field(value = "INT:int",          setterPolicy = ALWAYS   ) public void setIA(String n, Long   v){ setL("A", n, v); }
        @Field(value = "LONG:long",        setterPolicy = ALWAYS   ) public void setLA(String n, Long   v){ setL("A", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = ALWAYS   ) public void setFA(String n, Long   v){ setL("A", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = ALWAYS   ) public void setDA(String n, Long   v){ setL("A", n, v); }
        @Field(value = "ANY:any",          setterPolicy = ALWAYS   ) public void setAA(String n, Double v){ setD("A", n, v); }
        @Field(value = "STRING:string",    setterPolicy = ALWAYS   ) public void setSA(String n, Double v){ setD("A", n, v); }
        @Field(value = "INT:int",          setterPolicy = ALWAYS   ) public void setIA(String n, Double v){ setD("A", n, v); }
        @Field(value = "LONG:long",        setterPolicy = ALWAYS   ) public void setLA(String n, Double v){ setD("A", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = ALWAYS   ) public void setFA(String n, Double v){ setD("A", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = ALWAYS   ) public void setDA(String n, Double v){ setD("A", n, v); }

        @Field(value = "ANY:any",          setterPolicy = NOT_NULL ) public void setAN(String n, String v){ setS("N", n, v); }
        @Field(value = "STRING:string",    setterPolicy = NOT_NULL ) public void setSN(String n, String v){ setS("N", n, v); }
        @Field(value = "INT:int",          setterPolicy = NOT_NULL ) public void setIN(String n, String v){ setS("N", n, v); }
        @Field(value = "LONG:long",        setterPolicy = NOT_NULL ) public void setLN(String n, String v){ setS("N", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = NOT_NULL ) public void setFN(String n, String v){ setS("N", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = NOT_NULL ) public void setDN(String n, String v){ setS("N", n, v); }
        @Field(value = "ANY:any",          setterPolicy = NOT_NULL ) public void setAN(String n, Long   v){ setL("N", n, v); }
        @Field(value = "STRING:string",    setterPolicy = NOT_NULL ) public void setSN(String n, Long   v){ setL("N", n, v); }
        @Field(value = "INT:int",          setterPolicy = NOT_NULL ) public void setIN(String n, Long   v){ setL("N", n, v); }
        @Field(value = "LONG:long",        setterPolicy = NOT_NULL ) public void setLN(String n, Long   v){ setL("N", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = NOT_NULL ) public void setFN(String n, Long   v){ setL("N", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = NOT_NULL ) public void setDN(String n, Long   v){ setL("N", n, v); }
        @Field(value = "ANY:any",          setterPolicy = NOT_NULL ) public void setAN(String n, Double v){ setD("N", n, v); }
        @Field(value = "STRING:string",    setterPolicy = NOT_NULL ) public void setSN(String n, Double v){ setD("N", n, v); }
        @Field(value = "INT:int",          setterPolicy = NOT_NULL ) public void setIN(String n, Double v){ setD("N", n, v); }
        @Field(value = "LONG:long",        setterPolicy = NOT_NULL ) public void setLN(String n, Double v){ setD("N", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = NOT_NULL ) public void setFN(String n, Double v){ setD("N", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = NOT_NULL ) public void setDN(String n, Double v){ setD("N", n, v); }

        @Field(value = "ANY:any",          setterPolicy = NOT_EMPTY) public void setAE(String n, String v){ setS("E", n, v); }
        @Field(value = "STRING:string",    setterPolicy = NOT_EMPTY) public void setSE(String n, String v){ setS("E", n, v); }
        @Field(value = "INT:int",          setterPolicy = NOT_EMPTY) public void setIE(String n, String v){ setS("E", n, v); }
        @Field(value = "LONG:long",        setterPolicy = NOT_EMPTY) public void setLE(String n, String v){ setS("E", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = NOT_EMPTY) public void setFE(String n, String v){ setS("E", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = NOT_EMPTY) public void setDE(String n, String v){ setS("E", n, v); }
        @Field(value = "ANY:any",          setterPolicy = NOT_EMPTY) public void setAE(String n, Long   v){ setL("E", n, v); }
        @Field(value = "STRING:string",    setterPolicy = NOT_EMPTY) public void setSE(String n, Long   v){ setL("E", n, v); }
        @Field(value = "INT:int",          setterPolicy = NOT_EMPTY) public void setIE(String n, Long   v){ setL("E", n, v); }
        @Field(value = "LONG:long",        setterPolicy = NOT_EMPTY) public void setLE(String n, Long   v){ setL("E", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = NOT_EMPTY) public void setFE(String n, Long   v){ setL("E", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = NOT_EMPTY) public void setDE(String n, Long   v){ setL("E", n, v); }
        @Field(value = "ANY:any",          setterPolicy = NOT_EMPTY) public void setAE(String n, Double v){ setD("E", n, v); }
        @Field(value = "STRING:string",    setterPolicy = NOT_EMPTY) public void setSE(String n, Double v){ setD("E", n, v); }
        @Field(value = "INT:int",          setterPolicy = NOT_EMPTY) public void setIE(String n, Double v){ setD("E", n, v); }
        @Field(value = "LONG:long",        setterPolicy = NOT_EMPTY) public void setLE(String n, Double v){ setD("E", n, v); }
        @Field(value = "FLOAT:float",      setterPolicy = NOT_EMPTY) public void setFE(String n, Double v){ setD("E", n, v); }
        @Field(value = "DOUBLE:double",    setterPolicy = NOT_EMPTY) public void setDE(String n, Double v){ setD("E", n, v); }
    }

    @Test
    public void testNormalValues() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestFieldSettersRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .parse("Doesn't matter")

            // Default (== Always)
            .expectString(  "D-ANY:any",        "42")
            .expectString(  "D-STRING:string",  "FortyTwo")
            .expectString(  "D-INT:int",        "42")
            .expectString(  "D-LONG:long",      "42")
            .expectString(  "D-FLOAT:float",    "42.0")
            .expectString(  "D-DOUBLE:double",  "42.0")
            .expectLong(    "D-ANY:any",        42L)
            .noLong(     "D-STRING:string")
            .expectLong(    "D-INT:int",        42L)
            .expectLong(    "D-LONG:long",      42L)
            .noLong(     "D-FLOAT:float")
            .noLong(     "D-DOUBLE:double")
            .expectDouble(  "D-ANY:any",        42D)
            .noDouble(   "D-STRING:string")
            .noDouble(   "D-INT:int")
            .noDouble(   "D-LONG:long")
            .expectDouble(  "D-FLOAT:float",    42D)
            .expectDouble(  "D-DOUBLE:double",  42D)

            // Always
            .expectString(  "A-ANY:any",        "42")
            .expectString(  "A-STRING:string",  "FortyTwo")
            .expectString(  "A-INT:int",        "42")
            .expectString(  "A-LONG:long",      "42")
            .expectString(  "A-FLOAT:float",    "42.0")
            .expectString(  "A-DOUBLE:double",  "42.0")
            .expectLong(    "A-ANY:any",        42L)
            .noLong(     "A-STRING:string")
            .expectLong(    "A-INT:int",        42L)
            .expectLong(    "A-LONG:long",      42L)
            .noLong(     "A-FLOAT:float")
            .noLong(     "A-DOUBLE:double")
            .expectDouble(  "A-ANY:any",        42D)
            .noDouble(   "A-STRING:string")
            .noDouble(   "A-INT:int")
            .noDouble(   "A-LONG:long")
            .expectDouble(  "A-FLOAT:float",    42D)
            .expectDouble(  "A-DOUBLE:double",  42D)

            // Not Null
            .expectString(  "N-ANY:any",        "42")
            .expectString(  "N-STRING:string",  "FortyTwo")
            .expectString(  "N-INT:int",        "42")
            .expectString(  "N-LONG:long",      "42")
            .expectString(  "N-FLOAT:float",    "42.0")
            .expectString(  "N-DOUBLE:double",  "42.0")
            .expectLong(    "N-ANY:any",        42L)
            .noLong(     "N-STRING:string")
            .expectLong(    "N-INT:int",        42L)
            .expectLong(    "N-LONG:long",      42L)
            .noLong(     "N-FLOAT:float")
            .noLong(     "N-DOUBLE:double")
            .expectDouble(  "N-ANY:any",        42D)
            .noDouble(   "N-STRING:string")
            .noDouble(   "N-INT:int")
            .noDouble(   "N-LONG:long")
            .expectDouble(  "N-FLOAT:float",    42D)
            .expectDouble(  "N-DOUBLE:double",  42D)

            // Not Empty
            .expectString(  "E-ANY:any",        "42")
            .expectString(  "E-STRING:string",  "FortyTwo")
            .expectString(  "E-INT:int",        "42")
            .expectString(  "E-LONG:long",      "42")
            .expectString(  "E-FLOAT:float",    "42.0")
            .expectString(  "E-DOUBLE:double",  "42.0")
            .expectLong(    "E-ANY:any",        42L)
            .noLong(     "E-STRING:string")
            .expectLong(    "E-INT:int",        42L)
            .expectLong(    "E-LONG:long",      42L)
            .noLong(     "E-FLOAT:float")
            .noLong(     "E-DOUBLE:double")
            .expectDouble(  "E-ANY:any",        42D)
            .noDouble(   "E-STRING:string")
            .noDouble(   "E-INT:int")
            .noDouble(   "E-LONG:long")
            .expectDouble(  "E-FLOAT:float",    42D)
            .expectDouble(  "E-DOUBLE:double",  42D);
    }

    @Test
    public void testEmptyValues() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        Parser<TestFieldSettersRecord> parser = new Parser<>(TestFieldSettersRecord.class);
        parser.setRootType("INPUT");
        parser.addDissector(new EmptyValuesDissector());
        TestFieldSettersRecord testRecord = parser.parse("Doesn't matter");

        testRecord
            // Default (== Always)
            .expectString(  "D-ANY:any",        "")
            .expectString(  "D-STRING:string",  "")
            .expectString(  "D-INT:int",        "")
            .expectString(  "D-LONG:long",      "")
            .expectString(  "D-FLOAT:float",    "")
            .expectString(  "D-DOUBLE:double",  "")
            .expectLong(    "D-ANY:any",        null)
            .noLong(     "D-STRING:string")
            .expectLong(    "D-INT:int",        null)
            .expectLong(    "D-LONG:long",      null)
            .noLong(     "D-FLOAT:float")
            .noLong(     "D-DOUBLE:double")
            .expectDouble(  "D-ANY:any",        null)
            .noDouble(   "D-STRING:string")
            .noDouble(   "D-INT:int")
            .noDouble(   "D-LONG:long")
            .expectDouble(  "D-FLOAT:float",    null)
            .expectDouble(  "D-DOUBLE:double",  null)

            // Always
            .expectString(  "A-ANY:any",        "")
            .expectString(  "A-STRING:string",  "")
            .expectString(  "A-INT:int",        "")
            .expectString(  "A-LONG:long",      "")
            .expectString(  "A-FLOAT:float",    "")
            .expectString(  "A-DOUBLE:double",  "")
            .expectLong(    "A-ANY:any",        null)
            .noLong(     "A-STRING:string")
            .expectLong(    "A-INT:int",        null)
            .expectLong(    "A-LONG:long",      null)
            .noLong(     "A-FLOAT:float")
            .noLong(     "A-DOUBLE:double")
            .expectDouble(  "A-ANY:any",        null)
            .noDouble(   "A-STRING:string")
            .noDouble(   "A-INT:int")
            .noDouble(   "A-LONG:long")
            .expectDouble(  "A-FLOAT:float",    null)
            .expectDouble(  "A-DOUBLE:double",  null)

            // Not Null
            .expectString(  "N-ANY:any",        "")
            .expectString(  "N-STRING:string",  "")
            .expectString(  "N-INT:int",        "")
            .expectString(  "N-LONG:long",      "")
            .expectString(  "N-FLOAT:float",    "")
            .expectString(  "N-DOUBLE:double",  "")
            .noLong(     "N-ANY:any")
            .noLong(     "N-STRING:string")
            .noLong(     "N-INT:int")
            .noLong(     "N-LONG:long")
            .noLong(     "N-FLOAT:float")
            .noLong(     "N-DOUBLE:double")
            .noDouble(   "N-ANY:any")
            .noDouble(   "N-STRING:string")
            .noDouble(   "N-INT:int")
            .noDouble(   "N-LONG:long")
            .noDouble(   "N-FLOAT:float")
            .noDouble(   "N-DOUBLE:double")

            // Not Empty
            .noString(  "E-ANY:any")
            .noString(  "E-STRING:string")
            .noString(  "E-INT:int")
            .noString(  "E-LONG:long")
            .noString(  "E-FLOAT:float")
            .noString(  "E-DOUBLE:double")
            .noLong(    "E-ANY:any")
            .noLong(    "E-STRING:string")
            .noLong(    "E-INT:int")
            .noLong(    "E-LONG:long")
            .noLong(    "E-FLOAT:float")
            .noLong(    "E-DOUBLE:double")
            .noDouble(  "E-ANY:any")
            .noDouble(  "E-STRING:string")
            .noDouble(  "E-INT:int")
            .noDouble(  "E-LONG:long")
            .noDouble(  "E-FLOAT:float")
            .noDouble(  "E-DOUBLE:double");

    }

    @Test
    public void testNullValues() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        Parser<TestFieldSettersRecord> parser = new Parser<>(TestFieldSettersRecord.class);
        parser.setRootType("INPUT");
        parser.addDissector(new NullValuesDissector());
        TestFieldSettersRecord testRecord = parser.parse("Doesn't matter");

        testRecord
            // Default (== Always)
            .expectString(  "D-ANY:any",        null)
            .expectString(  "D-STRING:string",  null)
            .expectString(  "D-INT:int",        null)
            .expectString(  "D-LONG:long",      null)
            .expectString(  "D-FLOAT:float",    null)
            .expectString(  "D-DOUBLE:double",  null)
            .expectLong(    "D-ANY:any",        null)
            .noLong(     "D-STRING:string")
            .expectLong(    "D-INT:int",        null)
            .expectLong(    "D-LONG:long",      null)
            .noLong(     "D-FLOAT:float")
            .noLong(     "D-DOUBLE:double")
            .expectDouble(  "D-ANY:any",        null)
            .noDouble(   "D-STRING:string")
            .noDouble(   "D-INT:int")
            .noDouble(   "D-LONG:long")
            .expectDouble(  "D-FLOAT:float",    null)
            .expectDouble(  "D-DOUBLE:double",  null)

            // Always
            .expectString(  "A-ANY:any",        null)
            .expectString(  "A-STRING:string",  null)
            .expectString(  "A-INT:int",        null)
            .expectString(  "A-LONG:long",      null)
            .expectString(  "A-FLOAT:float",    null)
            .expectString(  "A-DOUBLE:double",  null)
            .expectLong(    "A-ANY:any",        null)
            .noLong(     "A-STRING:string")
            .expectLong(    "A-INT:int",        null)
            .expectLong(    "A-LONG:long",      null)
            .noLong(     "A-FLOAT:float")
            .noLong(     "A-DOUBLE:double")
            .expectDouble(  "A-ANY:any",        null)
            .noDouble(   "A-STRING:string")
            .noDouble(   "A-INT:int")
            .noDouble(   "A-LONG:long")
            .expectDouble(  "A-FLOAT:float",    null)
            .expectDouble(  "A-DOUBLE:double",  null)

            // Not Null
            .noString(  "N-ANY:any")
            .noString(  "N-STRING:string")
            .noString(  "N-INT:int")
            .noString(  "N-LONG:long")
            .noString(  "N-FLOAT:float")
            .noString(  "N-DOUBLE:double")
            .noLong(    "N-ANY:any")
            .noLong(    "N-STRING:string")
            .noLong(    "N-INT:int")
            .noLong(    "N-LONG:long")
            .noLong(    "N-FLOAT:float")
            .noLong(    "N-DOUBLE:double")
            .noDouble(  "N-ANY:any")
            .noDouble(  "N-STRING:string")
            .noDouble(  "N-INT:int")
            .noDouble(  "N-LONG:long")
            .noDouble(  "N-FLOAT:float")
            .noDouble(  "N-DOUBLE:double")

            // Not Empty
            .noString(  "E-ANY:any")
            .noString(  "E-STRING:string")
            .noString(  "E-INT:int")
            .noString(  "E-LONG:long")
            .noString(  "E-FLOAT:float")
            .noString(  "E-DOUBLE:double")
            .noLong(    "E-ANY:any")
            .noLong(    "E-STRING:string")
            .noLong(    "E-INT:int")
            .noLong(    "E-LONG:long")
            .noLong(    "E-FLOAT:float")
            .noLong(    "E-DOUBLE:double")
            .noDouble(  "E-ANY:any")
            .noDouble(  "E-STRING:string")
            .noDouble(  "E-INT:int")
            .noDouble(  "E-LONG:long")
            .noDouble(  "E-FLOAT:float")
            .noDouble(  "E-DOUBLE:double");
    }

}
