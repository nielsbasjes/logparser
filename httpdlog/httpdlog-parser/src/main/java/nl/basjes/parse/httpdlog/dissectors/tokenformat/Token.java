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
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import nl.basjes.parse.core.Casts;

import java.util.EnumSet;

public class Token {
//    private static Map<String, String> typeOverrule = new HashMap<String, String>();

    private final String name;
    private final String type;
    private final String regex;
    private final int startPos;
    private final int length;
    private final EnumSet<Casts> casts;
    private final int prio;

    public Token(
            final String nName,
            final String nType,
            final EnumSet<Casts> nCasts,
            final String nRegex,
            final int nStartPos,
            final int nLength) {
        this(nName, nType, nCasts, nRegex, nStartPos, nLength,  0);
    }

    public Token(
            final String nName,
            final String nType,
            final EnumSet<Casts> nCasts,
            final String nRegex,
            final int nStartPos,
            final int nLength,
            final int nPrio) {

        // RFC 2616 Section 4.2 states: "Field names are case-insensitive."
        name = nName.toLowerCase();
        type = nType;
//        if (typeOverrule.containsKey(name.toLowerCase())) {
//            type = typeOverrule.get(name.toLowerCase());
//        }
        regex = nRegex;
        startPos = nStartPos;
        length = nLength;
        casts = nCasts;
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

    public EnumSet<Casts> getCasts() {
        return casts;
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
