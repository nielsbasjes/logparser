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
package nl.basjes.parse.core.test;

import org.junit.Test;

public class TestUltimateDummyDissector {

    @Test
    public void verifyUltimateDummyDissector() {
        DissectorTester.create()
            .withDissector(new UltimateDummyDissector())
            .withInput("Doesn't matter")
            .expect("ANY:any",        "42")
            .expect("ANY:any",        42L)
            .expect("ANY:any",        42D)
            .expect("STRING:string",  "FortyTwo")
            .expectAbsentLong("STRING:string")
            .expectAbsentDouble("STRING:string")
            .expect("INT:int",        "42")
            .expect("INT:int",        42L)
            .expectAbsentDouble("INT:int")
            .expect("LONG:long",      "42")
            .expect("LONG:long",      42L)
            .expectAbsentDouble("LONG:long")
            .expect("FLOAT:float",    "42.0")
            .expectAbsentLong("FLOAT:float")
            .expect("FLOAT:float",    42D)
            .expect("DOUBLE:double",  "42.0")
            .expectAbsentLong("DOUBLE:double")
            .expect("DOUBLE:double",  42D)
//            .verbose()
            .checkExpectations();
    }

}
