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

package nl.basjes.parse.httpdlog.dissectors;

import java.net.HttpCookie;
import java.util.*;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

public class ResponseSetCookieListDissector extends Dissector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.SETCOOKIES";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    /** This should output all possible types */
    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("HTTP.SETCOOKIE:*");
        return result;
    }

    // --------------------------------------------

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        return true; // Everything went right.
    }

    // --------------------------------------------

    protected void initializeNewInstance(Dissector newInstance) {
        // Nothing to do
    }

    // --------------------------------------------
    private final Set<String> requestedCookies = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
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

    private final int minimalExpiresLength = "expires=XXXXXXX".length();

    // Cache the compiled pattern
    private static final String SPLIT_BY = ", ";

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        // This input is a ', ' separated list.
        // But the expires field can contain a ','
        // and HttpCookie.parse(...) doesn't always work :(
        String[] parts = fieldValue.split(SPLIT_BY);

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
                value = previous+ SPLIT_BY +part;
                previous="";
            }

            List<HttpCookie> cookies = HttpCookie.parse(value);

            for (HttpCookie cookie : cookies) {
                cookie.setVersion(1);
                String cookieName = cookie.getName().toLowerCase();
                if (wantAllCookies || requestedCookies.contains(cookieName)) {
                    parsable.addDissection(inputname,
                            getDissectionType(inputname, cookieName),
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
    public String getDissectionType(final String basename, final String name) {
        return "HTTP.SETCOOKIE";
    }

    // --------------------------------------------

}
