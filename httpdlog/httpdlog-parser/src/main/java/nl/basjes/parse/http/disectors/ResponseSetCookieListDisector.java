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

package nl.basjes.parse.http.disectors;

import java.net.HttpCookie;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;

public class ResponseSetCookieListDisector extends Disector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.SETCOOKIES";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    /** This should output all possible types */
    @Override
    public String[] getPossibleOutput() {
        String[] result = new String[1];
        result[0] = "HTTP.SETCOOKIE:*";
        return result;
    }

    // --------------------------------------------

    private Set<String> requestedCookies = new HashSet<String>(16);

    @Override
    public void prepareForDisect(final String inputname, final String outputname) {
        requestedCookies.add(outputname.substring(inputname.length() + 1));
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

    private final int minimalExpiresLength = "expires=XXXXXXX".length();

    // Cache the compiled pattern
    private final String splitBy = ", ";
    private final Pattern fieldSeparatorPattern = Pattern.compile(splitBy);

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        // This input is a ', ' separated list.
        // But the expires field can contain a ','
        // and HttpCookie.parse(...) doesn't always work :(
        String[] parts = fieldSeparatorPattern.split(fieldValue);

        String previous=""; 
        for (String part:parts) {
            int expiresIndex = part.toLowerCase().indexOf("expires=");
            if (expiresIndex != -1) {
                if (part.length() - minimalExpiresLength < expiresIndex) {
                    previous = part;
                    continue;
                }
            }
            String value = part;
            if (!previous.isEmpty()) {
                value = previous+splitBy+part;
                previous="";
            }

            List<HttpCookie> cookies = HttpCookie.parse(value);
            
            for (HttpCookie cookie : cookies) {
                cookie.setVersion(1);
                String cookieName = cookie.getName().toLowerCase();
                if (requestedCookies.contains(cookieName)) {
                    parsable.addDisection(inputname,
                            getDisectionType(inputname, cookieName),
                            cookieName,
                            value);
                }
            }
        }
        
    }

    // --------------------------------------------

    /**
     * This determines the type of the value that was just found.
     * This method is intended to be overruled by a subclass
     */
    public String getDisectionType(final String basename, final String name) {
        return "HTTP.SETCOOKIE";
    }

    // --------------------------------------------

}
