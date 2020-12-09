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
package nl.basjes.parse.httpdlog.dissectors.translate;

import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;

public class ConvertSecondsWithMillisStringDissector extends TypeConvertBaseDissector {
    public ConvertSecondsWithMillisStringDissector() {
        super();
    }

    public ConvertSecondsWithMillisStringDissector(String inputType, String nOutputType) {
        super(inputType, nOutputType);
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
        String[] epochStrings = value.getString().split("\\.", 2);
        Long seconds =  Long.parseLong(epochStrings[0]);
        Long milliseconds =  Long.parseLong(epochStrings[1]);
        Long epoch = seconds * 1000 + milliseconds;

        parsable.addDissection(inputname, outputType, "", epoch);
    }
}

