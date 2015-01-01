/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

package nl.basjes.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public final class Utils {

    private Utils() {}

    private static final Pattern VALID_STANDARD         = Pattern.compile("%([0-9A-Fa-f]{2})");
    private static final Pattern CHOPPED_STANDARD       = Pattern.compile("%[0-9A-Fa-f]?$");
    private static final Pattern VALID_NON_STANDARD     = Pattern.compile("%u([0-9A-Fa-f][0-9A-Fa-f])([0-9A-Fa-f][0-9A-Fa-f])");
    private static final Pattern CHOPPED_NON_STANDARD   = Pattern.compile("%u[0-9A-Fa-f]{0,3}$");

    /**
     * The main goal of the resilientUrlDecode is to have a UrlDecode that keeps working
     * even if the input is seriously flawed or even uses a rejected standard.
     * @param input the UrlEncoded input string
     * @return Url decoded result string
     */
    public static String resilientUrlDecode(String input) {
        String cookedInput = input;

        if (cookedInput.indexOf('%') > -1) {
            // Transform all existing UTF-8 standard into UTF-16 standard.
            cookedInput = VALID_STANDARD.matcher(cookedInput).replaceAll("%00%$1");

            // Discard chopped encoded char at the end of the line (there is no way to know what it was)
            cookedInput = CHOPPED_STANDARD.matcher(cookedInput).replaceAll("");

            // Handle non standard (rejected by W3C) encoding that is used anyway by some
            // See: http://stackoverflow.com/a/5408655/114196
            if (cookedInput.contains("%u")) {
                // Transform all existing non standard into UTF-16 standard.
                cookedInput = VALID_NON_STANDARD.matcher(cookedInput).replaceAll("%$1%$2");

                // Discard chopped encoded char at the end of the line
                cookedInput = CHOPPED_NON_STANDARD.matcher(cookedInput).replaceAll("");
            }
        }

        try {
            return URLDecoder.decode(cookedInput, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            // Will never happen because the encoding is hardcoded
            return null;
        }
    }

}
