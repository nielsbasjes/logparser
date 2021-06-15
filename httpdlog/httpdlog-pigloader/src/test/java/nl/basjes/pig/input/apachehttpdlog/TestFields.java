/*
 * Apache HTTPD & NGINX Access log parsing made easy
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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestFields {
    private static final Logger LOG = LoggerFactory.getLogger(TestFields.class);

    private static final String LOGFORMAT = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";

    @Test
    void fieldsTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
                "Fields = " +
                        "    LOAD '" + getClass().getResource("/access.log") + "' " +
                        "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
                        "          '" + LOGFORMAT + "', " +
                        "          'Fields'," +
                        "          '-map:request.firstline.uri.query.g:HTTP.URI'," +
                        "          '-map:request.firstline.uri.query.r:HTTP.URI'," +
                        "          '-map:request.firstline.uri.query.s:SCREENRESOLUTION'," +
                        "          '-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'" +
                        "           ) AS ( " +
                        "          Fields );"
        );

        pigServer.registerQuery("STORE Fields INTO 'Fields' USING mock.Storage();");

        List<Tuple> out = data.get("Fields");

        // Check the basics
        assertTrue(out.contains(tuple("HTTP.URI:request.firstline.uri")), "Missing Base URI");
        assertTrue(out.contains(tuple("HTTP.QUERYSTRING:request.firstline.uri.query")), "Missing Base QueryString");
        assertTrue(out.contains(tuple("STRING:request.firstline.uri.query.*")), "Missing Base Query Parameters");

        // Check the the remapped possibilities
        assertTrue(out.contains(tuple("HTTP.URI:request.firstline.uri.query.g")), "Missing Remapped URI G");
        assertTrue(out.contains(tuple("HTTP.QUERYSTRING:request.firstline.uri.query.g.query")), "Missing Remapped URI G QueryString");
        assertTrue(out.contains(tuple("STRING:request.firstline.uri.query.g.query.*")), "Missing Remapped URI G Query Parameters");
        assertTrue(out.contains(tuple("HTTP.URI:request.firstline.uri.query.r")), "Missing Remapped URI R");
        assertTrue(out.contains(tuple("HTTP.QUERYSTRING:request.firstline.uri.query.r.query")), "Missing Remapped URI R QueryString");
        assertTrue(out.contains(tuple("STRING:request.firstline.uri.query.r.query.*")), "Missing Remapped URI R Query Parameters");

        // Check the the remapped possibilities from the additional dissector
        assertTrue(out.contains(tuple("SCREENRESOLUTION:request.firstline.uri.query.s")), "Missing Remapped Extraloaded SCREENRESOLUTION");
        assertTrue(out.contains(tuple("SCREENWIDTH:request.firstline.uri.query.s.width")), "Missing Remapped Extraloaded Width");
        assertTrue(out.contains(tuple("SCREENHEIGHT:request.firstline.uri.query.s.height")), "Missing Remapped Extraloaded Height");
    }

    @Test
    void fieldsExampleTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Example = " +
            "    LOAD '" + getClass().getResource("/access.log") + "' " +
            "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "          '" + LOGFORMAT + "', " +
            "          'Example'," +
            "          '-map:request.firstline.uri.query.g:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.r:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.s:SCREENRESOLUTION'," +
            "          '-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'" +
            "           ) AS ( " +
            "          Example );"
        );

        pigServer.registerQuery("STORE Example INTO 'Example' USING mock.Storage();");

        validateExampleResult(data.get("Example"));
    }

    @Test
    void fieldsFullBareExampleTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Example = " +
            "    LOAD '" + getClass().getResource("/access.log") + "' " +
            "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "          '" + LOGFORMAT + "', " +
            "          '-map:request.firstline.uri.query.g:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.r:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.s:SCREENRESOLUTION'," +
            "          '-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'" +
            "           );"
        );

        pigServer.registerQuery("STORE Example INTO 'Example' USING mock.Storage();");

        validateExampleResult(data.get("Example"));
    }

    private void validateExampleResult(List<Tuple> out) throws Exception {
        assertEquals(1, out.size());
        assertEquals(1, out.get(0).size());

        String theValue = out.get(0).get(0).toString();

        LOG.info("Result is {}", theValue);
        assertTrue(theValue.contains("nl.basjes.pig.input.apachehttpdlog.Loader"));

        assertTrue(!theValue.contains("''"), "Empty field");
        assertTrue(theValue.contains("HTTP.URI:request.firstline.uri"), "Missing Base URI");
        assertTrue(theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query"), "Missing Base QueryString");
        assertTrue(theValue.contains("STRING:request.firstline.uri.query.*"), "Missing Base Query Parameters");

        // Check the special parameters
        assertTrue(theValue.contains("'-map:request.firstline.uri.query.s:SCREENRESOLUTION'"), "Missing MAP parameter");
        assertTrue(theValue.contains("'-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'"), "Missing LOAD parameter");

        // Check the remapped possibilities
        assertTrue(theValue.contains("HTTP.URI:request.firstline.uri.query.g"), "Missing Remapped URI G");
        assertTrue(theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query.g.query"), "Missing Remapped URI G QueryString");
        assertTrue(theValue.contains("STRING:request.firstline.uri.query.g.query.*"), "Missing Remapped URI G Query Parameters");
        assertTrue(theValue.contains("HTTP.URI:request.firstline.uri.query.r"), "Missing Remapped URI R");
        assertTrue(theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query.r.query"), "Missing Remapped URI R QueryString");
        assertTrue(theValue.contains("STRING:request.firstline.uri.query.r.query.*"), "Missing Remapped URI R Query Parameters");

        // Casts of values
        assertTrue(theValue.contains("request_firstline_uri:chararray"), "Missing Casts Base URI");
        assertTrue(theValue.contains("request_firstline_uri_query:chararray"), "Missing Casts Base QueryString");
        assertTrue(theValue.contains("request_firstline_uri_query__:map[]"), "Missing Casts Base Query Parameters");

        // Check the Casts of remapped possibilities
        assertTrue(theValue.contains("request_firstline_uri_query_g:chararray"), "Missing Casts Remapped URI G");
        assertTrue(theValue.contains("request_firstline_uri_query_g_query:chararray"), "Missing Casts Remapped URI G QueryString");
        assertTrue(theValue.contains("request_firstline_uri_query_g_query__:map[]"), "Missing Casts Remapped URI G Query Parameters");
        assertTrue(theValue.contains("request_firstline_uri_query_r:chararray"), "Missing Casts Remapped URI R");
        assertTrue(theValue.contains("request_firstline_uri_query_r_query:chararray"), "Missing Casts Remapped URI R QueryString");
        assertTrue(theValue.contains("request_firstline_uri_query_r_query__:map[]"), "Missing Casts Remapped URI R Query Parameters");
    }

    @Test
    void fieldsBareExampleTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Example = " +
                "    LOAD '" + getClass().getResource("/access.log") + "' " +
                "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
                "          '" + LOGFORMAT + "'" +
                "    );"
        );

        pigServer.registerQuery("STORE Example INTO 'Example' USING mock.Storage();");

        List<Tuple> out = data.get("Example");

        // Check the basics
        assertEquals(1, out.size());
        assertEquals(1, out.get(0).size());

        String theValue = out.get(0).get(0).toString();

        LOG.info("Result is {}", theValue);
        assertTrue(theValue.contains("nl.basjes.pig.input.apachehttpdlog.Loader"));

        assertTrue(!theValue.contains("''"), "Empty field");

        assertTrue(theValue.contains("HTTP.URI:request.firstline.uri"), "Missing Base URI");
        assertTrue(theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query"), "Missing Base QueryString");
        assertTrue(theValue.contains("STRING:request.firstline.uri.query.*"), "Missing Base Query Parameters");

        // Casts of values
        assertTrue(theValue.contains("request_firstline_uri:chararray"), "Missing Casts Base URI");
        assertTrue(theValue.contains("request_firstline_uri_query:chararray"), "Missing Casts Base QueryString");
        assertTrue(theValue.contains("request_firstline_uri_query__:map[]"), "Missing Casts Base Query Parameters");
    }

}
