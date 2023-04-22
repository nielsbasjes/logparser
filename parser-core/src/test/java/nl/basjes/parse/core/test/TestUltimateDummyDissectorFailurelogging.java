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
package nl.basjes.parse.core.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestUltimateDummyDissectorFailurelogging {

    @Test
    void verifyErrorSituation() {
        AssertionError assertionError = assertThrows(AssertionError.class, () ->
            DissectorTester.create()
                .withDissector(new NormalValuesDissector())
                .withInput("Doesn't matter")
                // All good
                .expect("ANY:any", "42")
                .expect("ANY:any", 42L)
                .expect("ANY:any", 42D)
                .expect("STRING:string", "FortyTwo")
                .expectAbsentLong("STRING:string")
                .expectAbsentDouble("STRING:string")
                .expect("INT:int", "42")
                .expect("INT:int", 42L)
                .expectAbsentDouble("INT:int")
                .expect("LONG:long", "42")
                .expect("LONG:long", 42L)
                .expectAbsentDouble("LONG:long")
                .expect("FLOAT:float", "42.0")
                .expectAbsentLong("FLOAT:float")
                .expect("FLOAT:float", 42D)
                .expect("DOUBLE:double", "42.0")
                .expectAbsentLong("DOUBLE:double")
                .expect("DOUBLE:double", 42D)

                // All bad
                .expect("ANY:any", "43")
                .expect("ANY:any", 43L)
                .expect("ANY:any", 43D)
                .expect("STRING:string", "FortyThree")
                .expectAbsentString("STRING:string")
                .expect("INT:int", "43")
                .expect("INT:int", 43L)
                .expectAbsentLong("INT:int")
                .expect("LONG:long", "43")
                .expect("LONG:long", 43L)
                .expectAbsentLong("LONG:long")
                .expect("FLOAT:float", "43.0")
                .expectAbsentDouble("FLOAT:float")
                .expect("FLOAT:float", 43D)
                .expect("DOUBLE:double", "43.0")
                .expectAbsentDouble("DOUBLE:double")
                .expect("DOUBLE:double", 43D)
                .verbose()
                .checkExpectations()
        );

        assertEquals("\n" +
            "[     ] /========================================================================\\\n"+
            "[     ] | Field         | Check         | Expected Value | Fail reason           |\n"+
            "[     ] +---------------+---------------+----------------+-----------------------+\n"+
            "[ERROR] | ANY:any       | String value  |             43 | Wrong value: 42       |\n" +
            "[ERROR] | DOUBLE:double | String value  |           43.0 | Wrong value: 42.0     |\n" +
            "[ERROR] | FLOAT:float   | String value  |           43.0 | Wrong value: 42.0     |\n" +
            "[ERROR] | INT:int       | String value  |             43 | Wrong value: 42       |\n" +
            "[ERROR] | LONG:long     | String value  |             43 | Wrong value: 42       |\n" +
            "[ERROR] | STRING:string | String value  |     FortyThree | Wrong value: FortyTwo |\n" +
            "[ERROR] | ANY:any       | Long value    |             43 | Wrong value: 42       |\n" +
            "[ERROR] | INT:int       | Long value    |             43 | Wrong value: 42       |\n" +
            "[ERROR] | LONG:long     | Long value    |             43 | Wrong value: 42       |\n" +
            "[ERROR] | ANY:any       | Double value  |           43.0 | Wrong value: 42.0     |\n" +
            "[ERROR] | DOUBLE:double | Double value  |           43.0 | Wrong value: 42.0     |\n" +
            "[ERROR] | FLOAT:float   | Double value  |           43.0 | Wrong value: 42.0     |\n" +
            "[ERROR] | STRING:string | String absent |       FortyTwo | Present               |\n" +
            "[     ] | STRING:string | Long absent   |                |                       |\n" +
            "[     ] | FLOAT:float   | Long absent   |                |                       |\n" +
            "[     ] | DOUBLE:double | Long absent   |                |                       |\n" +
            "[ERROR] | INT:int       | Long absent   |             42 | Present               |\n" +
            "[ERROR] | LONG:long     | Long absent   |             42 | Present               |\n" +
            "[     ] | STRING:string | Double absent |                |                       |\n" +
            "[     ] | INT:int       | Double absent |                |                       |\n" +
            "[     ] | LONG:long     | Double absent |                |                       |\n" +
            "[ERROR] | FLOAT:float   | Double absent |           42.0 | Present               |\n" +
            "[ERROR] | DOUBLE:double | Double absent |           42.0 | Present               |\n" +
            "[     ] \\========================================================================/\n",
            assertionError.getMessage());
    }

}
