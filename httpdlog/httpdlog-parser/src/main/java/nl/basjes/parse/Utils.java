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

package nl.basjes.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class Utils {

    private static Pattern validStandard      = Pattern.compile("%([0-9A-Fa-f]{2})");
    private static Pattern choppedStandard    = Pattern.compile("%[0-9A-Fa-f]{0,1}$");
    private static Pattern validNonStandard   = Pattern.compile("%u([0-9A-Fa-f][0-9A-Fa-f])([0-9A-Fa-f][0-9A-Fa-f])");
    private static Pattern choppedNonStandard = Pattern.compile("%u[0-9A-Fa-f]{0,3}$");

    public static String resilientUrlDecode(String input) {
        String cookedInput = input;

        if (cookedInput.indexOf('%') > -1) {
            // Transform all existing UTF-8 standard into UTF-16 standard.
            cookedInput = validStandard.matcher(cookedInput).replaceAll("%00%$1");

            // Discard chopped encoded char at the end of the line (there is no way to know what it was)
            cookedInput = choppedStandard.matcher(cookedInput).replaceAll("");

            // Handle non standard (rejected by W3C) encoding that is used anyway by some
            // See: http://stackoverflow.com/a/5408655/114196
            if (cookedInput.contains("%u")) {
              // Transform all existing non standard into UTF-16 standard.
              cookedInput = validNonStandard.matcher(cookedInput).replaceAll("%$1%$2");

              // Discard chopped encoded char at the end of the line
              cookedInput = choppedNonStandard.matcher(cookedInput).replaceAll("");
            }
        }

        try {
            return URLDecoder.decode(cookedInput,"UTF-16");
        } catch (UnsupportedEncodingException e) {
            // Will never happen because the encoding is hardcoded
            return null;
        }
    }

}
