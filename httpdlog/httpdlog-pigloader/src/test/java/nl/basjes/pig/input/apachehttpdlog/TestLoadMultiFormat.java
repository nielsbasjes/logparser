/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;

public class TestLoadMultiFormat {

    private static final String logformat_1 = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\"";
    private static final String logformat_2 = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";
    private final String logfile = getClass().getResource("/dual-format-access.log").toString();

    @Test
    public void remappedLoaderTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        String logformat = logformat_1 + '\n' + logformat_2;

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
                    "    '-load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector:x'," +
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

        assertEquals(2, out.size());
        assertEquals(tuple(
                        "2001:980:91c0:1:8d31:a232:25e5:85d",
                        "05/Sep/2010:11:27:50 +0200",
                        "koken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066",
                        "1280x800",
                        "1280",
                        "800",
                        "blablawashere",
                        "SomeThing",
                        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8"
                ).toDelimitedString("><#><"),
                out.get(0).toDelimitedString("><#><"));
        assertEquals(tuple(
                        "172.21.13.88",
                        "07/Apr/2013:03:04:49 +0200",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "Mozilla/5.0 Dummy UserAgent"
                ).toDelimitedString("><#><"),
                out.get(1).toDelimitedString("><#><"));
  }


}
