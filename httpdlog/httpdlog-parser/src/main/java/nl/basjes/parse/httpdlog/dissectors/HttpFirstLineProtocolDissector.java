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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HttpFirstLineProtocolDissector extends Dissector {

    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.PROTOCOL_VERSION";
    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("HTTP.PROTOCOL:");
        result.add("HTTP.PROTOCOL.VERSION:version");
        return result;
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty() || "-".equals(fieldValue)){
            return; // Nothing to do here
        }

        String[] protocol = fieldValue.split("/", 2);

        if (protocol.length == 2) {
            outputDissection(parsable, inputname, "HTTP.PROTOCOL", "", protocol[0]);
            outputDissection(parsable, inputname, "HTTP.PROTOCOL.VERSION", "version", protocol[1]);
            return;
        }

        // In the scenario that the actual URI is too long the last part ("HTTP/1.1") may have been cut off by the
        // Apache HTTPD webserver. To still be able to parse these we try that pattern too

        parsable.addDissection(inputname, "HTTP.PROTOCOL", "", (String) null);
        parsable.addDissection(inputname, "HTTP.PROTOCOL.VERSION", "version", (String) null);
    }

    private void outputDissection(Parsable<?> parsable,
                                  String inputname,
                                  String type,
                                  String name,
                                  String value)
            throws DissectionFailure {
        if (requestedParameters.contains(name)) {
            parsable.addDissection(inputname, type, name, value);
        }
    }

    // --------------------------------------------

    private final Set<String> requestedParameters = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        requestedParameters.add(extractFieldName(inputname, outputname));
        return Casts.STRING_ONLY;
    }

    // --------------------------------------------

}
