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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPASNDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCityDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCountryDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPISPDissector;
import org.junit.Test;

public class TestGeoIPDissectors {

    private static final String TEST_MMDB_BASE_DIR = "../../GeoIP2-TestData/test-data/";
    private static final String ASN_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoLite2-ASN-Test.mmdb";
    private static final String ISP_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-ISP-Test.mmdb";
    private static final String CITY_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-City-Test.mmdb";
    private static final String COUNTRY_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-Country-Test.mmdb";

    // Tests with IPv4
    @Test
    public void testGeoIPASN() {
        GeoIPASNDissector dissector = new GeoIPASNDissector();
        dissector.initializeFromSettingsParameter(ASN_TEST_MMDB);

        DissectorTester.create()
            .withInput("80.100.47.45")
            .withDissector(dissector)
            .expect("ASN:asn.number",               "4444")
            .expect("ASN:asn.number",               4444L)
            .expect("STRING:asn.organization",      "Basjes Global Network")
            .checkExpectations();
    }

    @Test
    public void testGeoIPISP() {
        GeoIPISPDissector dissector = new GeoIPISPDissector();
        dissector.initializeFromSettingsParameter(ISP_TEST_MMDB);

        DissectorTester.create()
            .withInput("80.100.47.45")
            .withDissector(dissector)
            .expect("ASN:asn.number",               "4444")
            .expect("ASN:asn.number",               4444L)
            .expect("STRING:asn.organization",      "Basjes Global Network")
            .expect("STRING:isp.name",              "Basjes ISP")
            .expect("STRING:isp.organization",      "Niels Basjes")
            .checkExpectations();
    }

    @Test
    public void testGeoIPCountry() {
        GeoIPCountryDissector dissector = new GeoIPCountryDissector();
        dissector.initializeFromSettingsParameter(COUNTRY_TEST_MMDB);

        DissectorTester.create()
            .withInput("80.100.47.45")
            .withDissector(dissector)
            .expect("STRING:continent.name",        "Europe")
            .expect("STRING:continent.code",        "EU")
            .expect("STRING:country.name",          "Netherlands")
            .expect("STRING:country.iso",           "NL")
            .checkExpectations();
    }

    @Test
    public void testGeoIPCity() {
        GeoIPCityDissector dissector = new GeoIPCityDissector();
        dissector.initializeFromSettingsParameter(CITY_TEST_MMDB);

        DissectorTester.create()
            .withInput("80.100.47.45")
            .withDissector(dissector)
            .expect("STRING:continent.name",        "Europe")
            .expect("STRING:continent.code",        "EU")
            .expect("STRING:country.name",          "Netherlands")
            .expect("STRING:country.iso",           "NL")
            .expect("STRING:city.name",             "Amstelveen")
            .expect("STRING:postal.code",           "1187")
            .expect("STRING:location.latitude",     "52.5")
            .expect("STRING:location.latitude",     52.5)
            .expect("STRING:location.longitude",    "5.75")
            .expect("STRING:location.longitude",    5.75)
            .checkExpectations();
    }

    // =================================================================================================================
    // Tests with IPv6

    @Test
    public void testGeoIPASNIpv6() {
        GeoIPASNDissector dissector = new GeoIPASNDissector();
        dissector.initializeFromSettingsParameter(ASN_TEST_MMDB);

        DissectorTester.create()
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .withDissector(dissector)
            .expect("ASN:asn.number",               "6666")
            .expect("ASN:asn.number",               6666L)
            .expect("STRING:asn.organization",      "Basjes Global Network IPv6")
            .checkExpectations();
    }

    @Test
    public void testGeoIPISPIpv6() {
        GeoIPISPDissector dissector = new GeoIPISPDissector();
        dissector.initializeFromSettingsParameter(ISP_TEST_MMDB);

        DissectorTester.create()
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .withDissector(dissector)
            .expect("ASN:asn.number",               "6666")
            .expect("ASN:asn.number",               6666L)
            .expect("STRING:asn.organization",      "Basjes Global Network IPv6")
            .expect("STRING:isp.name",              "Basjes ISP IPv6")
            .expect("STRING:isp.organization",      "Niels Basjes IPv6")
            .checkExpectations();
    }

    @Test
    public void testGeoIPCountryIpv6() {
        GeoIPCountryDissector dissector = new GeoIPCountryDissector();
        dissector.initializeFromSettingsParameter(COUNTRY_TEST_MMDB);

        DissectorTester.create()
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .withDissector(dissector)
            .expect("STRING:continent.name",        "Europe")
            .expect("STRING:continent.code",        "EU")
            .expect("STRING:country.name",          "Netherlands")
            .expect("STRING:country.iso",           "NL")
            .checkExpectations();
    }

    @Test
    public void testGeoIPCityIpv6() {
        GeoIPCityDissector dissector = new GeoIPCityDissector();
        dissector.initializeFromSettingsParameter(CITY_TEST_MMDB);

        DissectorTester.create()
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .withDissector(dissector)
            .expect("STRING:continent.name",        "Europe")
            .expect("STRING:continent.code",        "EU")
            .expect("STRING:country.name",          "Netherlands")
            .expect("STRING:country.iso",           "NL")
            .expect("STRING:city.name",             "Amstelveen")
            .expect("STRING:postal.code",           "1187")
            .expect("STRING:location.latitude",     "52.5")
            .expect("STRING:location.latitude",     52.5)
            .expect("STRING:location.longitude",    "5.75")
            .expect("STRING:location.longitude",    5.75)
            .checkExpectations();
    }

    // =================================================================================================================
    // Tests with localhost ... which is NOT in the database

    @Test
    public void testGeoIPISPLocalhost() {
        GeoIPISPDissector dissector = new GeoIPISPDissector();
        dissector.initializeFromSettingsParameter(ISP_TEST_MMDB);

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("ASN:asn.number")
            .expectAbsentLong("ASN:asn.number")
            .expectAbsentString("STRING:asn.organization")
            .expectAbsentString("STRING:isp.name")
            .expectAbsentString("STRING:isp.organization")
            .checkExpectations();
    }

    @Test
    public void testGeoIPASNLocalhost() {
        GeoIPASNDissector dissector = new GeoIPASNDissector();
        dissector.initializeFromSettingsParameter(ASN_TEST_MMDB);

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("ASN:asn.number")
            .expectAbsentLong("ASN:asn.number")
            .expectAbsentString("STRING:asn.organization")
            .checkExpectations();
    }

    @Test
    public void testGeoIPCountryLocalhost() {
        GeoIPCountryDissector dissector = new GeoIPCountryDissector();
        dissector.initializeFromSettingsParameter(COUNTRY_TEST_MMDB);

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("STRING:continent.name")
            .expectAbsentString("STRING:continent.code")
            .expectAbsentString("STRING:country.name")
            .expectAbsentString("STRING:country.iso")
            .checkExpectations();
    }

    @Test
    public void testGeoIPCityLocalhost() {
        GeoIPCityDissector dissector = new GeoIPCityDissector();
        dissector.initializeFromSettingsParameter(CITY_TEST_MMDB);

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("STRING:continent.name")
            .expectAbsentString("STRING:continent.code")
            .expectAbsentString("STRING:country.name")
            .expectAbsentString("STRING:country.iso")
            .expectAbsentString("STRING:city.name")
            .expectAbsentString("STRING:postal.code")
            .expectAbsentString("STRING:location.latitude")
            .expectAbsentString("STRING:location.latitude")
            .expectAbsentString("STRING:location.longitude")
            .expectAbsentString("STRING:location.longitude")
            .checkExpectations();
    }


}
