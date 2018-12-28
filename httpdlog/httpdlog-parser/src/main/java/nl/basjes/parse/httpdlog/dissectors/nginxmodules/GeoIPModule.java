/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2018 Niels Basjes
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

package nl.basjes.parse.httpdlog.dissectors.nginxmodules;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_NO_SPACE_STRING;
import static nl.basjes.parse.httpdlog.dissectors.tokenformat.TokenParser.FORMAT_STRING;

// Implement the variables described here:
// http://nginx.org/en/docs/http/ngx_http_geoip_module.html
public class GeoIPModule implements NginxModule {

    private static final String PREFIX = "nginxmodule.geoip";

    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> parsers = new ArrayList<>(60);

        // $geoip_country_code
        // two-letter country code, for example, “RU”, “US”.
        parsers.add(new TokenParser("$geoip_country_code", PREFIX + ".country.code", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_country_code3
        // three-letter country code, for example, “RUS”, “USA”.
        parsers.add(new TokenParser("$geoip_country_code3", PREFIX + ".country.code3", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_country_name
        // country name, for example, “Russian Federation”, “United States”.
        parsers.add(new TokenParser("$geoip_country_name", PREFIX + ".country.name", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_area_code telephone area code (US only).
        // This variable may contain outdated information since the corresponding database field is deprecated.
        parsers.add(new TokenParser("$geoip_area_code", PREFIX + ".area.code", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_city_continent_code
        // two-letter continent code, for example, “EU”, “NA”.
        parsers.add(new TokenParser("$geoip_city_continent_code", PREFIX + ".continent.code", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_city_country_code
        // two-letter country code, for example, “RU”, “US”.
        parsers.add(new TokenParser("$geoip_city_country_code", PREFIX + ".country.code", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_city_country_code3
        // three-letter country code, for example, “RUS”, “USA”.
        parsers.add(new TokenParser("$geoip_city_country_code3", PREFIX + ".country.code3", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_city_country_name
        // country name, for example, “Russian Federation”, “United States”.
        parsers.add(new TokenParser("$geoip_city_country_name", PREFIX + ".country.name", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_dma_code
        // DMA region code in US (also known as “metro code”), according to the geotargeting in Google AdWords API.
        parsers.add(new TokenParser("$geoip_dma_code", PREFIX + ".dma.code", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_latitude
        // latitude.
        parsers.add(new TokenParser("$geoip_latitude", PREFIX + ".location.latitude", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_longitude
        // longitude.
        parsers.add(new TokenParser("$geoip_longitude", PREFIX + ".location.longitude", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_region
        // two-symbol country region code (region, territory, state, province, federal land and the like), for example, “48”, “DC”.
        parsers.add(new TokenParser("$geoip_region", PREFIX + ".region.code", "STRING", Casts.STRING_ONLY, FORMAT_NO_SPACE_STRING));

        // $geoip_region_name
        // country region name (region, territory, state, province, federal land and the like), for example, “Moscow City”, “District of Columbia”.
        parsers.add(new TokenParser("$geoip_region_name", PREFIX + ".region.name", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_city
        // city name, for example, “Moscow”, “Washington”.
        parsers.add(new TokenParser("$geoip_city", PREFIX + ".city", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_postal_code
        // postal code.
        parsers.add(new TokenParser("$geoip_postal_code", PREFIX + ".postal.code", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        // $geoip_org
        // organization name, for example, “The University of Melbourne”.
        // TODO: Is is unclear is this is the ISP or the ASP organization
        parsers.add(new TokenParser("$geoip_org", PREFIX + ".organization", "STRING", Casts.STRING_ONLY, FORMAT_STRING));

        return parsers;
    }
}
