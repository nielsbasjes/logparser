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
package nl.basjes.parse.core.test;

import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalValuesDissector extends UltimateDummyDissector {
    private static final Logger LOG = LoggerFactory.getLogger(NormalValuesDissector.class);

    public NormalValuesDissector() {
    }

    public NormalValuesDissector(String inputType) {
        super(inputType);
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
        LOG.info("Outputting \"NORMAL\" values");
        parsable
            .addDissection(inputname, "ANY",    "any",    "42")
            .addDissection(inputname, "STRING", "string", "FortyTwo")
            .addDissection(inputname, "INT",    "int",    42)
            .addDissection(inputname, "LONG",   "long",   42L)
            .addDissection(inputname, "FLOAT",  "float",  42F)
            .addDissection(inputname, "DOUBLE", "double", 42D);
    }
}
