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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestRecord {

    private static final Logger LOG = LoggerFactory.getLogger(DissectorTester.class);

    private final Map<String, String> stringMap = new HashMap<>(32);
    private final Map<String, Long> longMap = new HashMap<>(32);
    private final Map<String, Double> doubleMap = new HashMap<>(32);

    public void setVerbose() {
        this.verbose = true;
    }

    boolean verbose = false;

    public void setStringValue(final String name, final String value) {
        if (verbose) {
            LOG.info("Received String: {} = {}", name, value);
        }
        stringMap.put(name, value);
    }
    public void setLongValue(final String name, final Long value) {
        if (verbose) {
            LOG.info("Received Long  : {} = {}", name, value);
        }
        longMap.put(name, value);
    }
    public void setDoubleValue(final String name, final Double value) {
        if (verbose) {
            LOG.info("Received Double: {} = {}", name, value);
        }
        doubleMap.put(name, value);
    }

    public String  getStringValue(final String name) {
        return stringMap.get(name);
    }
    public Long    getLongValue(final String name) {
        return longMap.get(name);
    }
    public Double  getDoubleValue(final String name) {
        return doubleMap.get(name);
    }

    public boolean hasStringValue(final String name) {
        return stringMap.containsKey(name);
    }
    public boolean hasLongValue(final String name) {
        return longMap.containsKey(name);
    }
    public boolean hasDoubleValue(final String name) {
        return doubleMap.containsKey(name);
    }

    public TestRecord expectString(String field, String value) {
        isPresent(stringMap, field, value);
        return this;
    }

    public TestRecord expectLong(String field, Long value) {
        isPresent(longMap, field, value);
        return this;
    }

    public TestRecord expectDouble(String field, Double value) {
        isPresent(doubleMap, field, value);
        return this;
    }

    private void isPresent(Map<String, ?> results, String field, Object value) {
        if (value == null) {
            assertTrue("The field \""+field+"\" is missing (a null value was expected).", results.containsKey(field));
            Object actualValue = results.get(field);
            if (actualValue != null) {
                assertEquals("The field \"" + field + "\" should be null but it was: " +
                    "(" + actualValue.getClass().getSimpleName() + ")\"" + actualValue + "\" ", null, actualValue);
            }
        } else {
            assertTrue("The field \""+field+"\" is missing (an entry of type "+value.getClass().getSimpleName()+" was expected).",
                results.containsKey(field));
            assertEquals("The field \"" + field + "\" should have the value (" +
                value
                + ")\"" + value.toString() + "\"is missing", value, results.get(field));
        }
    }

    public TestRecord noString(String field) {
        isAbsent(stringMap, field);
        return this;
    }

    public TestRecord noLong(String field) {
        isAbsent(longMap, field);
        return this;
    }

    public TestRecord noDouble(String field) {
        isAbsent(doubleMap, field);
        return this;
    }

    private void isAbsent(Map<String, ?> results, String field) {
        Object value = results.get(field);
        if (value != null) {
            fail("The value \""+value+"\" was found for field \""+field+"\"");
        } else {
            assertFalse("A null value was found for field \"" + field + "\"", results.containsKey(field));
        }
    }

    public void clear() {
        stringMap.clear();
        longMap.clear();
        doubleMap.clear();
    }
}
