/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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
package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.basjes.parse.httpdlog.Utils.resilientUrlDecode;

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
        requestedParameters.add(extractFieldName(inputname, outputname));
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

        String fieldValue = field.getValue().getString();
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
    @SuppressWarnings("UnusedParameters")
    public String getDissectionType(final String basename, final String name) {
        return "STRING";
    }

    // --------------------------------------------

}
