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
package nl.basjes.parse.core.test;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.SimpleDissector;

import java.util.EnumSet;
import java.util.HashMap;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_DOUBLE;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG_OR_DOUBLE;

/**
 * A dummy dissector to ensure retrieving all types works in the various wrappers
 */
public abstract class UltimateDummyDissector extends SimpleDissector {

    private static final HashMap<String, EnumSet<Casts>> DISSECTOR_CONFIG = new HashMap<>();
    static {
        DISSECTOR_CONFIG.put("ANY:any",         STRING_OR_LONG_OR_DOUBLE);
        DISSECTOR_CONFIG.put("STRING:string",   STRING_ONLY);
        DISSECTOR_CONFIG.put("INT:int",         STRING_OR_LONG);
        DISSECTOR_CONFIG.put("LONG:long",       STRING_OR_LONG);
        DISSECTOR_CONFIG.put("FLOAT:float",     STRING_OR_DOUBLE);
        DISSECTOR_CONFIG.put("DOUBLE:double",   STRING_OR_DOUBLE);
    }

    public UltimateDummyDissector() {
        super("INPUT", DISSECTOR_CONFIG);
    }

    public UltimateDummyDissector(String inputType) {
        super(inputType, DISSECTOR_CONFIG);
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        setInputType(settings);
        return true;
    }


}
