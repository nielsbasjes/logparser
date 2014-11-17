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

import java.util.*;
import java.util.regex.Pattern;

import nl.basjes.parse.Utils;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DisectionFailure;

public class RequestCookieListDisector extends Disector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.COOKIES";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    /** This should output all possible types */
    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("HTTP.COOKIE:*");
        return result;
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Disector newInstance) {
        // Nothing to do
    }

    // --------------------------------------------

    private final Set<String> requestedCookies = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDisect(final String inputname, final String outputname) {
        requestedCookies.add(outputname.substring(inputname.length() + 1));
        return Casts.STRING_ONLY;
    }

    // --------------------------------------------
    private boolean wantAllCookies = false;

    @Override
    public void prepareForRun() {
        wantAllCookies = requestedCookies.contains("*");
    }

    // --------------------------------------------

    // Cache the compiled pattern
    private final Pattern fieldSeparatorPattern = Pattern.compile("; ");

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) throws DisectionFailure {
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
                    if (wantAllCookies || requestedCookies.contains(theName)) {
                        parsable.addDisection(inputname, getDisectionType(inputname, theName), theName, "");
                    }
                }
            } else {
                String theName = value.substring(0, equalPos).trim().toLowerCase();
                if (wantAllCookies || requestedCookies.contains(theName)) {
                    String theValue = value.substring(equalPos + 1, value.length()).trim();
                    try {
                        parsable.addDisection(inputname, getDisectionType(inputname, theName), theName,
                                Utils.resilientUrlDecode(theValue));
                    } catch (IllegalArgumentException e) {
                        // This usually means that there was invalid encoding in the line
                        throw new DisectionFailure(e.getMessage());
                    }
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
