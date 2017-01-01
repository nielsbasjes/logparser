/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void testHexToByte() {
        // Test basic character decoder
        assertEquals((byte) 0x00, Utils.hexCharsToByte('0', '0'));
        assertEquals((byte) 0x11, Utils.hexCharsToByte('1', '1'));
        assertEquals((byte) 0x22, Utils.hexCharsToByte('2', '2'));
        assertEquals((byte) 0x33, Utils.hexCharsToByte('3', '3'));
        assertEquals((byte) 0x44, Utils.hexCharsToByte('4', '4'));
        assertEquals((byte) 0x55, Utils.hexCharsToByte('5', '5'));
        assertEquals((byte) 0x66, Utils.hexCharsToByte('6', '6'));
        assertEquals((byte) 0x77, Utils.hexCharsToByte('7', '7'));
        assertEquals((byte) 0x88, Utils.hexCharsToByte('8', '8'));
        assertEquals((byte) 0x99, Utils.hexCharsToByte('9', '9'));
        assertEquals((byte) 0xaa, Utils.hexCharsToByte('a', 'a'));
        assertEquals((byte) 0xbb, Utils.hexCharsToByte('b', 'b'));
        assertEquals((byte) 0xcc, Utils.hexCharsToByte('c', 'c'));
        assertEquals((byte) 0xdd, Utils.hexCharsToByte('d', 'd'));
        assertEquals((byte) 0xee, Utils.hexCharsToByte('e', 'e'));
        assertEquals((byte) 0xff, Utils.hexCharsToByte('f', 'f'));
    }

    @Test
    public void testApacheLogDecoder() {
        // Decoding a value
        assertEquals("bla bla bla", Utils.decodeApacheHTTPDLogValue("bla bla bla"));
        assertEquals("bla bla bla", Utils.decodeApacheHTTPDLogValue("bla\\x20bla bla"));
        assertEquals("bla\bbla\nbla\tbla", Utils.decodeApacheHTTPDLogValue("bla\\bbla\\nbla\\tbla"));
        assertEquals("bla\"bla\nbla\tbla", Utils.decodeApacheHTTPDLogValue("bla\\\"bla\\nbla\\tbla"));
        assertEquals(new String(new byte[] {(byte)0x0b}), Utils.decodeApacheHTTPDLogValue("\\v"));

        // Specials
        assertEquals("\\q", Utils.decodeApacheHTTPDLogValue("\\q"));
        assertEquals("", Utils.decodeApacheHTTPDLogValue(""));
        assertEquals(null, Utils.decodeApacheHTTPDLogValue(null));
    }

}
