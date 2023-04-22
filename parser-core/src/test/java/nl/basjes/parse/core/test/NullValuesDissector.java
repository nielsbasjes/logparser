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
package nl.basjes.parse.core.test;

import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullValuesDissector extends UltimateDummyDissector {
    private static final Logger LOG = LoggerFactory.getLogger(NullValuesDissector.class);

    public NullValuesDissector() {
    }

    public NullValuesDissector(String inputType) {
        super(inputType);
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
        LOG.info("Outputting \"NULL\" values");
        parsable
            .addDissection(inputname, "ANY",    "any",    (String) null)
            .addDissection(inputname, "STRING", "string", (String) null)
            .addDissection(inputname, "INT",    "int",    (Long)   null)
            .addDissection(inputname, "LONG",   "long",   (Long)   null)
            .addDissection(inputname, "FLOAT",  "float",  (Double) null)
            .addDissection(inputname, "DOUBLE", "double", (Double) null);
    }
}
