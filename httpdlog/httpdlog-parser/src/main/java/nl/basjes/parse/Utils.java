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


    public static byte hexCharsToByte(char c1, char c2){
        byte result;
        switch (c1) {
            case '0': result = (byte)0x00; break;
            case '1': result = (byte)0x10; break;
            case '2': result = (byte)0x20; break;
            case '3': result = (byte)0x30; break;
            case '4': result = (byte)0x40; break;
            case '5': result = (byte)0x50; break;
            case '6': result = (byte)0x60; break;
            case '7': result = (byte)0x70; break;
            case '8': result = (byte)0x80; break;
            case '9': result = (byte)0x90; break;
            case 'a': result = (byte)0xa0; break;
            case 'b': result = (byte)0xb0; break;
            case 'c': result = (byte)0xc0; break;
            case 'd': result = (byte)0xd0; break;
            case 'e': result = (byte)0xe0; break;
            case 'f': result = (byte)0xf0; break;
            case 'A': result = (byte)0xa0; break;
            case 'B': result = (byte)0xb0; break;
            case 'C': result = (byte)0xc0; break;
            case 'D': result = (byte)0xd0; break;
            case 'E': result = (byte)0xe0; break;
            case 'F': result = (byte)0xf0; break;
            default: throw new IllegalArgumentException("URLDecoder: Illegal hex characters (char 1): '" + c1 + "'");
        }
        switch (c2) {
            case '0': result |= (byte)0x00; break;
            case '1': result |= (byte)0x01; break;
            case '2': result |= (byte)0x02; break;
            case '3': result |= (byte)0x03; break;
            case '4': result |= (byte)0x04; break;
            case '5': result |= (byte)0x05; break;
            case '6': result |= (byte)0x06; break;
            case '7': result |= (byte)0x07; break;
            case '8': result |= (byte)0x08; break;
            case '9': result |= (byte)0x09; break;
            case 'a': result |= (byte)0x0a; break;
            case 'b': result |= (byte)0x0b; break;
            case 'c': result |= (byte)0x0c; break;
            case 'd': result |= (byte)0x0d; break;
            case 'e': result |= (byte)0x0e; break;
            case 'f': result |= (byte)0x0f; break;
            case 'A': result |= (byte)0x0a; break;
            case 'B': result |= (byte)0x0b; break;
            case 'C': result |= (byte)0x0c; break;
            case 'D': result |= (byte)0x0d; break;
            case 'E': result |= (byte)0x0e; break;
            case 'F': result |= (byte)0x0f; break;
            default: throw new IllegalArgumentException("URLDecoder: Illegal hex characters (char 2): '" + c2 + "'");
        }

        return result;
    }

    /**
     * Decoder for this specification:
     *
     * http://httpd.apache.org/docs/current/mod/mod_log_config.html#formats
     * Format Notes
     * For security reasons, starting with version 2.0.46, non-printable and other special characters
     * in %r, %i and %o are escaped using \xhh sequences, where hh stands for the hexadecimal representation of
     * the raw byte. Exceptions from this rule are " and \, which are escaped by prepending a backslash, and
     * all whitespace characters, which are written in their C-style notation (\n, \t, etc).
     *
     * Actual method in the original code:  ap_escape_logitem
     * https://github.com/apache/httpd/blob/trunk/server/util.c#L2001
     *
     * @param input The value as it was logged by the Apache HTTPD.
     * @return      The 'decoded' version of the logged value.
     */
    public static String decodeApacheHTTPDLogValue(String input){
        if (input == null || input.length() == 0) {
            return input;
        }

        if (!input.contains("\\")) {
            return input;
        }

        StringBuilder sb = new StringBuilder(input.length());

        // http://stackoverflow.com/q/8894258/114196 : Fastest way to iterate over all the chars in a String
        for (int i = 0; i < input.length(); i++) {
            char chr = input.charAt(i);

            if ( chr == '\\' ) {
                chr = input.charAt(++i);
                switch (chr){
                    case '"':
                    case '\\':
                        sb.append(chr);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'v':
                        sb.append(0x0b);
                        break;
                    case 'x':
                        // This should be \xhh  (hh = [0-9a-f][0-9a-f])
                        char chr1 = input.charAt(++i);
                        char chr2 = input.charAt(++i);
                        sb.append((char)hexCharsToByte(chr1, chr2));
                        break;
                    default:
                        // This shouldn't happen.
                        // Let's just append the unmodified input for now.
                        // FIXME: Figure out what to do (should not occur??)
                        sb.append('\\').append(chr);
                }
            } else {
                sb.append(chr);
            }

        }
        return sb.toString();
    }

}
