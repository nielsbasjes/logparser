/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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
package nl.basjes.parse.core;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class Parsable<RECORD> {

    private static final Logger LOG = LoggerFactory.getLogger(Parsable.class);

    private final Parser<RECORD>           parser;

    // The actual record for which all the information is intended.
    private final RECORD                   record;

    // This caches the values and intermediate values
    private final Map<String, ParsedField> cache      = new TreeMap<>();

    // The end nodes we really need as output
    // Values look like "TYPE:foo.bar"
    private final Set<String>              needed;

    // Values look like "TYPE:foo.bar"
    private final Set<String>              usefulIntermediates;

    // The set of ParsedFields that need to be parsed further
    private final Set<ParsedField>         toBeParsed = new HashSet<>();

    private final Map<String, Set<String>> typeRemappings;

    // --------------------------------------------

    public Parsable(final Parser<RECORD> parser, final RECORD record, Map<String, Set<String>> typeRemappings) {
        this.parser = parser;
        this.record = record;
        this.typeRemappings = typeRemappings;
        needed = parser.getNeeded();
        usefulIntermediates = parser.getUsefulIntermediateFields();
    }

    // --------------------------------------------
    /** Store a newly parsed value in the result set */
    public void setRootDissection(final String type, final String value) {
        LOG.debug("Got root dissection: type={}", type);

        // The root name is an empty string
        final ParsedField parsedfield = new ParsedField(type, "", value);

        cache.put(parsedfield.getId(), parsedfield);
        toBeParsed.add(parsedfield);
    }

    // --------------------------------------------

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final int value) throws DissectionFailure {
        LOG.debug("Got new (int) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, new Value((long)value), false);
    }

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final long value) throws DissectionFailure {
        LOG.debug("Got new (long) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, new Value(value), false);
    }

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final Long value) throws DissectionFailure {
        LOG.debug("Got new (Long) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, new Value(value), false);
    }

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final float value) throws DissectionFailure {
        LOG.debug("Got new (float) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, new Value((double)value), false);
    }

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final Double value) throws DissectionFailure {
        LOG.debug("Got new (Double) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, new Value(value), false);
    }

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final String value) throws DissectionFailure {
        LOG.debug("Got new (String) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, new Value(value), false);
    }

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final Value value) throws DissectionFailure {
        LOG.debug("Got new (Value) dissection: base={}; type={}; name=\"{}\"", base, type, name);
        addDissection(base, type, name, value, false);
    }

    private void addDissection(
            final String base,
            final String type,
            final String name,
            final Value value,
            final boolean recursion)
            throws DissectionFailure {
        String completeName;
        String neededWildCardName;
        if (base.isEmpty()) { // The root name is an empty string
            completeName = name;
            neededWildCardName = type + ':' + "*";
        } else {
            if (name.isEmpty()) {
                completeName = base;
            } else {
                completeName = base + '.' + name;
            }
            neededWildCardName = type + ':' + base + ".*";
        }
        String neededName = type + ':' + completeName;

        if (!recursion) {
            if (typeRemappings.containsKey(completeName)) {
                Set<String> typeRemappingSet = typeRemappings.get(completeName);
                for (String typeRemapping : typeRemappingSet) {
                    if (type.equals(typeRemapping)) {
                        throw new DissectionFailure(
                                "[Type Remapping] Trying to map to the same type (mapping definition bug!): " +
                                        " base=" + base + " type=" + type + " name=" + name);
                    }
                    addDissection(base, typeRemapping, name, value, true);
                }
            }
        }

        final ParsedField parsedfield = new ParsedField(type, completeName, value);

        if (usefulIntermediates.contains(completeName)) {
            cache.put(parsedfield.getId(), parsedfield);
            toBeParsed.add(parsedfield);
        }

        if (needed.contains(neededName)) {
            parser.store(record, neededName, neededName, value);
        }

        if (needed.contains(neededWildCardName)) {
            parser.store(record, neededWildCardName, neededName, value);
        }

    }

    // --------------------------------------------

    public ParsedField getParsableField(final String type, final String name) {
        return cache.get(ParsedField.makeId(type, name));
    }

    // --------------------------------------------

    public RECORD getRecord() {
        return record;
    }

    // --------------------------------------------

    public void setAsParsed(final ParsedField parsedField) {
        toBeParsed.remove(parsedField);
    }

    // --------------------------------------------

    public Set<ParsedField> getToBeParsed() {
        return toBeParsed;
    }

}
