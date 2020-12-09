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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.reference.BarDissector;
import nl.basjes.parse.core.reference.FooDissector;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.NormalValuesDissector;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static org.junit.Assert.assertEquals;

public class TestBadAPIUsage {

    @Test(expected = InvalidDissectorException.class)
    public void testChangingInputTypeShouldNotBePossibleByDefault() throws InvalidDissectorException {
        new DissectorTester.DummyDissector().setInputType("Change should not be allowed");
    }

    @Test
    public void testDissectorString(){
        assertEquals(
            "{ BarDissector : BARINPUT --> " +
                "[LONG:barlong, FLOAT:barfloat, STRING:barstring, INT:barint, DOUBLE:bardouble, ANY:barany] }",
            new BarDissector().toString());
    }

    public static class NullInputDissector extends Dissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname) {
        }
        @Override
        public String getInputType() {
            return null;
        }
        @Override
        public List<String> getPossibleOutput() {
            return Collections.singletonList("FOO:foo");
        }
        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return STRING_ONLY;
        }
    }

    @Test(expected = InvalidDissectorException.class)
    public void testNullInputHandling() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(Object.class).addDissector(new NullInputDissector()).parse("Foo");
    }

    public static class NullOutputDissector extends Dissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname) {
        }
        @Override
        public String getInputType() {
            return "SOMETHING";
        }
        @Override
        public List<String> getPossibleOutput() {
            return Collections.emptyList();
        }
        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return STRING_ONLY;
        }
    }

    @Test(expected = InvalidDissectorException.class)
    public void testNullOutputHandling() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(Object.class).addDissector(new NullOutputDissector()).parse("Foo");
    }

    public static class EmptyOutputDissector extends Dissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname) {
        }
        @Override
        public String getInputType() {
            return "SOMETHING";
        }
        @Override
        public List<String> getPossibleOutput() {
            return Collections.emptyList();
        }
        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return STRING_ONLY;
        }
    }
    @Test(expected = InvalidDissectorException.class)
    public void testEmptyOutputHandling() throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(Object.class).addDissector(new EmptyOutputDissector()).parse("Foo");
    }

    @Test(expected = MissingDissectorsException.class)
    public void testFailZeroDissectors() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .failOnMissingDissectors()
            .addParseTarget("setStringValue", "SOMETHING:that.is.not.present")
            .addParseTarget("setStringValue", "STRING:string")
            .parse("Doesn't matter");
    }

    @Test(expected = MissingDissectorsException.class)
    public void testFailOnMissingDissectors() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addDissector(new FooDissector())
            .addDissector(new BarDissector())
            .failOnMissingDissectors()
            .addParseTarget("setStringValue", "SOMETHING:that.is.not.present")
            .addParseTarget("setStringValue", "STRING:string")
            .parse("Doesn't matter");
    }

    @Test
    public void testIgnoreMissingDissectors() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addDissector(new FooDissector())
            .addDissector(new BarDissector())
            .ignoreMissingDissectors()
            .addParseTarget("setStringValue", Parser.SetterPolicy.ALWAYS, "SOMETHING:that.is.not.present")
            .addParseTarget("setStringValue", Parser.SetterPolicy.ALWAYS, "STRING:string")
            .parse("Doesn't matter");
    }


    @Test(expected = NoSuchMethodException.class)
    public void testNoSuchSetter() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addDissector(new FooDissector())
            .addDissector(new BarDissector())
            .ignoreMissingDissectors()
            .addParseTarget("NoSetterWithThisName", "SOMETHING:that.is.not.present")
            .parse("Doesn't matter");
    }

    @Test
    public void testBadParameters() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addDissector(new FooDissector())
            .addDissector(new BarDissector())
            .ignoreMissingDissectors()
            .addParseTarget("setStringValue", Parser.SetterPolicy.ALWAYS, "SOMETHING:that.is.not.present")
            .addParseTarget("setStringValue", Parser.SetterPolicy.ALWAYS, "STRING:string")
            .addParseTarget("setStringValue", null)
            .addParseTarget((Method)null, "foo")
            .parse("Doesn't matter");
    }

    @Test
    public void testFieldCleanup() throws NoSuchMethodException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        new Parser<>(TestRecord.class)
            .setRootType("INPUT")
            .addDissector(new NormalValuesDissector())
            .addParseTarget("setStringValue", "stRinG:stRinG")
            .parse("Doesn't matter")
            .expectString("STRING:string", "FortyTwo");
    }


}
