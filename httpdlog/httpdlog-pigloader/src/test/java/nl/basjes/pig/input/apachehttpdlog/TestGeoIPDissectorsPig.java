/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
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

package nl.basjes.pig.input.apachehttpdlog;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.junit.Assert.assertEquals;

public class TestGeoIPDissectorsPig {

    private static final String LOGFORMAT = "%h";
    private final String logfile = getClass().getResource("/geoip.log").toString();
    private static final String TEST_MMDB_BASE_DIR = "../../GeoIP2-TestData/test-data/";
    private static final String ISP_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-ISP-Test.mmdb";
    private static final String ASN_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoLite2-ASN-Test.mmdb";
    private static final String CITY_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-City-Test.mmdb";
    private static final String COUNTRY_TEST_MMDB = TEST_MMDB_BASE_DIR + "GeoIP2-Country-Test.mmdb";

    @Test
    public void testGeoIPCountryASNDissectorPig() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks = " +
            "       LOAD '" + logfile + "' " +
            "       USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "               '" + LOGFORMAT + "'," +
            "               'IP:connection.client.host'," +

            "       '-load:nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCountryDissector:" + COUNTRY_TEST_MMDB + "'," +
            "               'STRING:connection.client.host.continent.name'," +
            "               'STRING:connection.client.host.continent.code'," +
            "               'STRING:connection.client.host.country.name'," +
            "               'STRING:connection.client.host.country.iso'," +

            "       '-load:nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPASNDissector:"+ASN_TEST_MMDB+"'," +
            "               'ASN:connection.client.host.asn.number'," +
            "               'STRING:connection.client.host.asn.organization'" +

            "          )" +
            "       AS (" +
            "               connection_client_host:chararray," +
            "               connection_client_host_continent_name:chararray," +
            "               connection_client_host_continent_code:chararray," +
            "               connection_client_host_country_name:chararray," +
            "               connection_client_host_country_iso:chararray," +

            "               connection_client_host_asn_number:long," +
            "               connection_client_host_asn_organization:chararray" +
            "          );"
        );
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(1, out.size());

        Tuple result;

        result = out.get(0);
        assertEquals("80.100.47.45",            result.get(0));
        assertEquals("Europe",                  result.get(1));
        assertEquals("EU",                      result.get(2));
        assertEquals("Netherlands",             result.get(3));
        assertEquals("NL",                      result.get(4));
        assertEquals("4444",                    result.get(5).toString());
        assertEquals("Basjes Global Network",   result.get(6));
    }

    @Test
    public void testGeoIPCityISPDissectorPig() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks = " +
                "       LOAD '" + logfile + "' " +
                "       USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
                "               '" + LOGFORMAT + "'," +
                "               'IP:connection.client.host'," +
                "       '-load:nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCityDissector:"+CITY_TEST_MMDB+"'," +
                "               'STRING:connection.client.host.continent.name'," +
                "               'STRING:connection.client.host.continent.code'," +
                "               'STRING:connection.client.host.country.name'," +
                "               'STRING:connection.client.host.country.iso'," +
                "               'STRING:connection.client.host.subdivision.name'," +
                "               'STRING:connection.client.host.subdivision.iso'," +
                "               'STRING:connection.client.host.city.name'," +
                "               'STRING:connection.client.host.postal.code'," +
                "               'STRING:connection.client.host.location.latitude'," +
                "               'STRING:connection.client.host.location.longitude'," +

                "       '-load:nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPISPDissector:"+ISP_TEST_MMDB+"'," +
                "               'ASN:connection.client.host.asn.number'," +
                "               'STRING:connection.client.host.asn.organization'," +
                "               'STRING:connection.client.host.isp.name'," +
                "               'STRING:connection.client.host.isp.organization'" +
                "          )" +
                "       AS (" +
                "               connection_client_host:chararray," +

                "               connection_client_host_continent_name:chararray," +
                "               connection_client_host_continent_code:chararray," +
                "               connection_client_host_country_name:chararray," +
                "               connection_client_host_country_iso:chararray," +
                "               connection_client_host_subdivision_name:chararray," +
                "               connection_client_host_subdivision_iso:chararray," +
                "               connection_client_host_city_name:chararray," +
                "               connection_client_host_postal_code:chararray," +
                "               connection_client_host_location_latitude:double," +
                "               connection_client_host_location_longitude:double," +

                "               connection_client_host_asn_number:long," +
                "               connection_client_host_asn_organization:chararray," +
                "               connection_client_host_isp_name:chararray," +
                "               connection_client_host_isp_organization:chararray" +
                "          );"
        );
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(1, out.size());

        Tuple result;

        result = out.get(0);
        assertEquals("80.100.47.45",            result.get(0));
        assertEquals("Europe",                  result.get(1));
        assertEquals("EU",                      result.get(2));
        assertEquals("Netherlands",             result.get(3));
        assertEquals("NL",                      result.get(4));
        assertEquals("Noord Holland",           result.get(5));
        assertEquals("NH",                      result.get(6));
        assertEquals("Amstelveen",              result.get(7));
        assertEquals("1187",                    result.get(8));
        assertEquals("52.5",                    result.get(9).toString());
        assertEquals("5.75",                    result.get(10).toString());
        assertEquals("4444",                    result.get(11).toString());
        assertEquals("Basjes Global Network",   result.get(12));
        assertEquals("Basjes ISP",              result.get(13));
        assertEquals("Niels Basjes",            result.get(14));
    }

}
