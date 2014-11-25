/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
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

  @Test
  public void BasicLoaderTest() throws Exception {
    PigServer pigServer = new PigServer(ExecType.LOCAL);
    Storage.Data data = resetData(pigServer);

    String logformat = "'%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" %T'";

    pigServer.registerQuery(
      "Clicks = " +
      "    LOAD '" + getClass().getResource("/access.log").toString() + "' " +
      "    USING nl.basjes.pig.input.apachehttpdlog.omniture.OmnitureLoader(" +
      "            " + logformat + "," +
      "            'IP:connection.client.host'," +
      "            'TIME.STAMP:request.receive.time'," +
      "            'STRING:request.firstline.uri.query.g.query.referrer'," +
      "            'STRING:request.firstline.uri.query.s'," +
      "            'STRING:request.firstline.uri.query.r.query.q'," +
      "            'HTTP.COOKIE:request.cookies.bui'," +
      "            'HTTP.USERAGENT:request.user-agent'" +
      "            )" +
      "         AS (" +
      "            ConnectionClientHost," +
      "            RequestReceiveTime," +
      "            Referrer," +
      "            ScreenResolution," +
      "            GoogleQuery," +
      "            BUI," +
      "            RequestUseragent" +
      "            );"
    );

    pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

    List<Tuple> out = data.get("Clicks");

    assertEquals(1, out.size());
    assertEquals(tuple(
                    "10.20.30.40",
                    "[03/Apr/2013:00:00:11 +0200]",
                    "ADVNLGOT002010100200499999999",
                    "1600x900",
                    "the dark secret of harvest home dvd",
                    "DummyValueBUI",
                    "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
                    ),
            out.get(0));
  }
}
