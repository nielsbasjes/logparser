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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.test.DissectorTester;
import org.junit.jupiter.api.Test;

class TestModUniqueIdDissector {

    @Test
    void testUniqueId1() {
        // This test case was verified using https://github.com/web-online/mod-unique-id-decode
        //$ ./mod_unique_id_uudecoder -i VaGTKApid0AAALpaNo0AAAAC
        //unique_id.stamp = Sun Jul 12 00:05:28 2015
        //unique_id.in_addr = 10.98.119.64
        //unique_id.pid = 47706
        //unique_id.counter = 13965
        //unique_id.threadIndex = 2

        DissectorTester.create()
            .withDissector(new ModUniqueIdDissector())
            .withInput("VaGTKApid0AAALpaNo0AAAAC")
            .expect("TIME.EPOCH:epoch",           "1436652328000")
            .expect("IP:ip",                      "10.98.119.64")
            .expect("PROCESSID:processid",        "47706")
            .expect("COUNTER:counter",            "13965")
            .expect("THREAD_INDEX:threadindex",   "2")
            .checkExpectations();
    }

    @Test
    void testUniqueId2() {
        // This test case was verified using https://github.com/web-online/mod-unique-id-decode
        //$ ./mod_unique_id_uudecoder -i Ucdv38CoEJwAAEusp6EAAADz
        //unique_id.stamp = Sun Jun 23 23:59:59 2013
        //unique_id.in_addr = 192.168.16.156
        //unique_id.pid = 19372
        //unique_id.counter = 42913
        //unique_id.threadIndex = 243

        DissectorTester.create()
            .withDissector(new ModUniqueIdDissector())
            .withInput("Ucdv38CoEJwAAEusp6EAAADz")
            .expect("TIME.EPOCH:epoch",           "1372024799000")
            .expect("IP:ip",                      "192.168.16.156")
            .expect("PROCESSID:processid",        "19372")
            .expect("COUNTER:counter",            "42913")
            .expect("THREAD_INDEX:threadindex",   "243")
            .checkExpectations();
    }

    @Test
    void testBadUniqueIdTooShort() {
        DissectorTester.create()
            .withDissector(new ModUniqueIdDissector())
            .withInput("Ucdv38CoEJwAAEusp6EAAAD") // BAD: 1 letter too short
            .expectAbsentString("TIME.EPOCH:epoch")
            .expectAbsentString("IP:ip")
            .expectAbsentString("PROCESSID:processid")
            .expectAbsentString("COUNTER:counter")
            .expectAbsentString("THREAD_INDEX:threadindex")
            .checkExpectations();
    }

    @Test
    void testBadUniqueIdNotBase64() {
        DissectorTester.create()
            .withDissector(new ModUniqueIdDissector())
            .withInput("Ucdv38CoEJwAAEusp6EAAAD!") // BAD: 1 letter wrong
            .expectAbsentString("TIME.EPOCH:epoch")
            .expectAbsentString("IP:ip")
            .expectAbsentString("PROCESSID:processid")
            .expectAbsentString("COUNTER:counter")
            .expectAbsentString("THREAD_INDEX:threadindex")
            .checkExpectations();
    }

}
