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

public class GeoIPASNDissector extends AbstractGeoIPDissector {

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();

        result.add("ASN:asn.number");
        result.add("STRING:asn.organization");

        return result;
    }

    boolean wantAsnNumber = false;
    boolean wantAsnOrganization = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname;
        if (!inputname.isEmpty()) {
            name = outputname.substring(inputname.length() + 1);
        }

        if ("asn.number".equals(name)) {
            wantAsnNumber = true;
            return Casts.STRING_OR_LONG;
        }
        if ("asn.organization".equals(name)) {
            wantAsnOrganization = true;
            return Casts.STRING_ONLY;
        }
        return null;
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure {
        AsnResponse response;
        try {
            response = reader.asn(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            return;
        }

        if (response == null) {
            return;
        }

        if (wantAsnNumber) {
            parsable.addDissection(inputname, "ASN", "asn.number", response.getAutonomousSystemNumber());
        }
        if (wantAsnOrganization) {
            parsable.addDissection(inputname, "STRING", "asn.organization", response.getAutonomousSystemOrganization());
        }
    }
    // --------------------------------------------

}
