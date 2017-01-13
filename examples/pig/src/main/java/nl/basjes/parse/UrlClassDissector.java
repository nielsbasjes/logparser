/*
 * Apache HTTPD & NGINX Access log parsing made easy
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
package nl.basjes.parse;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class UrlClassDissector extends Dissector {

    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.PATH";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("HTTP.PATH.CLASS:class");
        return result;
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        return true; // Everything went right.
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        // Nothing to do
    }

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        return Casts.STRING_ONLY;
    }

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        // NOTE; This is just a silly example to illustrate what can be done.
        String result;
        do {
            if (fieldValue.startsWith("/1-500e-KWh")) {
                result = "PowerTick";
                break;
            }
            if (fieldValue.endsWith(".html")) {
                result = "Page";
                break;
            }
            if (fieldValue.endsWith(".gif")) {
                result = "Image";
                break;
            }
            if (fieldValue.endsWith(".css")) {
                result = "StyleSheet";
                break;
            }
            if (fieldValue.endsWith(".js")) {
                result = "Script";
                break;
            }
            if (fieldValue.endsWith("_form")) {
                result = "HackAttempt";
                break;
            }

            result = "Other";
        } while (false); // Yeah ...I know ...

        parsable.addDissection(inputname, "HTTP.PATH.CLASS", "class", result);
    }
    // --------------------------------------------

}
