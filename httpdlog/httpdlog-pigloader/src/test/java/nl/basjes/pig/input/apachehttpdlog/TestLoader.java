/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.basjes.pig.input.apachehttpdlog;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;

import java.util.List;

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

        assertEquals(1, out.size());
        assertEquals(tuple(
                    "172.21.13.88",
                    3L,
                    4L,
                    49L,
                    "GET /1-500e-KWh?FoO=bAr%20BaR HTTP/1.0",
                    "GET",
                    "/1-500e-KWh?FoO=bAr%20BaR",
                    "&FoO=bAr%20BaR",
                    "bAr BaR",
                    "Mozilla/5.0 Dummy UserAgent"),
            out.get(0));
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
            "            RequestFirstlineUriQueryAll:map[]," +
            "            RequestFirstlineUriQueryFoo," +
            "            RequestUseragent ;");

        pigServer.registerQuery("STORE Clicks2 INTO 'Clicks2' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks2");

        assertEquals(1, out.size());
        assertEquals(tuple(
                        3L,
                        4L,
                        49L,
                        "&FoO=bAr%20BaR",
                        "bAr BaR",
                        "Mozilla/5.0 Dummy UserAgent"),
                out.get(0));
    }


}
