/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;

public class ScreenResolutionDissector extends Dissector {

    public static final String SCREENRESOLUTION = "SCREENRESOLUTION";
    private String separator = "x";
    private boolean wantWidth = false;
    private boolean wantHeight = false;

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        if (settings.length() > 0) {
            this.separator = settings;
        }
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(SCREENRESOLUTION, inputname);

        final String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        if (fieldValue.contains(separator)) {
            String[] parts = fieldValue.split(separator);
            if (wantWidth) {
                parsable.addDissection(inputname, "SCREENWIDTH", "width", parts[0]);
            }
            if (wantHeight) {
                parsable.addDissection(inputname, "SCREENHEIGHT", "height", parts[1]);
            }
        }
    }

    @Override
    public String getInputType() {
        return SCREENRESOLUTION;
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("SCREENWIDTH:width");
        result.add("SCREENHEIGHT:height");
        return result;
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        String name = extractFieldName(inputname, outputname);
        if ("width".equals(name)) {
            wantWidth = true;
            return STRING_OR_LONG;
        }
        if ("height".equals(name)) {
            wantHeight = true;
            return STRING_OR_LONG;
        }
        return NO_CASTS;
    }
}
