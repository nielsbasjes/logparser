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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;

public class RequestCookieListDisector implements Disector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.COOKIES";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    /** This should output all possible types */
    @Override
    public String[] getPossibleOutput() {
        String[] result = new String[1];
        result[0] = "HTTP.COOKIE:*";
        return result;
    }

    // --------------------------------------------

    @Override
    public void prepareForDisect(final String inputname, final String outputname) {
        // We do not do anything extra here
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

    // Cache the compiled pattern
    private final Pattern fieldSeparatorPattern = Pattern.compile("; ");

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        String[] allValues = fieldSeparatorPattern.split(fieldValue);
        for (String value : allValues) {
            int equalPos = value.indexOf('=');
            if (equalPos == -1) {
                if (!"".equals(value)) {
                    String theName = value.trim().toLowerCase(); // Just a name, no value
                    parsable.addDisection(inputname, getDisectionType(inputname, theName), theName, "");
                }
            } else {
                try {
                    String theName = value.substring(0, equalPos).trim().toLowerCase();
                    String theValue = value.substring(equalPos + 1, value.length()).trim();
                    parsable.addDisection(inputname, getDisectionType(inputname, theName), theName, URLDecoder.decode(theValue, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    return;
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
        return "HTTP.COOKIE";
    }

    // --------------------------------------------

}
