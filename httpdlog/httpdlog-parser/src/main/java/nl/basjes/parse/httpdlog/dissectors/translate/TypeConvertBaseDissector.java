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
package nl.basjes.parse.httpdlog.dissectors.translate;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.EnumSet;
import java.util.HashMap;

import static nl.basjes.parse.core.Casts.STRING_OR_LONG;

public abstract class TypeConvertBaseDissector extends SimpleDissector {
    protected String inputType;
    protected String outputType;
    public TypeConvertBaseDissector() {
        super(null, new HashMap<>());
    }

    private static HashMap<String, EnumSet<Casts>> fillOutputConfig(String outputType, EnumSet<Casts> casts) {
        HashMap<String, EnumSet<Casts>>  typeConvertConfig = new HashMap<>();
        typeConvertConfig.put(outputType + ":", casts);
        return typeConvertConfig;
    }

    public TypeConvertBaseDissector(String nInputType, String nOutputType) {
        super(nInputType, fillOutputConfig(nOutputType, STRING_OR_LONG));
        inputType = nInputType;
        outputType = nOutputType;
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        super.initializeNewInstance(newInstance);
        ((TypeConvertBaseDissector)newInstance).inputType = inputType;
        ((TypeConvertBaseDissector)newInstance).outputType = outputType;
    }
}
