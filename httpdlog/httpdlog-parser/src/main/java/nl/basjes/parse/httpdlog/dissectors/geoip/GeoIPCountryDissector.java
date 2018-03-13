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

        return result;
    }

    private boolean wantContinentName   = false;
    private boolean wantContinentCode   = false;
    private boolean wantCountryName     = false;
    private boolean wantCountryIso      = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname;
        if (!inputname.isEmpty()) {
            name = outputname.substring(inputname.length() + 1);
        }

        if ("continent.name".equals(name)) {
            wantContinentName = true;
            return Casts.STRING_ONLY;
        }
        if ("continent.code".equals(name)) {
            wantContinentCode = true;
            return Casts.STRING_ONLY;
        }
        if ("country.name".equals(name)) {
            wantCountryName = true;
            return Casts.STRING_ONLY;
        }
        if ("country.iso".equals(name)) {
            wantCountryIso = true;
            return Casts.STRING_ONLY;
        }
        return null;
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
        Continent continent = response.getContinent();
        if (continent != null) {
            if (wantContinentName) {
                parsable.addDissection(inputname, "STRING", "continent.name", continent.getName());
            }
            if (wantContinentCode) {
                parsable.addDissection(inputname, "STRING", "continent.code", continent.getCode());
            }
        }
        Country country = response.getCountry();
        if (country != null) {
            if (wantCountryName) {
                parsable.addDissection(inputname, "STRING", "country.name", country.getName());
            }
            if (wantCountryIso) {
                parsable.addDissection(inputname, "STRING", "country.iso", country.getIsoCode());
            }
        }
    }
    // --------------------------------------------

}
