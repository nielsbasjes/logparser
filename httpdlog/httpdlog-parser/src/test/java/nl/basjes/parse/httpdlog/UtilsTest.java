/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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

package nl.basjes.parse.httpdlog;

import static org.junit.Assert.*;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testUrlDecoder() {
        // Normal cases
        assertEquals("  ", Utils.resilientUrlDecode("  "));
        assertEquals("  ", Utils.resilientUrlDecode(" %20"));
        assertEquals("  ", Utils.resilientUrlDecode("%20 "));
        assertEquals("  ", Utils.resilientUrlDecode("%20%20"));
        assertEquals("  ", Utils.resilientUrlDecode("%u0020%u0020"));
        assertEquals("  ", Utils.resilientUrlDecode("%20%u0020"));
        assertEquals("  ", Utils.resilientUrlDecode("%u0020%20"));

        // Deformed characters at the end of the line (desired is they are discarded)
        assertEquals("x ", Utils.resilientUrlDecode("x %2"));
        assertEquals("x ", Utils.resilientUrlDecode("x%20%2"));
        assertEquals("x", Utils.resilientUrlDecode("x%u202"));
        assertEquals("x", Utils.resilientUrlDecode("x%u20"));
        assertEquals("x", Utils.resilientUrlDecode("x%u2"));
        assertEquals("x", Utils.resilientUrlDecode("x%u"));
        assertEquals("x", Utils.resilientUrlDecode("x%"));

        // Combined test case (7 spaces and a chopped one)
        assertEquals("       ", Utils.resilientUrlDecode("%20 %20%u0020%20 %20%2"));
    }

    @Test
    public void testApacheLogDecoder() {
        // Test basic character decoder
        assertEquals((byte)0x00, Utils.hexCharsToByte('0', '0'));
        assertEquals((byte)0x20, Utils.hexCharsToByte('2', '0'));
        assertEquals((byte)0x43, Utils.hexCharsToByte('4', '3'));
        assertEquals((byte)0x88, Utils.hexCharsToByte('8', '8'));
        assertEquals((byte)0xde, Utils.hexCharsToByte('d', 'E'));
        assertEquals((byte)0xff, Utils.hexCharsToByte('F', 'f'));

        // Decoding a value
        assertEquals("bla bla bla", Utils.decodeApacheHTTPDLogValue("bla bla bla"));
        assertEquals("bla bla bla", Utils.decodeApacheHTTPDLogValue("bla\\x20bla bla"));
        assertEquals("bla\bbla\nbla\tbla", Utils.decodeApacheHTTPDLogValue("bla\\bbla\\nbla\\tbla"));
        assertEquals("bla\"bla\nbla\tbla", Utils.decodeApacheHTTPDLogValue("bla\\\"bla\\nbla\\tbla"));
    }
}
