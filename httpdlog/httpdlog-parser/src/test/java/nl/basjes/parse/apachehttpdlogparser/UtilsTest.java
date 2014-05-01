/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.basjes.parse.apachehttpdlogparser;

import nl.basjes.parse.Utils;
import static org.junit.Assert.*;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testUrlDecoder(){
        // Normal cases
        assertEquals("  ", Utils.resilientUrlDecode("  "));
        assertEquals("  ", Utils.resilientUrlDecode(" %20"));
        assertEquals("  ", Utils.resilientUrlDecode("%20 "));
        assertEquals("  ", Utils.resilientUrlDecode("%20%20"));
        assertEquals("  ", Utils.resilientUrlDecode("%u2020"));

        // Deformed characters (desired is they are discarded)
        assertEquals(" ", Utils.resilientUrlDecode(" %2"));
        assertEquals(" ", Utils.resilientUrlDecode("%20%2"));
        assertEquals("", Utils.resilientUrlDecode("%u202"));
        assertEquals("", Utils.resilientUrlDecode("%u20"));
        assertEquals("", Utils.resilientUrlDecode("%u2"));
        assertEquals("", Utils.resilientUrlDecode("%u"));
        assertEquals("", Utils.resilientUrlDecode("%"));
    }
}
