/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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
package nl.basjes.parse.core.nl.basjes.parse.core.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

    public String getStringValue(final String name) {
        return stringMap.get(name);
    }
    public Long getLongValue(final String name) {
        return longMap.get(name);
    }
    public Double getDoubleValue(final String name) {
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


    public void clear() {
        stringMap.clear();
        longMap.clear();
        doubleMap.clear();
    }
}
