/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

public class FooDissector extends SimpleDissector {

    private static HashMap<String, EnumSet<Casts>> dissectorConfig = new HashMap<>();
    static {
        dissectorConfig.put("ANY:fooany",         STRING_OR_LONG_OR_DOUBLE);
        dissectorConfig.put("STRING:foostring",   STRING_ONLY);
        dissectorConfig.put("INT:fooint",         STRING_OR_LONG);
        dissectorConfig.put("LONG:foolong",       STRING_OR_LONG);
        dissectorConfig.put("FLOAT:foofloat",     STRING_OR_DOUBLE);
        dissectorConfig.put("DOUBLE:foodouble",   STRING_OR_DOUBLE);
    }

    public FooDissector() {
        super("FOOINPUT", dissectorConfig);
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
        parsable.addDissection(inputname, "ANY",    "fooany",    "42");
        parsable.addDissection(inputname, "STRING", "foostring", "42");
        parsable.addDissection(inputname, "INT",    "fooint",    42);
        parsable.addDissection(inputname, "LONG",   "foolong",   42L);
        parsable.addDissection(inputname, "FLOAT",  "foofloat",  42F);
        parsable.addDissection(inputname, "DOUBLE", "foodouble", 42D);
    }
}
