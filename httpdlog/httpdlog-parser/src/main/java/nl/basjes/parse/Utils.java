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

public class Utils {

    public static String resilientUrlDecode(String input) {
        String cookedInput = input;


        cookedInput = cookedInput.replaceAll("%[0-9A-Fa-f]$","");
        cookedInput = cookedInput.replaceAll("%$", "");

        if (cookedInput.contains("%u")) {
            cookedInput = cookedInput.replaceAll("%u([0-9A-Fa-f][0-9A-Fa-f])([0-9A-Fa-f][0-9A-Fa-f])","%$1%$2");

            if (cookedInput.contains("%u")) {
                cookedInput = cookedInput.replaceAll("%u[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]$", "");
                if (cookedInput.contains("%u")) {
                    cookedInput = cookedInput.replaceAll("%u[0-9A-Fa-f][0-9A-Fa-f]$", "");
                }
                if (cookedInput.contains("%u")) {
                    cookedInput = cookedInput.replaceAll("%u[0-9A-Fa-f]$", "");
                }
                if (cookedInput.contains("%u")) {
                    cookedInput = cookedInput.replaceAll("%u$", "");
                }
            }
        }

        try {
            return URLDecoder.decode(cookedInput,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Should never happen
        }
        return null;
    }


}
