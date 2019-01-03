/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_DOUBLE;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;

public class GeoIPCityDissector extends GeoIPCountryDissector {

    @SuppressWarnings("unused") // Used via reflection
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
        result.add("NUMBER:city.geonameid");

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
    private boolean wantCityGeoNameId              = false;
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
        if (!result.isEmpty()) {
            return result;
        }

        String name = extractFieldName(inputname, outputname);

        switch (name) {

            case "subdivision.name":
                wantSubdivisionName = true;
                wantAnySubdivision = true;
                return STRING_ONLY;

            case "subdivision.iso":
                wantSubdivisionIso = true;
                wantAnySubdivision = true;
                return STRING_ONLY;

            // ---------------------------------

            case "city.name":
                wantCityName = true;
                wantAnyCity = true;
                return STRING_ONLY;

            case "city.confidence":
                wantCityConfidence = true;
                wantAnyCity = true;
                return STRING_OR_LONG;

            case "city.geonameid":
                wantCityGeoNameId = true;
                wantAnyCity = true;
                return STRING_OR_LONG;

            // ---------------------------------

            case "postal.code":
                wantPostalCode = true;
                wantAnyPostal = true;
                return STRING_ONLY;

            case "postal.confidence":
                wantPostalConfidence = true;
                wantAnyPostal = true;
                return STRING_OR_LONG;

            // ---------------------------------

            case "location.latitude":
                wantLocationLatitude = true;
                wantAnyLocation = true;
                return STRING_OR_DOUBLE;

            case "location.longitude":
                wantLocationLongitude = true;
                wantAnyLocation = true;
                return STRING_OR_DOUBLE;

            case "location.accuracyradius":
                wantLocationAccuracyradius = true;
                wantAnyLocation = true;
                return STRING_OR_LONG;

            case "location.timezone":
                wantLocationTimezone = true;
                wantAnyLocation = true;
                return STRING_ONLY;

            case "location.averageincome":
                wantLocationAverageincome = true;
                wantAnyLocation = true;
                return STRING_OR_LONG;

            case "location.metrocode":
                wantLocationMetrocode = true;
                wantAnyLocation = true;
                return STRING_OR_LONG;

            case "location.populationdensity":
                wantLocationPopulationdensity = true;
                wantAnyLocation = true;
                return STRING_OR_LONG;
            default:
                return NO_CASTS;
        }
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
                if (wantCityGeoNameId) {
                    parsable.addDissection(inputname, "NUMBER", "city.geonameid", city.getGeoNameId());
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
