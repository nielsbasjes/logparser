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
import com.maxmind.geoip2.model.AbstractCountryResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GeoIPCountryDissector extends AbstractGeoIPDissector {

    @SuppressWarnings("unused") // Used via reflection
    public GeoIPCountryDissector() {
        super();
    }

    public GeoIPCountryDissector(String databaseFileName) {
        super(databaseFileName);
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();

        result.add("STRING:continent.name");
        result.add("STRING:continent.code");
        result.add("STRING:country.name");
        result.add("STRING:country.iso");
        result.add("NUMBER:country.getconfidence");
        result.add("BOOLEAN:country.isineuropeanunion");

        return result;
    }

    private boolean wantContinentName               = false;
    private boolean wantContinentCode               = false;
    private boolean wantAnyContinent                = false;

    private boolean wantCountryName                 = false;
    private boolean wantCountryIso                  = false;
    private boolean wantCountryGetConfidence        = false;
    private boolean wantCountryIsInEuropeanUnion    = false;
    private boolean wantAnyCountry                  = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = extractFieldName(inputname, outputname);

        switch (name) {
            case "continent.name":
                wantContinentName = true;
                wantAnyContinent = true;
                return Casts.STRING_ONLY;

            case "continent.code":
                wantContinentCode = true;
                wantAnyContinent = true;
                return Casts.STRING_ONLY;

            case "country.name":
                wantCountryName = true;
                wantAnyCountry = true;
                return Casts.STRING_ONLY;

            case "country.iso":
                wantCountryIso = true;
                wantAnyCountry = true;
                return Casts.STRING_ONLY;

            case "country.getconfidence":
                wantCountryGetConfidence = true;
                wantAnyCountry = true;
                return Casts.STRING_OR_LONG;

            case "country.isineuropeanunion":
                wantCountryIsInEuropeanUnion = true;
                wantAnyCountry = true;
                return Casts.STRING_OR_LONG;

            default:
                return null;
        }
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress)
        throws DissectionFailure {
        CountryResponse response;
        try {
            response = reader.country(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            return;
        }

        if (response == null) {
            return;
        }
        extractCountryFields(parsable, inputname, response);
    }

    protected void extractCountryFields(final Parsable<?> parsable, final String inputname, AbstractCountryResponse response)
        throws DissectionFailure {
        if (wantAnyContinent) {
            Continent continent = response.getContinent();
            if (continent != null) {
                if (wantContinentName) {
                    parsable.addDissection(inputname, "STRING", "continent.name", continent.getName());
                }
                if (wantContinentCode) {
                    parsable.addDissection(inputname, "STRING", "continent.code", continent.getCode());
                }
            }
        }
        if (wantAnyCountry) {
            Country country = response.getCountry();
            if (country != null) {
                if (wantCountryName) {
                    parsable.addDissection(inputname, "STRING", "country.name", country.getName());
                }
                if (wantCountryIso) {
                    parsable.addDissection(inputname, "STRING", "country.iso", country.getIsoCode());
                }

                if (wantCountryGetConfidence) {
                    parsable.addDissection(inputname, "NUMBER", "country.getconfidence", country.getConfidence());
                }
                if (wantCountryIsInEuropeanUnion) {
                    parsable.addDissection(inputname, "BOOLEAN", "country.isineuropeanunion", country.isInEuropeanUnion() ? 1L : 0L);
                }
            }
        }
    }
    // --------------------------------------------

}
