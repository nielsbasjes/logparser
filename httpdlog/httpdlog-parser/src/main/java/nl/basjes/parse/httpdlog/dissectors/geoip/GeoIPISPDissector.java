/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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
package nl.basjes.parse.httpdlog.dissectors.geoip;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.IspResponse;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.List;

import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_ONLY;

public class GeoIPISPDissector extends GeoIPASNDissector {

    @SuppressWarnings("unused") // Used via reflection
    public GeoIPISPDissector() {
        super();
    }

    public GeoIPISPDissector(String databaseFileName) {
        super(databaseFileName);
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = super.getPossibleOutput();

        result.add("STRING:isp.name");
        result.add("STRING:isp.organization");

        return result;
    }

    private boolean wantIspName = false;
    private boolean wantIspOrganization = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        EnumSet<Casts> result = super.prepareForDissect(inputname, outputname);
        if (!result.isEmpty()) {
            return result;
        }
        String name = extractFieldName(inputname, outputname);

        switch (name) {
            case "isp.name":
                wantIspName = true;
                return STRING_ONLY;

            case "isp.organization":
                wantIspOrganization = true;
                return STRING_ONLY;

            default:
                return NO_CASTS;
        }
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure {
        IspResponse response;
        try {
            response = reader.isp(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            return;
        }

        extractAsnFields(parsable, inputname, response);
        extractIspFields(parsable, inputname, response);
    }

    protected void extractIspFields(final Parsable<?> parsable, final String inputname, IspResponse response) throws DissectionFailure {
        if (wantIspName) {
            parsable.addDissection(inputname, "STRING", "isp.name", response.getIsp());
        }
        if (wantIspOrganization) {
            parsable.addDissection(inputname, "STRING", "isp.organization", response.getOrganization());
        }
    }


    // --------------------------------------------

}
