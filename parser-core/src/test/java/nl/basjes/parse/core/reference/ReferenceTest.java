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
package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import org.junit.jupiter.api.Test;

public class ReferenceTest {

    @Test
    public void verifyFoo() {
        DissectorTester.create()
            .withDissector(new FooDissector())
            .withInput("Doesn't matter")
            .expect("ANY:fooany",        "42")
            .expect("ANY:fooany",        42L)
            .expect("ANY:fooany",        42D)
            .expect("STRING:foostring",  "42")
            .expectAbsentLong("STRING:foostring")
            .expectAbsentDouble("STRING:foostring")
            .expect("INT:fooint",        "42")
            .expect("INT:fooint",        42L)
            .expectAbsentDouble("INT:fooint")
            .expect("LONG:foolong",      "42")
            .expect("LONG:foolong",      42L)
            .expectAbsentDouble("LONG:foolong")
            .expect("FLOAT:foofloat",    "42.0")
            .expectAbsentLong("FLOAT:foofloat")
            .expect("FLOAT:foofloat",    42D)
            .expect("DOUBLE:foodouble",  "42.0")
            .expectAbsentLong("DOUBLE:foodouble")
            .expect("DOUBLE:foodouble",  42D)
//            .verbose()
            .checkExpectations();
    }

    @Test
    public void verifyBar() {
        DissectorTester.create()
            .withDissector(new BarDissector())
            .withInput("Doesn't matter")
            .expect("ANY:barany",        "42")
            .expect("ANY:barany",        42L)
            .expect("ANY:barany",        42D)
            .expect("STRING:barstring",  "42")
            .expectAbsentLong("STRING:barstring")
            .expectAbsentDouble("STRING:barstring")
            .expect("INT:barint",        "42")
            .expect("INT:barint",        42L)
            .expectAbsentDouble("INT:barint")
            .expect("LONG:barlong",      "42")
            .expect("LONG:barlong",      42L)
            .expectAbsentDouble("LONG:barlong")
            .expect("FLOAT:barfloat",    "42.0")
            .expectAbsentLong("FLOAT:barfloat")
            .expect("FLOAT:barfloat",    42D)
            .expect("DOUBLE:bardouble",  "42.0")
            .expectAbsentLong("DOUBLE:bardouble")
            .expect("DOUBLE:bardouble",  42D)
//            .verbose()
            .checkExpectations();
    }

    @Test
    public void runManuallyCombined(){
        Parser<TestRecord> parser = new Parser<>(TestRecord.class);
        parser.addDissector(new FooDissector());
        parser.addDissector(new BarDissector());
        parser.addTypeRemapping("foostring", "BARINPUT");
        parser.setRootType(new FooDissector().getInputType());

        DissectorTester.create()
            .withParser(parser)
            .withInput("BlaBlaBla")

            .expect("ANY:fooany",                   "42")
            .expect("ANY:fooany",                   42L)
            .expect("ANY:fooany",                   42D)
            .expect("STRING:foostring",             "42")
            .expectAbsentLong("STRING:foostring")
            .expectAbsentDouble("STRING:foostring")
            .expect("INT:fooint",                   "42")
            .expect("INT:fooint",                   42L)
            .expectAbsentDouble("INT:fooint")
            .expect("LONG:foolong",                 "42")
            .expect("LONG:foolong",                 42L)
            .expectAbsentDouble("LONG:foolong")
            .expect("FLOAT:foofloat",               "42.0")
            .expectAbsentLong("FLOAT:foofloat")
            .expect("FLOAT:foofloat",               42D)
            .expect("DOUBLE:foodouble",             "42.0")
            .expectAbsentLong("DOUBLE:foodouble")
            .expect("DOUBLE:foodouble",             42D)

            .expect("ANY:foostring.barany",         "42")
            .expect("ANY:foostring.barany",         42L)
            .expect("ANY:foostring.barany",         42D)
            .expect("STRING:foostring.barstring",   "42")
            .expectAbsentLong("STRING:foostring.barstring")
            .expectAbsentDouble("STRING:foostring.barstring")
            .expect("INT:foostring.barint",         "42")
            .expect("INT:foostring.barint",         42L)
            .expectAbsentDouble("INT:foostring.barint")
            .expect("LONG:foostring.barlong",       "42")
            .expect("LONG:foostring.barlong",       42L)
            .expectAbsentDouble("LONG:foostring.barlong")
            .expect("FLOAT:foostring.barfloat",     "42.0")
            .expectAbsentLong("FLOAT:foostring.barfloat")
            .expect("FLOAT:foostring.barfloat",     42D)
            .expect("DOUBLE:foostring.bardouble",   "42.0")
            .expectAbsentLong("DOUBLE:foostring.bardouble")
            .expect("DOUBLE:foostring.bardouble",   42D)

            .checkExpectations();
    }

    @Test
    public void runAutomaticallyAddedBar(){
        DissectorTester.create()
            .withDissector(new FooSpecialDissector())
            .withInput("BlaBlaBla")

            .expect("ANY:fooany",                   "42")
            .expect("ANY:fooany",                   42L)
            .expect("ANY:fooany",                   42D)
            .expect("STRING:foostring",             "42")
            .expectAbsentLong("STRING:foostring")
            .expectAbsentDouble("STRING:foostring")
            .expect("INT:fooint",                 "42")
            .expect("INT:fooint",                 42L)
            .expectAbsentDouble("INT:fooint")
            .expect("LONG:foolong",                 "42")
            .expect("LONG:foolong",                 42L)
            .expectAbsentDouble("LONG:foolong")
            .expect("FLOAT:foofloat",             "42.0")
            .expectAbsentLong("FLOAT:foofloat")
            .expect("FLOAT:foofloat",             42D)
            .expect("DOUBLE:foodouble",             "42.0")
            .expectAbsentLong("DOUBLE:foodouble")
            .expect("DOUBLE:foodouble",             42D)

            .expect("ANY:foostring.barany",         "42")
            .expect("ANY:foostring.barany",         42L)
            .expect("ANY:foostring.barany",         42D)
            .expect("STRING:foostring.barstring",   "42")
            .expectAbsentLong("STRING:foostring.barstring")
            .expectAbsentDouble("STRING:foostring.barstring")
            .expect("INT:foostring.barint",       "42")
            .expect("INT:foostring.barint",       42L)
            .expectAbsentDouble("INT:foostring.barint")
            .expect("LONG:foostring.barlong",       "42")
            .expect("LONG:foostring.barlong",       42L)
            .expectAbsentDouble("LONG:foostring.barlong")
            .expect("FLOAT:foostring.barfloat",   "42.0")
            .expectAbsentLong("FLOAT:foostring.barfloat")
            .expect("FLOAT:foostring.barfloat",   42D)
            .expect("DOUBLE:foostring.bardouble",   "42.0")
            .expectAbsentLong("DOUBLE:foostring.bardouble")
            .expect("DOUBLE:foostring.bardouble",   42D)

            .checkExpectations();
    }

}
