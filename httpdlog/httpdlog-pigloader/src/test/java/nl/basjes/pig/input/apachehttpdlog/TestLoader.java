/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.map;
import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;

public class TestLoader {

    private PigServer createPigServerWithLoader() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        pigServer.registerQuery(
            "Clicks = " +
            "    LOAD '" + getClass().getResource("/access.log").toString() + "' " +
            "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "            '%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"'," +
            "            'IP:connection.client.host'," +
            "            'TIME.HOUR:request.receive.time.hour'," +
            "            'TIME.MINUTE:request.receive.time.minute'," +
            "            'TIME.SECOND:request.receive.time.second',"+
            "            'HTTP.FIRSTLINE:request.firstline'," +
            "            'HTTP.METHOD:request.firstline.method'," +
            "    '-map:request.firstline.uri.query.g:HTTP.URI'," +
            "            'HTTP.URI:request.firstline.uri'," +
            "            'HTTP.QUERYSTRING:request.firstline.uri.query'," +
            "            'STRING:request.firstline.uri.query.*'," +
            "            'STRING:request.firstline.uri.query.FOO'," +
            "            'HTTP.USERAGENT:request.user-agent'" +
            "            )" +
            "" +
            "         AS (" +
            "            ConnectionClientHost:chararray," +
            "            Hour:long," +
            "            Minute:long," +
            "            Second:long," +
            "            RequestFirstline:chararray," +
            "            RequestFirstlineMethod:chararray," +
            "            RequestFirstlineUri:chararray," +
            "            RequestFirstlineUriQuery:chararray," +
            "            RequestFirstlineUriQueryAll:map[]," +
            "            RequestFirstlineUriQueryFoo:chararray," +
            "            RequestUseragent:chararray" +
            "            );"
        );
        return pigServer;
    }

    @Test
    public void testBasicLoader() throws Exception {
        PigServer pigServer = createPigServerWithLoader();
        Storage.Data data = resetData(pigServer);
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(2, out.size());
        assertEquals(tuple(
                    "172.21.13.88",
                    3L,
                    4L,
                    49L,
                    "GET /1-500e-KWh?FoO=bAr%20BaR&bAr=fOo%20FoO HTTP/1.0",
                    "GET",
                    "/1-500e-KWh?FoO=bAr%20BaR&bAr=fOo%20FoO",
                    "&FoO=bAr%20BaR&bAr=fOo%20FoO",
                    map("foo", "bAr BaR",
                        "bar", "fOo FoO"),
                    "bAr BaR",
                    "Mozilla/5.0 Dummy UserAgent"),
            out.get(0));
        assertEquals(tuple(
            "172.21.13.88",
            3L,
            4L,
            49L,
            "GET /1-500e-KWh/[bla/index.html?FoO=bAr%20BaR&bAr=fOo%20FoO HTTP/1.0",
            "GET",
            "/1-500e-KWh/[bla/index.html?FoO=bAr%20BaR&bAr=fOo%20FoO",
            "&FoO=bAr%20BaR&bAr=fOo%20FoO",
            map("foo", "bAr BaR",
                "bar", "fOo FoO"),
            "bAr BaR",
            "Mozilla/5.0 Dummy UserAgent"),
            out.get(1));
    }

    @Test
    public void testBasicLoaderWithPushDown() throws Exception {
        PigServer pigServer = createPigServerWithLoader();
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks2 =" +
            "   FOREACH  Clicks " +
            "   GENERATE Hour," +
            "            Minute," +
            "            Second," +
            "            RequestFirstlineUriQuery," +
            "            RequestFirstlineUriQueryAll," +
            "            RequestFirstlineUriQueryFoo," +
            "            RequestUseragent ;");

        pigServer.registerQuery("STORE Clicks2 INTO 'Clicks2' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks2");

        assertEquals(2, out.size());
        assertEquals(tuple(
                        3L,
                        4L,
                        49L,
                        "&FoO=bAr%20BaR&bAr=fOo%20FoO",
                        map("foo", "bAr BaR",
                            "bar", "fOo FoO"),
                        "bAr BaR",
                        "Mozilla/5.0 Dummy UserAgent"),
                out.get(0));
    }


}
