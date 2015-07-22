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

package nl.basjes.pig.input.apachehttpdlog;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFields {
    private static final Logger LOG = LoggerFactory.getLogger(TestFields.class);

    private static final String LOGFORMAT = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";

    @Test
    public void fieldsTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
                "Fields = " +
                        "    LOAD '" + getClass().getResource("/access.log").toString() + "' " +
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
        assertTrue("Missing Base URI", out.contains(tuple("HTTP.URI:request.firstline.uri")));
        assertTrue("Missing Base QueryString", out.contains(tuple("HTTP.QUERYSTRING:request.firstline.uri.query")));
        assertTrue("Missing Base Query Parameters", out.contains(tuple("STRING:request.firstline.uri.query.*")));

        // Check the the remapped possibilities
        assertTrue("Missing Remapped URI G", out.contains(tuple("HTTP.URI:request.firstline.uri.query.g")));
        assertTrue("Missing Remapped URI G QueryString", out.contains(tuple("HTTP.QUERYSTRING:request.firstline.uri.query.g.query")));
        assertTrue("Missing Remapped URI G Query Parameters", out.contains(tuple("STRING:request.firstline.uri.query.g.query.*")));
        assertTrue("Missing Remapped URI R", out.contains(tuple("HTTP.URI:request.firstline.uri.query.r")));
        assertTrue("Missing Remapped URI R QueryString", out.contains(tuple("HTTP.QUERYSTRING:request.firstline.uri.query.r.query")));
        assertTrue("Missing Remapped URI R Query Parameters", out.contains(tuple("STRING:request.firstline.uri.query.r.query.*")));

        // Check the the remapped possibilities from the additional dissector
        assertTrue("Missing Remapped Extraloaded SCREENRESOLUTION", out.contains(tuple("SCREENRESOLUTION:request.firstline.uri.query.s")));
        assertTrue("Missing Remapped Extraloaded Width", out.contains(tuple("SCREENWIDTH:request.firstline.uri.query.s.width")));
        assertTrue("Missing Remapped Extraloaded Height", out.contains(tuple("SCREENHEIGHT:request.firstline.uri.query.s.height")));
    }

    @Test
    public void fieldsExampleTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Fields = " +
            "    LOAD '" + getClass().getResource("/access.log").toString() + "' " +
            "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "          '" + LOGFORMAT + "', " +
            "          'Example'," +
            "          '-map:request.firstline.uri.query.g:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.r:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.s:SCREENRESOLUTION'," +
            "          '-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'" +
            "           ) AS ( " +
            "          Fields );"
        );

        pigServer.registerQuery("STORE Fields INTO 'Fields' USING mock.Storage();");

        validateExampleResult(data.get("Fields"));
    }

    @Test
    public void fieldsBareExampleTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Fields = " +
            "    LOAD '" + getClass().getResource("/access.log").toString() + "' " +
            "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "          '" + LOGFORMAT + "', " +
            "          '-map:request.firstline.uri.query.g:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.r:HTTP.URI'," +
            "          '-map:request.firstline.uri.query.s:SCREENRESOLUTION'," +
            "          '-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'" +
            "           );"
        );

        pigServer.registerQuery("STORE Fields INTO 'Fields' USING mock.Storage();");

        validateExampleResult(data.get("Fields"));
    }

    private void validateExampleResult(List<Tuple> out) throws Exception {
        assertEquals(1, out.size());
        assertEquals(1, out.get(0).size());

        String theValue = out.get(0).get(0).toString();

        LOG.info("Result is {}", theValue);
        assertTrue(theValue.contains("nl.basjes.pig.input.apachehttpdlog.Loader"));

        assertTrue("Missing Base URI", theValue.contains("HTTP.URI:request.firstline.uri"));
        assertTrue("Missing Base QueryString", theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query"));
        assertTrue("Missing Base Query Parameters", theValue.contains("STRING:request.firstline.uri.query.*"));

        // Check the special parameters
        assertTrue("Missing MAP parameter", theValue.contains("'-map:request.firstline.uri.query.s:SCREENRESOLUTION'"));
        assertTrue("Missing LOAD parameter", theValue.contains("'-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'"));

        // Check the remapped possibilities
        assertTrue("Missing Remapped URI G", theValue.contains("HTTP.URI:request.firstline.uri.query.g"));
        assertTrue("Missing Remapped URI G QueryString", theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query.g.query"));
        assertTrue("Missing Remapped URI G Query Parameters", theValue.contains("STRING:request.firstline.uri.query.g.query.*"));
        assertTrue("Missing Remapped URI R", theValue.contains("HTTP.URI:request.firstline.uri.query.r"));
        assertTrue("Missing Remapped URI R QueryString", theValue.contains("HTTP.QUERYSTRING:request.firstline.uri.query.r.query"));
        assertTrue("Missing Remapped URI R Query Parameters", theValue.contains("STRING:request.firstline.uri.query.r.query.*"));

        // Casts of values
        assertTrue("Missing Casts Base URI", theValue.contains("request_firstline_uri:chararray"));
        assertTrue("Missing Casts Base QueryString", theValue.contains("request_firstline_uri_query:chararray"));
//        assertTrue("Missing Casts Base Query Parameters", theValue.contains("request_firstline_uri_query_*:chararray"));
        assertTrue("Missing Casts Base Query Parameters", theValue.contains("request_firstline_uri_query__:map[]"));

        // Check the Casts of remapped possibilities
        assertTrue("Missing Casts Remapped URI G", theValue.contains("request_firstline_uri_query_g:chararray"));
        assertTrue("Missing Casts Remapped URI G QueryString", theValue.contains("request_firstline_uri_query_g_query:chararray"));
//        assertTrue("Missing Casts Remapped URI G Query Parameters", theValue.contains("request_firstline_uri_query_g_query_*:chararray"));
        assertTrue("Missing Casts Remapped URI G Query Parameters", theValue.contains("request_firstline_uri_query_g_query__:map[]"));
        assertTrue("Missing Casts Remapped URI R", theValue.contains("request_firstline_uri_query_r:chararray"));
        assertTrue("Missing Casts Remapped URI R QueryString", theValue.contains("request_firstline_uri_query_r_query:chararray"));
//        assertTrue("Missing Casts Remapped URI R Query Parameters", theValue.contains("request_firstline_uri_query_r_query_*:chararray"));
        assertTrue("Missing Casts Remapped URI R Query Parameters", theValue.contains("request_firstline_uri_query_r_query__:map[]"));
    }
}
