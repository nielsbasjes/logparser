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
package nl.basjes.parse.core.annotation;

import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.Value;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.test.UltimateDummyDissector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static class SetAllTypesNormalDissector extends UltimateDummyDissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            LOG.info("Outputting \"NORMAL\" values");
            super.dissect(parsable, inputname, value);
        }
    }

    public static class SetAllTypesNullDissector extends UltimateDummyDissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            LOG.info("Outputting \"NULL\" values");
            parsable.addDissection(inputname, "ANY",    "any",    (String) null);
            parsable.addDissection(inputname, "STRING", "string", (String) null);
            parsable.addDissection(inputname, "INT",    "int",    (Long)   null);
            parsable.addDissection(inputname, "LONG",   "long",   (Long)   null);
            parsable.addDissection(inputname, "FLOAT",  "float",  (Double) null);
            parsable.addDissection(inputname, "DOUBLE", "double", (Double) null);
        }
    }

    public static class SetAllTypesEmptyDissector extends UltimateDummyDissector {
        @Override
        public void dissect(Parsable<?> parsable, String inputname, Value value) throws DissectionFailure {
            LOG.info("Outputting \"EMPTY\" values");
            parsable.addDissection(inputname, "ANY",    "any",    "");
            parsable.addDissection(inputname, "STRING", "string", "");
            parsable.addDissection(inputname, "INT",    "int",    "");
            parsable.addDissection(inputname, "LONG",   "long",   "");
            parsable.addDissection(inputname, "FLOAT",  "float",  "");
            parsable.addDissection(inputname, "DOUBLE", "double", "");
        }
    }

    public static void isPresent(Map<String, ?> results, String field, Object value) {
        if (value == null) {
            assertTrue("The field \""+field+"\" is missing (a null value was expected).", results.containsKey(field));
            Object actualValue = results.get(field);
            if (actualValue != null) {
                assertEquals("The field \"" + field + "\" should be null but it was: " +
                    "(" + actualValue.getClass().getSimpleName() + ")\"" + actualValue + "\" ", value, actualValue);
            }
        } else {
            assertTrue("The field \""+field+"\" is missing (an entry of type "+value.getClass().getSimpleName()+" was expected).",
                results.containsKey(field));
            assertEquals("The field \"" + field + "\" should have the value (" +
                value
                + ")\"" + value.toString() + "\"is missing", value, results.get(field));
        }
    }

    public static void isAbsent(Map<String, ?> results, String field) {
        Object value = results.get(field);
        if (value != null) {
            fail("The value \""+value+"\" was found for field \""+field+"\"");
        } else {
            assertFalse("A null value was found for field \"" + field + "\"", results.containsKey(field));
        }
    }
}
