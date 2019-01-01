/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

    private static HashMap<String, EnumSet<Casts>> dissectorConfig = new HashMap<>();
    static {
        dissectorConfig.put("ANY:any",         STRING_OR_LONG_OR_DOUBLE);
        dissectorConfig.put("STRING:string",   STRING_ONLY);
        dissectorConfig.put("INT:int",         STRING_OR_LONG);
        dissectorConfig.put("LONG:long",       STRING_OR_LONG);
        dissectorConfig.put("FLOAT:float",     STRING_OR_DOUBLE);
        dissectorConfig.put("DOUBLE:double",   STRING_OR_DOUBLE);
    }

    public UltimateDummyDissector() {
        super("INPUT", dissectorConfig);
    }

    public UltimateDummyDissector(String inputType) {
        super(inputType, dissectorConfig);
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        setInputType(settings);
        return true;
    }


}
