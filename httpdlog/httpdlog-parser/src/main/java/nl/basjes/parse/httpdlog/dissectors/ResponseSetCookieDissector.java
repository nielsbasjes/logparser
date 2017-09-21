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
package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ResponseSetCookieDissector extends Dissector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.SETCOOKIE";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    /** This should output all possible types */
    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("STRING:value");
        result.add("STRING:expires");
        result.add("TIME.EPOCH:expires");
        result.add("STRING:path");
        result.add("STRING:domain");
        result.add("STRING:comment");
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


    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = extractFieldName(inputname, outputname);
        switch (name) {
            case "value":   return Casts.STRING_ONLY;
            case "expires": return Casts.STRING_OR_LONG;
            case "path":    return Casts.STRING_ONLY;
            case "domain":  return Casts.STRING_ONLY;
            case "comment": return Casts.STRING_ONLY;
            default:        return Casts.STRING_ONLY;
        }
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        String[] parts = fieldValue.split(";");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            String[] keyValue = part.split("=", 2);

            String key = keyValue[0].trim();
            String value = "";
            if (keyValue.length == 2) {
                value = keyValue[1].trim();
            }

            if (i==0) {
                parsable.addDissection(inputname, "STRING", "value", value);
            } else {
                switch (key) {
                    // We ignore the max-age field because that is unsupported by IE anyway.
                    case "expires":
                        Long expires = parseExpire(value);
                        // Backwards compatibility: STRING version is in seconds
                        parsable.addDissection(inputname, "STRING",     "expires", expires / 1000);
                        parsable.addDissection(inputname, "TIME.EPOCH", "expires", expires);
                        break;
                    case "domain":
                        parsable.addDissection(inputname, "STRING", "domain",   value);
                        break;
                    case "comment":
                        parsable.addDissection(inputname, "STRING", "comment",  value);
                        break;
                    case "path":
                        parsable.addDissection(inputname, "STRING", "path",     value);
                        break;
                    default: // Ignore anything else
                }
            }
        }
    }

    // --------------------------------------------

    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'").withZone(ZoneOffset.UTC),
        DateTimeFormatter.ofPattern("EEE',' dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneOffset.UTC),
        DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z")  .withZone(ZoneOffset.UTC),
    };

    private Long parseExpire(String expireString) {
        for (DateTimeFormatter dateFormat: DATE_FORMATS) {
            try {
                return dateFormat.parse(expireString, ZonedDateTime::from).toEpochSecond() * 1000;
            } catch (IllegalArgumentException iae) {
                // Ignore and continue
            }
        }
        return 0L;
    }

}
