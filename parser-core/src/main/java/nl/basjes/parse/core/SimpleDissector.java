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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.core.Casts.NO_CASTS;

public abstract class SimpleDissector extends Dissector {

    String inputType;
    // Using HashMap instead of Map<> because a Map<> is not Serializable
    private HashMap<String, EnumSet<Casts>> outputTypes;

    private HashMap<String, EnumSet<Casts>> outputCasts;

    public SimpleDissector(String inputType, Map<String, EnumSet<Casts>> outputTypes) {
        this.inputType = inputType;
        this.outputTypes = new HashMap<>(outputTypes);

        outputCasts = new HashMap<>(outputTypes.size());
        for (Map.Entry<String, EnumSet<Casts>> type: outputTypes.entrySet()) {
            outputCasts.put(type.getKey().split(":", 2)[1], type.getValue());
        }
    }

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public void setInputType(String nInputType) {
        inputType = nInputType;
    }

    @Override
    public List<String> getPossibleOutput() {
        return new ArrayList<>(outputTypes.keySet());
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        String name = extractFieldName(inputname, outputname);
        return outputCasts.getOrDefault(name, NO_CASTS);
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        if (newInstance instanceof SimpleDissector) {
            SimpleDissector dissector = (SimpleDissector) newInstance;
            dissector.inputType     = inputType;
            dissector.outputTypes   = outputTypes;
            dissector.outputCasts   = outputCasts;
        }
    }

    @Override
    public final void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(getInputType(), inputname);
        Value value = field.getValue();
        if (value == null) {
            return; // Nothing to do here
        }
        dissect(parsable, inputname, value);
    }

    public abstract void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure;

}
