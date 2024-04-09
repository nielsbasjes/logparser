/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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

package nl.basjes.parse.httpdlog;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.AbstractSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.io.Text;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class TestApacheHttpdlogDeserializer {

    private static final Logger LOG = LoggerFactory.getLogger(TestApacheHttpdlogDeserializer.class);

    private final String logformat = "%h %a %A %l %u %t \"%r\" " +
        "%>s %b %p \"%q\" \"%{Referer}i\" %D \"%{User-agent}i\" " +
        "\"%{Cookie}i\" " +
        "\"%{Set-Cookie}o\" " +
        "\"%{If-None-Match}i\" \"%{Etag}o\"";

    private final String testLogLine =
        "127.0.0.1 127.0.0.1 127.0.0.1 - - [24/Oct/2012:23:00:44 +0200] \"GET /index.php?s=800x600 HTTP/1.1\" " +
        "200 - 80 \"\" \"-\" 80991 \"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" " +
        "\"jquery-ui-theme=Eggplant; Apache=127.0.0.1.1351111543699529\" " +
        "\"" +
        "NBA-1=1234, " +
        "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT, " +
        "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/, " +
        "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/; domain=.basj.es" +
        "\" \"-\" \"-\"";

    @Test
    void testBasicParse() throws Throwable {
        // Create the SerDe
        AbstractSerDe serDe = getTestSerDe();

        // Data
        Text t = new Text(testLogLine);

        // Deserialize
        Object row = serDe.deserialize(t);

        if (!(row instanceof List)) {
            fail("row must be instanceof List<>");
        }
        List<?> rowArray = (List<?>)row;
        LOG.debug("Deserialized row: {}", row);
        assertEquals("127.0.0.1",     rowArray.get(0));
        assertEquals(1351112444000L,  rowArray.get(1));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0", rowArray.get(2));
        assertEquals(800L,            rowArray.get(3));
        assertEquals(600L,            rowArray.get(4));
    }

    @Test
    void testHighFailRatio1() throws Throwable {
        AbstractSerDe serDe = getTestSerDe();

        // Data
        Text goodLine = new Text(testLogLine);
        Text badLine = new Text("A really bad line");
        Object row;

        // Deserialize good line
        row = serDe.deserialize(goodLine);
        assertNotNull(row);

        // Deserialize bad line
        row = serDe.deserialize(badLine);
        assertNull(row);

        for (int i = 0; i < 999; i++) {
            // Deserialize good line
            row = serDe.deserialize(goodLine);
            assertNotNull(row);
        }
        assertThrows(SerDeException.class, () -> {
            Object extraRow;
            for (int i = 0; i < 99; i++) {
                // Deserialize bad line
                extraRow = serDe.deserialize(badLine);
                assertNull(extraRow);
            }
        });
    }

    private AbstractSerDe getTestSerDe() throws SerDeException {
        // Create the SerDe
        Properties schema = new Properties();
        schema.setProperty(serdeConstants.LIST_COLUMNS,
            "ip,timestamp,useragent,screenWidth,screenHeight");
        schema.setProperty(serdeConstants.LIST_COLUMN_TYPES,
            "string,bigint,string,bigint,bigint");

        schema.setProperty("logformat",           logformat);
        schema.setProperty("field:timestamp",     "TIME.EPOCH:request.receive.time.epoch");
        schema.setProperty("field:ip",            "IP:connection.client.host");
        schema.setProperty("field:useragent",     "HTTP.USERAGENT:request.user-agent");
        schema.setProperty("load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector", "x");
        schema.setProperty("map:request.firstline.uri.query.s", "SCREENRESOLUTION");
        schema.setProperty("field:screenWidth",   "SCREENWIDTH:request.firstline.uri.query.s.width");
        schema.setProperty("field:screenHeight",  "SCREENHEIGHT:request.firstline.uri.query.s.height");

        AbstractSerDe serDe = new ApacheHttpdlogDeserializer();
        serDe.initialize(new Configuration(), schema, null);
        return serDe;
    }


}
