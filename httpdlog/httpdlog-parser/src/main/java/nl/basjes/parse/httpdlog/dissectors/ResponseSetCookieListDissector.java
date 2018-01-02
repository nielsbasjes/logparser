/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
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

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        requestedCookies.add(extractFieldName(inputname, outputname));
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

        final String fieldValue = field.getValue().getString();
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
                    parsable.addDissection(inputname, "HTTP.SETCOOKIE", cookieName, value);
                }
            }
        }

    }

    // --------------------------------------------

}
