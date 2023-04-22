/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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
package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.httpdlog.Utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static nl.basjes.parse.core.Casts.STRING_ONLY;

public class RequestCookieListDissector extends Dissector {
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

    private final Set<String> requestedCookies = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        requestedCookies.add(extractFieldName(inputname, outputname));
        return STRING_ONLY;
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
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue().getString();
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
                        parsable.addDissection(inputname, "HTTP.COOKIE", theName, "");
                    }
                }
            } else {
                String theName = value.substring(0, equalPos).trim().toLowerCase();
                if (wantAllCookies || requestedCookies.contains(theName)) {
                    String theValue = value.substring(equalPos + 1).trim();
                    try {
                        parsable.addDissection(inputname, "HTTP.COOKIE", theName,
                                Utils.resilientUrlDecode(theValue));
                    } catch (IllegalArgumentException e) {
                        // This usually means that there was invalid encoding in the line
                        throw new DissectionFailure(e.getMessage());
                    }
                }
            }
        }
    }

    // --------------------------------------------

}
