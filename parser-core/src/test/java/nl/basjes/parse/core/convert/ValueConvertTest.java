/*
 * Apache HTTPD logparsing made easy
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
package nl.basjes.parse.core.convert;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static org.junit.Assert.assertTrue;

public class ValueConvertTest {

    @Test
    public void verifyTypeConversionStoM() {
        DissectorTester.create()
            .withDissector("something", new SecondsToMilliseconds())
            .withDissector(new MillisecondsToSeconds())
            .withInput("12345") // Which is in seconds because that dissector is 'first'
            .expect("SECONDS:something", "12345")
            .expect("MILLISECONDS:something", "12345000")
//            .verbose()
            .checkExpectations();
    }

    @Test
    public void verifyTypeConversionMtoS() {
        DissectorTester.create()
            .withDissector("something", new MillisecondsToSeconds())
            .withDissector(new SecondsToMilliseconds())
            .withInput("12345000") // Which is in MILLIseconds because that dissector is 'first'
            .expect("SECONDS:something", "12345")
            .expect("MILLISECONDS:something", "12345000")
//            .verbose()
            .checkExpectations();
    }

    @Test
    public void verifyTypeConversionPossibleFields() {
        List<String> possible = DissectorTester.create()
            .withDissector("something", new MillisecondsToSeconds())
            .withDissector(new SecondsToMilliseconds())
            .withInput("12345000") // Which is in MILLIseconds because that dissector is 'first'
            .getPossible();

        assertTrue(possible.contains("MILLISECONDS:something"));
        assertTrue(possible.contains("SECONDS:something"));
    }

    public abstract static class TypeConvertBaseDissector extends SimpleDissector {
        protected String inputType;
        protected String outputType;

        private static HashMap<String, EnumSet<Casts>> fillOutputConfig(String outputType, EnumSet<Casts> casts) {
            HashMap<String, EnumSet<Casts>> typeConvertConfig = new HashMap<>();
            typeConvertConfig.put(outputType + ":", casts);
            return typeConvertConfig;
        }

        public TypeConvertBaseDissector(String nInputType, String nOutputType) {
            super(nInputType, fillOutputConfig(nOutputType, STRING_OR_LONG));
            inputType = nInputType;
            outputType = nOutputType;
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
            super.initializeNewInstance(newInstance);
            ((TypeConvertBaseDissector) newInstance).inputType = inputType;
            ((TypeConvertBaseDissector) newInstance).outputType = outputType;
        }
    }

    public static class SecondsToMilliseconds extends TypeConvertBaseDissector {

        public SecondsToMilliseconds() {
            super("SECONDS", "MILLISECONDS");
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            parsable.addDissection(inputname, "MILLISECONDS", "", value.getLong() * 1000);
        }
    }

    public static class MillisecondsToSeconds extends TypeConvertBaseDissector {
        public MillisecondsToSeconds() {
            super("MILLISECONDS", "SECONDS");
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            parsable.addDissection(inputname, "SECONDS", "", value.getLong() / 1000);
        }
    }

}
