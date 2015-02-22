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

public class TestLoadDissectorDynamically {

    private final String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\"";
    private final String logfile = getClass().getResource("/omniture-access.log").toString();

    @Test
    public void remappedLoaderTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks = " +
                    "    LOAD '" + logfile + "' " +
                    "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
                    "            '" + logformat + "'," +
                    "            'IP:connection.client.host'," +
                    "            'TIME.STAMP:request.receive.time'," +
                    "    '-map:request.firstline.uri.query.g:HTTP.URI'," +
                    "            'STRING:request.firstline.uri.query.g.query.promo'," +
                    "            'STRING:request.firstline.uri.query.s'," +
                    "    '-map:request.firstline.uri.query.s:SCREENRESOLUTION'," +
                    "    '-load:nl.basjes.parse.dissectors.http.ScreenResolutionDissector:x'," +
                    "            'SCREENWIDTH:request.firstline.uri.query.s.width'," +
                    "            'SCREENHEIGHT:request.firstline.uri.query.s.height'," +
                    "    '-map:request.firstline.uri.query.r:HTTP.URI'," +
                    "            'STRING:request.firstline.uri.query.r.query.blabla'," +
                    "            'HTTP.COOKIE:request.cookies.bui'," +
                    "            'HTTP.USERAGENT:request.user-agent'" +
                    "            )" +
                    "         AS (" +
                    "            ConnectionClientHost," +
                    "            RequestReceiveTime," +
                    "            Referrer," +
                    "            ScreenResolution," +
                    "            ScreenWidth," +
                    "            ScreenHeight," +
                    "            GoogleQuery," +
                    "            BUI," +
                    "            RequestUseragent" +
                    "            );"
        );
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(1, out.size());
        assertEquals(tuple(
                    "2001:980:91c0:1:8d31:a232:25e5:85d",
                    "[05/Sep/2010:11:27:50 +0200]",
                    "koken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066",
                    "1280x800",
                    "1280",
                    "800",
                    "blablawashere",
                    "SomeThing",
                    "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8"
            ).toDelimitedString("><#><"),
            out.get(0).toDelimitedString("><#><"));
  }


}
