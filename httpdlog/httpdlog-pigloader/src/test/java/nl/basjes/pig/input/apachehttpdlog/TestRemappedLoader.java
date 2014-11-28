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

public class TestRemappedLoader {

  final String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\"";
  final String logfile = getClass().getResource("/omniture-access.log").toString();

  @Test
  public void RemappedLoaderTest() throws Exception {
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
                    "blablawashere",
                    "SomeThing",
                    "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8"


//             "GET /b/ss/advbolprod2/1/H.22.1/s73176445413647?AQB=1&pccr=true&vidn=27F07A1B85012045-4000011500517C43&&ndh=1&t=19%2F5%2F2012%2023%3A51%3A27%202%20-120&ce=UTF-8&ns=bol&pageName=%2Fnl%2Fp%2Ffissler-speciaal-pannen-grillpan-28-x-28-cm%2F9200000002876066%2F&g=http%3A%2F%2Fwww.bol.com%2Fnl%2Fp%2Ffissler-speciaal-pannen-grillpan-28-x-28-cm%2F9200000002876066%2F%3Fpromo%3Dkoken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066%26bltg.pg_nm%3Dkoken-pannen%26bltg.slt_id%3D303%26bltg.slt_nm%3Dhs-koken-pannen-afj-120601%26bltg.slt_p&r=http%3A%2F%2Fwww.bol.com%2Fnl%2Fm%2Fkoken-tafelen%2Fkoken-pannen%2FN%2F11766%2Findex.html&cc=EUR&ch=D%3Dv3&server=ps316&events=prodView%2Cevent1%2Cevent2%2Cevent31&products=%3B9200000002876066%3B%3B%3B%3Bevar3%3Dkth%7Cevar8%3D9200000002876066_Fissler%20Speciaal%20Pannen%20-%20Grillpan%20-%2028%20x%2028%20cm%7Cevar35%3D170%7Cevar47%3DKTH%7Cevar9%3DNew%7Cevar40%3Dno%20reviews%2C%3B%3B%3B%3Bevent31%3D423&c1=catalog%3Akth%3Aproduct-detail&v1=D%3Dc1&h1=catalog%2Fkth%2Fproduct-detail&h2=D%3DpageName&v3=kth&l3=endeca_001-mensen_default%2Cendeca_exact-boeken_default%2Cendeca_verschijningsjaar_default%2Cendeca_hardgoodscategoriesyn_default%2Cendeca_searchrank-hadoop_default%2Cendeca_genre_default%2Cendeca_uitvoering_default&v4=ps316&v6=koken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066&v10=Tu%2023%3A30&v12=logged%20in&v13=New&c25=niet%20ssl&c26=3631&c30=84.106.227.113.1323208998208762&v31=2000285551&c45=20120619235127&c46=20120501%204.3.4.1&c47=D%3Ds_vi&c49=%2Fnl%2Fcatalog%2Fproduct-detail.jsp&c50=%2Fnl%2Fcatalog%2Fproduct-detail.jsp&v51=www.bol.com&s=1280x800&c=24&j=1.7&v=N&k=Y&bw=1280&bh=272&p=Shockwave%20Flash%3B&AQE=1 HTTP/1.1" 200 23617 
                    

            ),
            out.get(0));
  }


}
