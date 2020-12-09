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

import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Utils {

    private Utils() {}

    private static final Pattern CHOPPED_STANDARD       = Pattern.compile("%[0-9A-Fa-f]?$");
    private static final Pattern VALID_NON_STANDARD     = Pattern.compile("%u([0-9A-Fa-f][0-9A-Fa-f])+");
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
            // Discard chopped encoded char at the end of the line (there is no way to know what it was)
            cookedInput = CHOPPED_STANDARD.matcher(cookedInput).replaceAll("");

            // Handle non standard (rejected by W3C) encoding that is used anyway by some
            // See: https://stackoverflow.com/a/5408655/114196
            if (cookedInput.contains("%u")) {
                cookedInput = replaceString(cookedInput, "%u00", "%u");

                // Transform all existing non standard into UTF-8 standard.
                cookedInput = VALID_NON_STANDARD.matcher(cookedInput).replaceAll("%$1");

                // Discard chopped encoded char at the end of the line
                cookedInput = CHOPPED_NON_STANDARD.matcher(cookedInput).replaceAll("");
            }
        }

        try {
            return URLDecoder.decode(cookedInput, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Will never happen because the encoding is hardcoded
            return null;
        }
    }


    public static byte hexCharsToByte(String twoHexDigits) {
        if (twoHexDigits == null || twoHexDigits.length() != 2) {
            throw new IllegalArgumentException("URLDecoder: Illegal hex characters : \"" + twoHexDigits + "\"");
        }
        return hexCharsToByte(twoHexDigits.charAt(0), twoHexDigits.charAt(1));
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
            case '0': break; // result |= (byte)0x00; --> An OR with only 0 bits is useless
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

        // https://stackoverflow.com/q/8894258/114196 : Fastest way to iterate over all the chars in a String
        for (int i = 0; i < input.length(); i++) {
            char chr = input.charAt(i);

            if (chr == '\\') {
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
                        sb.append((char)hexCharsToByte('0', 'b'));
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
                        sb.append('\\').append(chr);
                }
            } else {
                sb.append(chr);
            }

        }
        return sb.toString();
    }

    private static final Map<String, String> HTML_ENTITY_REPLACE_MAP;

    private static String htmlEntityToURLEncoded(String entity) {
        try {
            return URLEncoder.encode(StringEscapeUtils.unescapeHtml4(entity), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // This will never happen
        }
        return null;
    }

    static {
        // The list of common entities I chose to support on their name.
        List<String> entities = Arrays.asList(
            "&nbsp;",
            "&lt;",
            "&gt;",
            "&amp;",
            "&quot;",
            "&tilde;",
            "&cent;",
            "&pound;",
            "&yen;",
            "&euro;",
            "&copy;",
            "&reg;"
        );

        Map<String, String> aMap = new HashMap<>();
        entities.forEach(entity -> aMap.put(entity, htmlEntityToURLEncoded(entity)));
        HTML_ENTITY_REPLACE_MAP = Collections.unmodifiableMap(aMap);
    }

    /**
     * Sometimes people put HTML encoded chars into a URL.
     * Because it is very hard to correctly decode these we are just making them 'inert'
     * so they do not break the rest of the processing.
     * @param uriString The input URI
     * @return Cleaned string
     */
    public static String makeHTMLEncodedInert(String uriString) {
        String result = uriString;

        // For some entities we have a valid replace value.
        for (Map.Entry<String, String> entry: HTML_ENTITY_REPLACE_MAP.entrySet()) {
            result = replaceString(result, entry.getKey(), entry.getValue());
        }

        // For the rest we simply make it inert
        // TODO: Convert the codepoint to valid UTF-8
        // Named entities ( like &gt; and &euro; )
        result = result.replaceAll("&([a-zA-Z]+;)", "*$1");

        // Numerical decimal entities
        result = result.replaceAll("&(#[0-9a-fA-F]+;)", "*$1");

        // Numerical hexadecimal entities
        result = result.replaceAll("&(#x[0-9a-fA-F]+;)", "*$1");

        return result;
    }

    // Copied from Yauaa
    public static String replaceString(
        final String input,
        final String searchFor,
        final String replaceWith
    ){
        //startIdx and idxSearchFor delimit various chunks of input; these
        //chunks always end where searchFor begins
        int startIdx = 0;
        int idxSearchFor = input.indexOf(searchFor, startIdx);
        if (idxSearchFor < 0) {
            return input;
        }
        final StringBuilder result = new StringBuilder(input.length()+32);

        while (idxSearchFor >= 0) {
            //grab a part of input which does not include searchFor
            result.append(input, startIdx, idxSearchFor);
            //add replaceWith to take place of searchFor
            result.append(replaceWith);

            //reset the startIdx to just after the current match, to see
            //if there are any further matches
            startIdx = idxSearchFor + searchFor.length();
            idxSearchFor = input.indexOf(searchFor, startIdx);
        }
        //the final chunk will go to the end of input
        result.append(input.substring(startIdx));
        return result.toString();
    }


}
