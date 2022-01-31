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

package nl.basjes.parse.httpdlog.translate;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertCLFIntoNumber;
import nl.basjes.parse.httpdlog.dissectors.translate.ConvertNumberIntoCLF;
import org.junit.jupiter.api.Test;

class TestTranslators {

    @Test
    void testCLFToNumberMin() {
        DissectorTester.create()
            .withDissector("root", new ConvertCLFIntoNumber("IN", "OUT"))
            .withInput(null) // A '-' in the input file goes into the dissector as a null value
            .expect("OUT:root", 0L)
            .checkExpectations();
    }

    @Test
    void testCLFToNumber0() {
        DissectorTester.create()
            .withDissector("root", new ConvertCLFIntoNumber("IN", "OUT"))
            .withInput("0")
            .expect("OUT:root", 0L)
            .checkExpectations();
    }

    @Test
    void testCLFToNumber1() {
        DissectorTester.create()
            .withDissector("root", new ConvertCLFIntoNumber("IN", "OUT"))
            .withInput("1")
            .expect("OUT:root", 1L)
            .checkExpectations();
    }

    @Test
    void testNumberToCLF0() {
        DissectorTester.create()
            .withDissector("root", new ConvertNumberIntoCLF("IN", "OUT"))
            .withInput("0")
            .expect("OUT:root", 0L)
            .expect("OUT:root", "0")
            .checkExpectations();
    }

    @Test
    void testNumberToCLF1() {
        DissectorTester.create()
            .withDissector("root", new ConvertNumberIntoCLF("IN", "OUT"))
            .withInput("1")
            .expect("OUT:root", 1L)
            .expect("OUT:root", "1")
            .checkExpectations();
    }


}
