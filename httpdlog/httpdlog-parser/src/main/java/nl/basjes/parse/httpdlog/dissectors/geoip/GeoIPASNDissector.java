/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AsnResponse;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;

public class GeoIPASNDissector extends AbstractGeoIPDissector {

    @SuppressWarnings("unused") // Used via reflection
    public GeoIPASNDissector() {
        super();
    }

    public GeoIPASNDissector(String databaseFileName) {
        super(databaseFileName);
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();

        result.add("ASN:asn.number");
        result.add("STRING:asn.organization");

        return result;
    }

    private boolean wantAsnNumber = false;
    private boolean wantAsnOrganization = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = extractFieldName(inputname, outputname);

        switch (name) {
            case "asn.number":
                wantAsnNumber = true;
                return STRING_OR_LONG;

            case "asn.organization":
                wantAsnOrganization = true;
                return STRING_ONLY;

            default:
                return NO_CASTS;
        }
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure {
        AsnResponse response;
        try {
            response = reader.asn(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            return;
        }

        extractAsnFields(parsable, inputname, response);
    }

    protected void extractAsnFields(final Parsable<?> parsable, final String inputname, AsnResponse response) throws DissectionFailure {
        if (wantAsnNumber) {
            parsable.addDissection(inputname, "ASN", "asn.number", response.getAutonomousSystemNumber());
        }
        if (wantAsnOrganization) {
            parsable.addDissection(inputname, "STRING", "asn.organization", response.getAutonomousSystemOrganization());
        }
    }
    // --------------------------------------------

}
