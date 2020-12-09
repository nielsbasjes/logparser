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

package nl.basjes.parse.httpdlog;

import org.junit.Test;

import static nl.basjes.parse.httpdlog.Utils.makeHTMLEncodedInert;
import static nl.basjes.parse.httpdlog.Utils.resilientUrlDecode;
import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testUrlDecoder() {
        // Normal cases
        assertEquals("  ", resilientUrlDecode("  "));
        assertEquals("  ", resilientUrlDecode(" %20"));
        assertEquals("  ", resilientUrlDecode("%20 "));
        assertEquals("  ", resilientUrlDecode("%20%20"));
        assertEquals("  ", resilientUrlDecode("%u0020%u0020"));
        assertEquals("  ", resilientUrlDecode("%20%u0020"));
        assertEquals("  ", resilientUrlDecode("%u0020%20"));

        // Deformed characters at the end of the line (desired is they are discarded)
        assertEquals("x ", resilientUrlDecode("x %2"));
        assertEquals("x ", resilientUrlDecode("x%20%2"));
        assertEquals("x 2", resilientUrlDecode("x%u202"));
        assertEquals("x ", resilientUrlDecode("x%u20"));
        assertEquals("x", resilientUrlDecode("x%u2"));
        assertEquals("x", resilientUrlDecode("x%u"));
        assertEquals("x", resilientUrlDecode("x%"));

        // Combined test case (7 spaces and a chopped one)
        assertEquals("       ", resilientUrlDecode("%20 %20%u0020%20 %20%2"));
    }

    @Test
    public void testHtmlEncoding() {
        assertEquals("<", resilientUrlDecode(makeHTMLEncodedInert("&lt;")));
        assertEquals(">", resilientUrlDecode(makeHTMLEncodedInert("&gt;")));
        assertEquals("€", resilientUrlDecode(makeHTMLEncodedInert("&euro;")));

        assertEquals("*#x12345;", makeHTMLEncodedInert("&#x12345;"));
        assertEquals("*#xaBcDeF;", makeHTMLEncodedInert("&#xaBcDeF;"));
    }

    @Test
    public void testHtmlEncodingFull() {
        // Normal cases
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert("  ")));
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert(" %20")));
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert("%20 ")));
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert("%20%20")));
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert("%u0020%u0020")));
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert("%20%u0020")));
        assertEquals("  ", resilientUrlDecode(makeHTMLEncodedInert("%u0020%20")));

        // Deformed characters at the end of the line (desired is they are discarded)
        assertEquals("x ", resilientUrlDecode(makeHTMLEncodedInert("x %2")));
        assertEquals("x ", resilientUrlDecode(makeHTMLEncodedInert("x%20%2")));
        assertEquals("x 2", resilientUrlDecode(makeHTMLEncodedInert("x%u202")));
        assertEquals("x ", resilientUrlDecode(makeHTMLEncodedInert("x%u20")));
        assertEquals("x", resilientUrlDecode(makeHTMLEncodedInert("x%u2")));
        assertEquals("x", resilientUrlDecode(makeHTMLEncodedInert("x%u")));
        assertEquals("x", resilientUrlDecode(makeHTMLEncodedInert("x%")));

        // Combined test case (7 spaces and a chopped one)
        assertEquals("       ", resilientUrlDecode(makeHTMLEncodedInert("%20 %20%u0020%20 %20%2")));


        // Normal cases
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert(" &gt; ")));
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert(" &gt;%20")));
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert("%20&gt; ")));
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert("%20&gt;%20")));
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert("%u0020&gt;%u0020")));
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert("%20&gt;%u0020")));
        assertEquals(" > ", resilientUrlDecode(makeHTMLEncodedInert("%u0020&gt;%20")));

        // Deformed characters at the end of the line (desired is they are discarded)
        assertEquals(">x ", resilientUrlDecode(makeHTMLEncodedInert("&gt;x %2")));
        assertEquals(">x ", resilientUrlDecode(makeHTMLEncodedInert("&gt;x%20%2")));
        assertEquals(">x 2",  resilientUrlDecode(makeHTMLEncodedInert("&gt;x%u202")));
        assertEquals(">x ",  resilientUrlDecode(makeHTMLEncodedInert("&gt;x%u20")));
        assertEquals(">x",  resilientUrlDecode(makeHTMLEncodedInert("&gt;x%u2")));
        assertEquals(">x",  resilientUrlDecode(makeHTMLEncodedInert("&gt;x%u")));
        assertEquals(">x",  resilientUrlDecode(makeHTMLEncodedInert("&gt;x%")));


        // Normal cases
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert(" &foobar; ")));
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert(" &foobar;%20")));
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert("%20&foobar; ")));
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert("%20&foobar;%20")));
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert("%u0020&foobar;%u0020")));
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert("%20&foobar;%u0020")));
        assertEquals(" *foobar; ", resilientUrlDecode(makeHTMLEncodedInert("%u0020&foobar;%20")));

        // Deformed characters at the end of the line (desired is they are discarded)
        assertEquals("*foobar;x ", resilientUrlDecode(makeHTMLEncodedInert("&foobar;x %2")));
        assertEquals("*foobar;x ", resilientUrlDecode(makeHTMLEncodedInert("&foobar;x%20%2")));
        assertEquals("*foobar;x 2",  resilientUrlDecode(makeHTMLEncodedInert("&foobar;x%u202")));
        assertEquals("*foobar;x ",  resilientUrlDecode(makeHTMLEncodedInert("&foobar;x%u20")));
        assertEquals("*foobar;x",  resilientUrlDecode(makeHTMLEncodedInert("&foobar;x%u2")));
        assertEquals("*foobar;x",  resilientUrlDecode(makeHTMLEncodedInert("&foobar;x%u")));
        assertEquals("*foobar;x",  resilientUrlDecode(makeHTMLEncodedInert("&foobar;x%")));

        assertEquals("€",  resilientUrlDecode(makeHTMLEncodedInert("&euro;")));
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
        assertEquals((byte) 0xAA, Utils.hexCharsToByte('A', 'A'));
        assertEquals((byte) 0xBB, Utils.hexCharsToByte('B', 'B'));
        assertEquals((byte) 0xCC, Utils.hexCharsToByte('C', 'C'));
        assertEquals((byte) 0xDD, Utils.hexCharsToByte('D', 'D'));
        assertEquals((byte) 0xEE, Utils.hexCharsToByte('E', 'E'));
        assertEquals((byte) 0xFF, Utils.hexCharsToByte('F', 'F'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexToByteIllegalLeft() {
        Utils.hexCharsToByte('X', '0');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexToByteIllegalRight() {
        Utils.hexCharsToByte('0', 'X');
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
