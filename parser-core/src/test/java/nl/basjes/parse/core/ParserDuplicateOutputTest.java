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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;

import static nl.basjes.parse.core.Casts.STRING_ONLY;

public class ParserDuplicateOutputTest {

    public abstract static class TestDissector extends SimpleDissector {
        private static HashMap<String, EnumSet<Casts>> dissectorConfig = new HashMap<>();
        static {
            dissectorConfig.put("STRING:output",   STRING_ONLY);
        }

        public TestDissector() {
            super("INPUT", dissectorConfig);
        }
    }

    public static class FooDissector extends TestDissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            parsable.addDissection(inputname, "STRING", "output", "foo");
        }
    }
    public static class BarDissector extends TestDissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            parsable.addDissection(inputname, "STRING", "output", "bar");
        }
    }

    // Verify: If you have two dissectors doing the SAME input/output you should get BOTH
    @Test
    public void testParseString() {
        DissectorTester.create()
            .verbose()
            .withDissector(new FooDissector())
            .withDissector(new BarDissector())
            .withInput("SomeThing")
            .printPossible()
            .expect("STRING:output", "foo")
            .expect("STRING:output", "bar")
            .checkExpectations();
    }
}
