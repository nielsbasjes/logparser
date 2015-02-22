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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;

public class TestRemappedLoader {

    private final String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\"";
    private final String logfile = getClass().getResource("/omniture-access.log").toString();

    @Test
    public void testRemappedLoader() throws Exception {
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
            "            'STRING:request.firstline.uri.query.g.query.*'," +
            "            'STRING:request.firstline.uri.query.s'," +
            "    '-map:request.firstline.uri.query.r:HTTP.URI'," +
            "            'STRING:request.firstline.uri.query.r.query.blabla'," +
            "            'HTTP.COOKIE:request.cookies.bui'," +
            "            'HTTP.USERAGENT:request.user-agent'" +
            "            )" +
            "         AS (" +
            "            ConnectionClientHost," +
            "            RequestReceiveTime," +
            "            Promo," +
            "            QueryParams:map[]," +
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
                map(
                    "promo"       , "koken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066",
                    "bltg.pg_nm"  , "koken-pannen",
                    "bltg.slt_nm" , "hs-koken-pannen-afj-120601",
                    "bltg.slt_id" , "303",
                    "bltg.slt_p"  , ""
                ),
                "1280x800",
                "blablawashere",
                "SomeThing",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 " +
                        "(KHTML, like Gecko) Version/5.0.1 Safari/533.17.8"
            ),
            out.get(0));
    }


    // TODO: Migrate to use PIG-4405 once that is released
    /**
     * @param input These params are alternating "key", "value". So the number of params MUST be even !!
     * Implementation is very similar to the TOMAP UDF.
     * So map("A", B, "C", D) generates a map "A"->B, "C"->D
     * @return a map containing the provided objects
     */
    public static Map<String, Object> map(Object... input) {
        if (input == null || input.length < 2) {
            return null;
        }

        try {
            Map<String, Object> output = new HashMap<String, Object>();

            for (int i = 0; i < input.length; i=i+2) {
                String key = (String)input[i];
                Object val = input[i+1];
                output.put(key, val);
            }

            return output;
        } catch (ClassCastException e){
            throw new IllegalArgumentException("Map key must be a String");
        } catch (ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("Function input must have even number of parameters");
        } catch (Exception e) {
            throw new RuntimeException("Error while creating a map", e);
        }
    }

}
