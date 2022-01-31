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
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.STRING_ONLY;

public class StrfTimeStampDissector extends Dissector {

    private final TimeStampDissector timeStampDissector;

    private String strfDateTimePattern = null;
    private String inputType = "TIME.?????";

    public StrfTimeStampDissector() {
        timeStampDissector = new TimeStampDissector();
    }

    public void setDateTimePattern(String newDateTimePattern) {
        if (newDateTimePattern == null) {
            timeStampDissector.setDateTimePattern("");
            return; // Done
        }

        if (newDateTimePattern.equals(strfDateTimePattern)) {
            return; // Nothing to do
        }

        this.strfDateTimePattern = newDateTimePattern;
        timeStampDissector.setFormatter(StrfTimeToDateTimeFormatter.convert(newDateTimePattern));
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        setDateTimePattern(settings);
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(inputType, inputname);
        timeStampDissector.dissect(field, parsable, inputname);
    }

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public List<String> getPossibleOutput() {
        return timeStampDissector.getPossibleOutput();
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        return timeStampDissector.prepareForDissect(inputname, outputname);
    }

    @Override
    public void prepareForRun() {
        timeStampDissector.prepareForRun();
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        StrfTimeStampDissector newStrfTimeStampDissector = (StrfTimeStampDissector) newInstance;
        newStrfTimeStampDissector.timeStampDissector.initializeNewInstance(newStrfTimeStampDissector.timeStampDissector);
        newStrfTimeStampDissector.setInputType(inputType);
        newStrfTimeStampDissector.setDateTimePattern(strfDateTimePattern);
    }

    @Override
    public void setInputType(String newInputType) {
        inputType = newInputType;
    }

    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        parser.addDissector(new LocalizedTimeDissector(inputType));
    }

    public static class LocalizedTimeDissector extends Dissector {

        String inputType = null;

        public LocalizedTimeDissector() {
        }

        public LocalizedTimeDissector(String inputType) {
            this.inputType = inputType;
        }

        @Override
        public void setInputType(String newInputType) {
            inputType = newInputType;
        }

        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            setInputType(settings);
            return true;
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            parsable.addDissection(inputname, "TIME.LOCALIZEDSTRING", "", field.getValue());
        }

        @Override
        public String getInputType() {
            return inputType;
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("TIME.LOCALIZEDSTRING:");
            return result;
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return STRING_ONLY;
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
            newInstance.setInputType(inputType);
        }
    }
}
