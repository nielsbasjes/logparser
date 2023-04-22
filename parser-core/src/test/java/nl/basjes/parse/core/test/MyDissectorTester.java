/*
 * Apache HTTPD logparsing made easy
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

package nl.basjes.parse.core.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyDissectorTester {
    @Test
    void testPrefixInserter(){
        DissectorTester tester = DissectorTester.create();

        tester.withPathPrefix("Prefix.");
        assertEquals("Foo:Prefix.Bar", tester.addPrefix("Foo:Bar"));

        tester.withPathPrefix("");
        assertEquals("Foo:Bar", tester.addPrefix("Foo:Bar"));

        tester.withPathPrefix("Prefix.");
        assertEquals("Foo:Prefix.Bar", tester.addPrefix("Foo:Bar"));

        tester.withPathPrefix(null);
        assertEquals("Foo:Bar", tester.addPrefix("Foo:Bar"));

        tester.withPathPrefix("Prefix.");
        assertEquals("Foo:Prefix.Bar", tester.addPrefix("Foo:Bar"));
    }

}
