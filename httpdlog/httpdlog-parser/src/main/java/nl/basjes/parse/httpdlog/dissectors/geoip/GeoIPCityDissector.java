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
import com.maxmind.geoip2.model.AbstractCityResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.List;

public class GeoIPCityDissector extends GeoIPCountryDissector {

    public GeoIPCityDissector() {
        super();
    }

    public GeoIPCityDissector(String databaseFileName) {
        super(databaseFileName);
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = super.getPossibleOutput();

        result.add("STRING:subdivision.name");
        result.add("STRING:subdivision.iso");
        result.add("STRING:city.name");
        result.add("STRING:postal.code");
        result.add("STRING:location.latitude");
        result.add("STRING:location.longitude");

        return result;
    }

    private boolean wantSubdivisionName     = false;
    private boolean wantSubdivisionIso      = false;
    private boolean wantCityName            = false;
    private boolean wantPostalCode          = false;
    private boolean wantLocationLatitude    = false;
    private boolean wantLocationLongitude   = false;
    private boolean wantLocationTimezone    = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        EnumSet<Casts> result = super.prepareForDissect(inputname, outputname);
        if (result != null) {
            return result;
        }

        String name = outputname;
        if (!inputname.isEmpty()) {
            name = outputname.substring(inputname.length() + 1);
        }

        if ("subdivision.name".equals(name)) {
            wantSubdivisionName = true;
            return Casts.STRING_ONLY;
        }
        if ("subdivision.iso".equals(name)) {
            wantSubdivisionIso = true;
            return Casts.STRING_ONLY;
        }
        if ("city.name".equals(name)) {
            wantCityName = true;
            return Casts.STRING_ONLY;
        }
        if ("postal.code".equals(name)) {
            wantPostalCode = true;
            return Casts.STRING_ONLY;
        }
        if ("location.latitude".equals(name)) {
            wantLocationLatitude = true;
            return Casts.STRING_OR_DOUBLE;
        }
        if ("location.longitude".equals(name)) {
            wantLocationLongitude = true;
            return Casts.STRING_OR_DOUBLE;
        }
        if ("location.timezone".equals(name)) {
            wantLocationTimezone = true;
            return Casts.STRING_ONLY;
        }
        return null;
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure {
        // City is the 'Country' + more details.
        CityResponse response;
        try {
            response = reader.city(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            return;
        }

        if (response == null) {
            return;
        }

        extractCountryFields(parsable, inputname, response);
        extractCityFields(parsable, inputname, response);
    }

    protected void extractCityFields(final Parsable<?> parsable, final String inputname, AbstractCityResponse response) throws DissectionFailure {
        Subdivision subdivision = response.getMostSpecificSubdivision();
        if (subdivision != null) {
            if (wantSubdivisionName) {
                parsable.addDissection(inputname, "STRING", "subdivision.name", subdivision.getName());
            }
            if (wantSubdivisionIso) {
                parsable.addDissection(inputname, "STRING", "subdivision.iso", subdivision.getIsoCode());
            }
        }

        if (wantCityName) {
            City city = response.getCity();
            if (city != null) {
                parsable.addDissection(inputname, "STRING", "city.name", city.getName());
            }
            // TODO: city.getConfidence()
        }

        if (wantPostalCode) {
            Postal postal = response.getPostal();
            if (postal != null) {
                parsable.addDissection(inputname, "STRING", "postal.code", postal.getCode());
            }
            // TODO: postal.getConfidence()
        }

        Location location = response.getLocation();
        if (location != null) {
            if (wantLocationLatitude) {
                parsable.addDissection(inputname, "STRING", "location.latitude", location.getLatitude());
            }
            if (wantLocationLongitude) {
                parsable.addDissection(inputname, "STRING", "location.longitude", location.getLongitude());
            }
            if (wantLocationTimezone) {
                parsable.addDissection(inputname, "STRING", "location.timezone", location.getTimeZone());
            }
            // TODO: location.getAccuracyRadius()
            // TODO: location.getAverageIncome();
            // TODO: location.getMetroCode();
            // TODO: location.getPopulationDensity();
        }
    }

    // --------------------------------------------

}
