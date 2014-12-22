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

package nl.basjes.parse.dissectors.http;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.util.*;

import static nl.basjes.parse.Utils.resilientUrlDecode;

public class QueryStringFieldDissector extends Dissector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.QUERYSTRING";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    /** This should output all possible types */
    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("STRING:*");
        return result;
    }

    // --------------------------------------------

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        return true; // Everything went right.
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        // Nothing to do
    }

    // --------------------------------------------

    private final Set<String> requestedParameters = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        requestedParameters.add(outputname.substring(inputname.length() + 1));
        return Casts.STRING_ONLY;
    }

    // --------------------------------------------

    private boolean wantAllFields = false;

    @Override
    public void prepareForRun() {
        wantAllFields = requestedParameters.contains("*");
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        String[] allValues = fieldValue.split("&");

        for (String value : allValues) {
            int equalPos = value.indexOf('=');
            if (equalPos == -1) {
                if (!"".equals(value)) {
                    String name = value.toLowerCase();
                    if (wantAllFields || requestedParameters.contains(name)) {
                        parsable.addDissection(inputname, getDissectionType(inputname, value), name, "");
                    }
                }
            } else {
                String name = value.substring(0, equalPos).toLowerCase();
                if (wantAllFields || requestedParameters.contains(name)) {
                    try {
                        parsable.addDissection(inputname, getDissectionType(inputname, name), name,
                                resilientUrlDecode(value.substring(equalPos + 1, value.length())));
                    } catch (IllegalArgumentException e) {
                        // This usually means that there was invalid encoding in the line
                        throw new DissectionFailure(e.getMessage());
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
    public String getDissectionType(final String basename, final String name) {
        return "STRING";
    }

    // --------------------------------------------

}
