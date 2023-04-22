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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPASNDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCityDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCountryDissector;
import nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPISPDissector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestGeoIPDissectors {

    private static final String TEST_MMDB_BASE_DIR = "../../GeoIP2-TestData/test-data/";
    private static final String ASN_TEST_MMDB     = TEST_MMDB_BASE_DIR + "GeoLite2-ASN-Test.mmdb";
    private static final String ISP_TEST_MMDB     = TEST_MMDB_BASE_DIR + "GeoIP2-ISP-Test.mmdb";
    private static final String CITY_TEST_MMDB    = TEST_MMDB_BASE_DIR + "GeoIP2-City-Test.mmdb";
    private static final String COUNTRY_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-Country-Test.mmdb";

    DissectorTester createTester(Dissector dissector) {
        return DissectorTester.create()
            .withDissector(dissector)
            .withPathPrefix("");
    }

    public static class TestGeoIPDissectorsWithPrefix extends TestGeoIPDissectors {
        // We run the SAME tests again but now wrapped in a parser that does things with a prefix.
        DissectorTester createTester(Dissector dissector) {
            return DissectorTester.create()
                .withParser(new HttpdLoglineParser<>(TestRecord.class, "%h"))
                .withDissector(dissector)
                .withPathPrefix("connection.client.host.");
        }
    }

    // =================================================================================================================
    // No such file
    @Test
    void testBadFileASN() {
        AssertionError assertionError = assertThrows(AssertionError.class, ()->
            createTester(new GeoIPASNDissector("Does not exist"))
                .withInput("80.100.47.45")
                .expect("ASN:asn.number",         "4444")
                .checkExpectations());
        assertTrue(assertionError.getMessage().contains("Does not exist (No such file or directory)"));
    }

    @Test
    void testBadFileISP() {
        AssertionError assertionError = assertThrows(AssertionError.class, () ->
            createTester(new GeoIPISPDissector("Does not exist"))
                .withInput("80.100.47.45")
                .expect("ASN:asn.number", "4444")
                .checkExpectations());
        assertTrue(assertionError.getMessage().contains("Does not exist (No such file or directory)"));
    }

    @Test
    void testBadFileCity() {
        AssertionError assertionError = assertThrows(AssertionError.class, () ->
            createTester(new GeoIPCityDissector("Does not exist"))
                .withInput("80.100.47.45")
                .expect("STRING:continent.name", "Europe")
                .checkExpectations());
        assertTrue(assertionError.getMessage().contains("Does not exist (No such file or directory)"));
    }

    @Test
    void testBadFileCountry() {
        AssertionError assertionError = assertThrows(AssertionError.class, () ->
            createTester(new GeoIPCountryDissector("Does not exist"))
                .withInput("80.100.47.45")
                .expect("STRING:continent.name", "Europe")
                .checkExpectations());
        assertTrue(assertionError.getMessage().contains("Does not exist (No such file or directory)"));
    }

    // =================================================================================================================
    // IP not in index
    @Test
    void testUnknownIPASN() {
        createTester(new GeoIPASNDissector(ASN_TEST_MMDB))
            .withInput("1.2.3.4")
            .expectAbsentString("ASN:asn.number")
            .checkExpectations();
    }

    @Test
    void testUnknownIPISP() {
        createTester(new GeoIPISPDissector(ISP_TEST_MMDB))
            .withInput("1.2.3.4")
            .expectAbsentString("ASN:asn.number")
            .checkExpectations();
    }

    @Test
    void testUnknownIPCity() {
        createTester(new GeoIPCityDissector(CITY_TEST_MMDB))
            .withInput("1.2.3.4")
            .expectAbsentString("STRING:continent.name")
            .checkExpectations();
    }

    @Test
    void testUnknownIPCountry() {
        createTester(new GeoIPCountryDissector(COUNTRY_TEST_MMDB))
            .withInput("1.2.3.4")
            .expectAbsentString("STRING:continent.name")
            .checkExpectations();
    }

    // =================================================================================================================

    // Tests with IPv4
    @Test
    void testGeoIPASN() {
        createTester(new GeoIPASNDissector(ASN_TEST_MMDB))
            .withInput("80.100.47.45")
            .expect("ASN:asn.number",               "4444")
            .expect("ASN:asn.number",               4444L)
            .expect("STRING:asn.organization",      "Basjes Global Network")
            .checkExpectations();
    }

    @Test
    void testGeoIPISP() {
        createTester(new GeoIPISPDissector(ISP_TEST_MMDB))
            .withInput("80.100.47.45")
            .expect("ASN:asn.number",               "4444")
            .expect("ASN:asn.number",               4444L)
            .expect("STRING:asn.organization",      "Basjes Global Network")
            .expect("STRING:isp.name",              "Basjes ISP")
            .expect("STRING:isp.organization",      "Niels Basjes")
            .checkExpectations();
    }

    @Test
    void testGeoIPCountry() {
        createTester(new GeoIPCountryDissector(COUNTRY_TEST_MMDB))
            .withInput("80.100.47.45")
            .expect("STRING:continent.name",                "Europe")
            .expect("STRING:continent.code",                "EU")
            .expect("STRING:country.name",                  "Netherlands")
            .expect("STRING:country.iso",                   "NL")
            .expect("NUMBER:country.getconfidence",         "42")
            .expect("NUMBER:country.getconfidence",         42L)
            .expect("BOOLEAN:country.isineuropeanunion",    "1")
            .expect("BOOLEAN:country.isineuropeanunion",    1L)
            .checkExpectations();
    }

    @Test
    void testGeoIPCity() {
        createTester(new GeoIPCityDissector(CITY_TEST_MMDB))
            .withInput("80.100.47.45")
            .expect("STRING:continent.name",                "Europe")
            .expect("STRING:continent.code",                "EU")

            .expect("STRING:country.name",                  "Netherlands")
            .expect("STRING:country.iso",                   "NL")
            .expect("NUMBER:country.getconfidence",         "42")
            .expect("NUMBER:country.getconfidence",         42L)
            .expect("BOOLEAN:country.isineuropeanunion",    "1")
            .expect("BOOLEAN:country.isineuropeanunion",    1L)

            .expect("STRING:subdivision.name",              "Noord Holland")
            .expect("STRING:subdivision.iso",               "NH")

            .expect("STRING:city.name",                     "Amstelveen")
            .expect("NUMBER:city.confidence",               1L)
            .expect("NUMBER:city.geonameid",                1234L)

            .expect("STRING:postal.code",                   "1187")
            .expect("NUMBER:postal.confidence",             2L)

            .expect("STRING:location.latitude",             "52.5")
            .expect("STRING:location.latitude",             52.5)
            .expect("STRING:location.longitude",            "5.75")
            .expect("STRING:location.longitude",            5.75)
            .expect("NUMBER:location.accuracyradius",       4L)
            .expect("NUMBER:location.metrocode",            5L)

            .checkExpectations();
    }

    // =================================================================================================================
    // Tests with IPv6

    @Test
    void testGeoIPASNIpv6() {
        createTester(new GeoIPASNDissector(ASN_TEST_MMDB))
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .expect("ASN:asn.number",               "6666")
            .expect("ASN:asn.number",               6666L)
            .expect("STRING:asn.organization",      "Basjes Global Network IPv6")
            .checkExpectations();
    }

    @Test
    void testGeoIPISPIpv6() {
        createTester(new GeoIPISPDissector(ISP_TEST_MMDB))
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .expect("ASN:asn.number",               "6666")
            .expect("ASN:asn.number",               6666L)
            .expect("STRING:asn.organization",      "Basjes Global Network IPv6")
            .expect("STRING:isp.name",              "Basjes ISP IPv6")
            .expect("STRING:isp.organization",      "Niels Basjes IPv6")
            .checkExpectations();
    }

    @Test
    void testGeoIPCountryIpv6() {
        createTester(new GeoIPCountryDissector(COUNTRY_TEST_MMDB))
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .expect("STRING:continent.name",                    "Europe")
            .expect("STRING:continent.code",                    "EU")
            .expect("STRING:country.name",                      "Netherlands")
            .expect("STRING:country.iso",                       "NL")
            .expect("NUMBER:country.getconfidence",             "42")
            .expect("NUMBER:country.getconfidence",             42L)
            .expect("BOOLEAN:country.isineuropeanunion",        1L)
            .expect("BOOLEAN:country.isineuropeanunion",        "1")
            .checkExpectations();
    }

    @Test
    void testGeoIPCityIpv6() {
        createTester(new GeoIPCityDissector(CITY_TEST_MMDB))
            .withInput("2001:980:91c0:1:21c:c0ff:fe06:e580")
            .expect("STRING:continent.name",                    "Europe")
            .expect("STRING:continent.code",                    "EU")

            .expect("STRING:country.name",                      "Netherlands")
            .expect("STRING:country.iso",                       "NL")
            .expect("NUMBER:country.getconfidence",             "42")
            .expect("NUMBER:country.getconfidence",             42L)
            .expect("BOOLEAN:country.isineuropeanunion",        "1")
            .expect("BOOLEAN:country.isineuropeanunion",        1L)

            .expect("STRING:subdivision.name",                  "Noord Holland")
            .expect("STRING:subdivision.iso",                   "NH")

            .expect("STRING:city.name",                         "Amstelveen")
            .expect("NUMBER:city.confidence",                   11L)
            .expect("NUMBER:city.geonameid",                    1234L)

            .expect("STRING:postal.code",                       "1187")
            .expect("NUMBER:postal.confidence",                 12L)

            .expect("STRING:location.latitude",                 "52.5")
            .expect("STRING:location.latitude",                 52.5)
            .expect("STRING:location.longitude",                "5.75")
            .expect("STRING:location.longitude",                5.75)
            .expect("STRING:location.timezone",                 "Europe/Amsterdam")
            .expect("NUMBER:location.accuracyradius",           14L)
            .expect("NUMBER:location.metrocode",                15L)
            .checkExpectations();
    }

    // =================================================================================================================
    // Tests with localhost ... which is NOT in the database

    @Test
    void testGeoIPISPLocalhost() {
        createTester(new GeoIPISPDissector(ISP_TEST_MMDB))
            .withInput("127.0.0.1")
            .expectAbsentString("ASN:asn.number")
            .expectAbsentLong("ASN:asn.number")
            .expectAbsentString("STRING:asn.organization")
            .expectAbsentString("STRING:isp.name")
            .expectAbsentString("STRING:isp.organization")
            .checkExpectations();
    }

    @Test
    void testGeoIPASNLocalhost() {
        createTester(new GeoIPASNDissector(ASN_TEST_MMDB))
            .withInput("127.0.0.1")
            .expectAbsentString("ASN:asn.number")
            .expectAbsentLong("ASN:asn.number")
            .expectAbsentString("STRING:asn.organization")
            .checkExpectations();
    }

    @Test
    void testGeoIPCountryLocalhost() {
        createTester(new GeoIPCountryDissector(COUNTRY_TEST_MMDB))
            .withInput("127.0.0.1")
            .expectAbsentString("STRING:continent.name")
            .expectAbsentString("STRING:continent.code")
            .expectAbsentString("STRING:country.name")
            .expectAbsentString("STRING:country.iso")
            .expectAbsentString("NUMBER:country.getconfidence")
            .expectAbsentLong("NUMBER:country.getconfidence")
            .expectAbsentString("BOOLEAN:country.isineuropeanunion")
            .expectAbsentLong("BOOLEAN:country.isineuropeanunion")
            .checkExpectations();
    }

    @Test
    void testGeoIPCityLocalhost() {
        createTester(new GeoIPCityDissector(CITY_TEST_MMDB))
            .withInput("127.0.0.1")
            .expectAbsentString("STRING:continent.name")
            .expectAbsentString("STRING:continent.code")
            .expectAbsentString("STRING:country.name")
            .expectAbsentString("STRING:country.iso")
            .expectAbsentString("NUMBER:country.getconfidence")
            .expectAbsentLong("NUMBER:country.getconfidence")
            .expectAbsentString("BOOLEAN:country.isineuropeanunion")
            .expectAbsentLong("BOOLEAN:country.isineuropeanunion")
            .expectAbsentString("STRING:city.name")
            .expectAbsentString("STRING:postal.code")
            .expectAbsentString("STRING:location.latitude")
            .expectAbsentDouble("STRING:location.latitude")
            .expectAbsentString("STRING:location.longitude")
            .expectAbsentDouble("STRING:location.longitude")
            .checkExpectations();
    }

}
