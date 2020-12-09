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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestRecord {

    private static final Logger LOG = LoggerFactory.getLogger(DissectorTester.class);

    private final Map<String, Set<String>>  stringMap   = new HashMap<>(32);
    private final Map<String, Set<Long>>    longMap     = new HashMap<>(32);
    private final Map<String, Set<Double>>  doubleMap   = new HashMap<>(32);

    public void setVerbose() {
        this.verbose = true;
    }

    boolean verbose = false;

    public void setStringValue(final String name, final String value) {
        if (verbose) {
            LOG.info("Received String: {} = {}", name, value);
        }
        stringMap.computeIfAbsent(name, s -> new HashSet<>()).add(value);
    }

    public void setLongValue(final String name, final Long value) {
        if (verbose) {
            LOG.info("Received Long  : {} = {}", name, value);
        }
        longMap.computeIfAbsent(name, s -> new HashSet<>()).add(value);
    }

    public void setDoubleValue(final String name, final Double value) {
        if (verbose) {
            LOG.info("Received Double: {} = {}", name, value);
        }
        doubleMap.computeIfAbsent(name, s -> new HashSet<>()).add(value);
    }

    public String  getStringValue(final String name) {
        Set<String> value = stringMap.get(name);
        if (value == null) {
            return null;
        }
        return value.iterator().next();
    }

    public Long    getLongValue(final String name) {
        Set<Long> value = longMap.get(name);
        if (value == null) {
            return null;
        }
        return value.iterator().next();
    }

    public Double  getDoubleValue(final String name) {
        Set<Double> value = doubleMap.get(name);
        if (value == null) {
            return null;
        }
        return value.iterator().next();
    }

    public Set<String>  getStringValues(final String name) {
        return stringMap.get(name);
    }

    public Set<Long>    getLongValues(final String name) {
        return longMap.get(name);
    }

    public Set<Double>  getDoubleValues(final String name) {
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

    public TestRecord expectString(String field, String... values) {
        if (values == null){
            isPresent(stringMap, field, values);
        } else {
            for (String value : values) {
                isPresent(stringMap, field, value);
            }
        }
        return this;
    }

    public TestRecord expectLong(String field, Long... values) {
        if (values == null){
            isPresent(longMap, field, values);
        } else {
            for (Long value : values) {
                isPresent(longMap, field, value);
            }
        }
        return this;
    }

    public TestRecord expectDouble(String field, Double... values) {
        if (values == null){
            isPresent(doubleMap, field, values);
        } else {
            for (Double value : values) {
                isPresent(doubleMap, field, value);
            }
        }
        return this;
    }

    private void isPresent(Map<String, ?> results, String field, Object value) {
        if (value == null) {
            assertTrue("The field \""+field+"\" is missing (a null value was expected).", results.containsKey(field));
            Object actualValue = results.get(field);
            assertNotNull("The field \"" + field + "\" should be present but it is not", actualValue);
            assertTrue("Invalid type used, result must be a Set<?>", actualValue instanceof Set);
            Set<?> actualValues = (Set<?>)actualValue;
            for (Object actualValuee: actualValues) {
                assertNull("The field \"" + field + "\" should only have null values but we found: " +
                    "(" + actualValue.getClass().getSimpleName() + ")\"" + actualValue + "\" ", actualValuee);
            }
        } else {
            assertTrue("The field \""+field+"\" is missing (an entry of type "+value.getClass().getSimpleName()+" was expected).",
                results.containsKey(field));
            Object result = results.get(field);
            assertTrue("Invalid type used, result must be a Set<?>", result instanceof Set);
            Set<?> resultSet = (Set<?>)result;
            assertTrue("The field \"" + field + "\" should have the value (" +
                value
                + ")\"" + value.toString() + "\"is missing", resultSet.contains(value));
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
            assertFalse("A null value was found for field \"" + field + "\"", results.containsKey(field) || results.get(field) != null);
        }
    }

    public void clear() {
        stringMap.clear();
        longMap.clear();
        doubleMap.clear();
    }
}
