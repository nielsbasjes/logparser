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
        result.add("NUMBER:city.confidence");

        result.add("STRING:postal.code");
        result.add("NUMBER:postal.confidence");

        result.add("STRING:location.latitude");
        result.add("STRING:location.longitude");
        result.add("STRING:location.timezone");
        result.add("NUMBER:location.accuracyradius");
        result.add("NUMBER:location.averageincome");
        result.add("NUMBER:location.metrocode");
        result.add("NUMBER:location.populationdensity");

        return result;
    }

    private boolean wantSubdivisionName            = false;
    private boolean wantSubdivisionIso             = false;
    private boolean wantAnySubdivision             = false;

    private boolean wantCityName                   = false;
    private boolean wantCityConfidence             = false;
    private boolean wantAnyCity                    = false;

    private boolean wantPostalCode                 = false;
    private boolean wantPostalConfidence           = false;
    private boolean wantAnyPostal                  = false;

    private boolean wantLocationLatitude           = false;
    private boolean wantLocationLongitude          = false;
    private boolean wantLocationTimezone           = false;
    private boolean wantLocationAccuracyradius     = false;
    private boolean wantLocationAverageincome      = false;
    private boolean wantLocationMetrocode          = false;
    private boolean wantLocationPopulationdensity  = false;
    private boolean wantAnyLocation                = false;


    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        EnumSet<Casts> result = super.prepareForDissect(inputname, outputname);
        if (result != null) {
            return result;
        }

        String name = extractFieldName(inputname, outputname);

        if ("subdivision.name".equals(name)) {
            wantSubdivisionName = true;
            wantAnySubdivision = true;
            return Casts.STRING_ONLY;
        }
        if ("subdivision.iso".equals(name)) {
            wantSubdivisionIso = true;
            wantAnySubdivision = true;
            return Casts.STRING_ONLY;
        }

        if ("city.name".equals(name)) {
            wantCityName = true;
            wantAnyCity = true;
            return Casts.STRING_ONLY;
        }
        if ("city.confidence".equals(name)) {
            wantCityConfidence = true;
            wantAnyCity = true;
            return Casts.STRING_OR_LONG;
        }

        if ("postal.code".equals(name)) {
            wantPostalCode = true;
            wantAnyPostal = true;
            return Casts.STRING_ONLY;
        }
        if ("postal.confidence".equals(name)) {
            wantPostalConfidence = true;
            wantAnyPostal = true;
            return Casts.STRING_OR_LONG;
        }

        if ("location.latitude".equals(name)) {
            wantLocationLatitude = true;
            wantAnyLocation = true;
            return Casts.STRING_OR_DOUBLE;
        }
        if ("location.longitude".equals(name)) {
            wantLocationLongitude = true;
            wantAnyLocation = true;
            return Casts.STRING_OR_DOUBLE;
        }
        if ("location.accuracyradius".equals(name)) {
            wantLocationAccuracyradius = true;
            wantAnyLocation = true;
            return Casts.STRING_OR_LONG;
        }
        if ("location.timezone".equals(name)) {
            wantLocationTimezone = true;
            wantAnyLocation = true;
            return Casts.STRING_ONLY;
        }
        if ("location.averageincome".equals(name)) {
            wantLocationAverageincome = true;
            wantAnyLocation = true;
            return Casts.STRING_OR_LONG;
        }
        if ("location.metrocode".equals(name)) {
            wantLocationMetrocode = true;
            wantAnyLocation = true;
            return Casts.STRING_OR_LONG;
        }
        if ("location.populationdensity".equals(name)) {
            wantLocationPopulationdensity = true;
            wantAnyLocation = true;
            return Casts.STRING_OR_LONG;
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
        if (wantAnySubdivision) {
            Subdivision subdivision = response.getMostSpecificSubdivision();
            if (subdivision != null) {
                if (wantSubdivisionName) {
                    parsable.addDissection(inputname, "STRING", "subdivision.name", subdivision.getName());
                }
                if (wantSubdivisionIso) {
                    parsable.addDissection(inputname, "STRING", "subdivision.iso", subdivision.getIsoCode());
                }
            }
        }

        if (wantAnyCity) {
            City city = response.getCity();
            if (city != null) {
                if (wantCityName) {
                    parsable.addDissection(inputname, "STRING", "city.name", city.getName());
                }
                if (wantCityConfidence) {
                    parsable.addDissection(inputname, "NUMBER", "city.confidence", city.getConfidence());
                }
            }
        }

        if (wantAnyPostal) {
            Postal postal = response.getPostal();
            if (postal != null) {
                if (wantPostalCode) {
                    parsable.addDissection(inputname, "STRING", "postal.code", postal.getCode());
                }
                if (wantPostalConfidence) {
                    parsable.addDissection(inputname, "NUMBER", "postal.confidence", postal.getConfidence());
                }
            }
        }

        if (wantAnyLocation) {
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
                if (wantLocationAccuracyradius) {
                    parsable.addDissection(inputname, "NUMBER", "location.accuracyradius", location.getAccuracyRadius());
                }
                if (wantLocationAverageincome) {
                    Integer value = location.getAverageIncome();
                    if (value != null) {
                        parsable.addDissection(inputname, "NUMBER", "location.averageincome", value);
                    }
                }
                if (wantLocationMetrocode) {
                    Integer value = location.getMetroCode();
                    if (value != null) {
                        parsable.addDissection(inputname, "NUMBER", "location.metrocode", value);
                    }
                }
                if (wantLocationPopulationdensity) {
                    Integer value = location.getPopulationDensity();
                    if (value != null) {
                        parsable.addDissection(inputname, "NUMBER", "location.populationdensity", value);
                    }
                }
            }
        }
    }

    // --------------------------------------------

}
