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
package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.util.EnumSet;
import java.util.HashMap;

import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_DOUBLE;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG_OR_DOUBLE;

public class BarDissector extends SimpleDissector {

    private static final HashMap<String, EnumSet<Casts>> DISSECTOR_CONFIG = new HashMap<>();
    static {
        DISSECTOR_CONFIG.put("ANY:barany",         STRING_OR_LONG_OR_DOUBLE);
        DISSECTOR_CONFIG.put("STRING:barstring",   STRING_ONLY);
        DISSECTOR_CONFIG.put("INT:barint",         STRING_OR_LONG);
        DISSECTOR_CONFIG.put("LONG:barlong",       STRING_OR_LONG);
        DISSECTOR_CONFIG.put("FLOAT:barfloat",     STRING_OR_DOUBLE);
        DISSECTOR_CONFIG.put("DOUBLE:bardouble",   STRING_OR_DOUBLE);
    }

    public BarDissector() {
        super("BARINPUT", DISSECTOR_CONFIG);
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
        parsable.addDissection(inputname, "ANY",    "barany",    "42");
        parsable.addDissection(inputname, "STRING", "barstring", "42");
        parsable.addDissection(inputname, "INT",    "barint",    42);
        parsable.addDissection(inputname, "LONG",   "barlong",   42L);
        parsable.addDissection(inputname, "FLOAT",  "barfloat",  42F);
        parsable.addDissection(inputname, "DOUBLE", "bardouble", 42D);
    }
}
