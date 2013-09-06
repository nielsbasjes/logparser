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

package nl.basjes.parse.apachehttpdlog.logformat;

import java.util.HashMap;
import java.util.Map;



public class Token {
    private static Map<String, String> typeOverrule = new HashMap<String, String>();

    private String name;
    private String type;
    private String regex;
    private int startPos;
    private int length;
    private int prio;

    public Token(
            final String nName,
            final String nType,
            final String nRegex,
            final int nStartPos,
            final int nLength) {
        this(nName, nType, nRegex, nStartPos, nLength, 0);
    }

    public Token(
            final String nName,
            final String nType,
            final String nRegex,
            final int nStartPos,
            final int nLength,
            final int nPrio) {
        
        // RFC 2616 Section 4.2 states: "Field names are case-insensitive."
        name = nName.toLowerCase();
        type = nType;
        if (typeOverrule.containsKey(name.toLowerCase())) {
            type = typeOverrule.get(name.toLowerCase());
        }
        regex = nRegex;
        startPos = nStartPos;
        length = nLength;
        prio = nPrio;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRegex() {
        return regex;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getLength() {
        return length;
    }

    public int getPrio() {
        return prio;
    }
    
    // This is used by your favorite debugger.
    @Override
    public String toString() {
        return "{" + type + ':' + name + " (" + startPos + "+" + length + ");Prio=" + prio + "}";
    }
}
