/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.basjes.parse.core;

import java.util.*;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Parsable<RECORD> {

    static final Logger LOG = LoggerFactory.getLogger(Parsable.class);

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

    private final Map<String,Set<String>>  typeRemappings;

    private String                         rootname   = null;

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
    public void setRootDissection(final String type, final String name,
                                  final String value) {
        LOG.debug("Got root dissection: type={}; name=\"{}\"", type, name);

        rootname = name;

        final ParsedField parsedfield = new ParsedField(type, name, value);

        cache.put(parsedfield.getId(), parsedfield);
        toBeParsed.add(parsedfield);
    }

    // --------------------------------------------

    /** Store a newly parsed value in the result set */
    public void addDissection(final String base, final String type, final String name, final String value) throws DissectionFailure {
        LOG.debug("Got new dissection: base=" + base + "; type=" + type + "; name=\"" + name + "\"");
        addDissection(base, type, name, value, false);
    }

    private void addDissection(final String base, final String type, final String name, final String value, final boolean recursion) throws DissectionFailure {
        String completeName;
        String neededWildCardName;
        if (base.equals(rootname)) {
            completeName = name;
            neededWildCardName = type + ':' + "*";
        } else {
            completeName = base + '.' + name;
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
