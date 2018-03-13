/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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
package nl.basjes.parse.httpdlog.dissectors.geoip;

import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class AbstractGeoIPDissector extends Dissector {

    static final String INPUT_TYPE = "IP";

    String databaseFileName;

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        databaseFileName = settings;
        return true; // Everything went right.
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        newInstance.initializeFromSettingsParameter(databaseFileName);
    }

    // --------------------------------------------

    protected DatabaseReader reader;

    @Override
    public void prepareForRun() throws InvalidDissectorException {
        // This creates the DatabaseReader object, which should be reused across lookups.
        try {
            reader = new DatabaseReader
                .Builder(openDatabaseFile(databaseFileName))
                .fileMode(Reader.FileMode.MEMORY)
                .withCache(new CHMCache())
                .build();
        } catch (IOException e) {
            throw new InvalidDissectorException(this.getClass().getCanonicalName() + ":" + e.getMessage());
        }
    }

    protected InputStream openDatabaseFile(String filename) throws FileNotFoundException {
        return new FileInputStream(filename);
    }


    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(fieldValue);
            if (ipAddress == null) {
                return;
            }
        } catch (UnknownHostException e) {
            return;
        }

        dissect(parsable, inputname, ipAddress);
    }

    // --------------------------------------------

    abstract void dissect(Parsable<?> parsable, String inputname, InetAddress ipAddress) throws DissectionFailure;
}
