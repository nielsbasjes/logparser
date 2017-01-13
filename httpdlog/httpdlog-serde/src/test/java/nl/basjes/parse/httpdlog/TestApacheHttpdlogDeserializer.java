/*
 * Apache HTTPD & NGINX Access log parsing made easy
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

package nl.basjes.parse.httpdlog;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.AbstractDeserializer;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.io.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestApacheHttpdlogDeserializer {

    private static final Logger LOG = LoggerFactory.getLogger(TestApacheHttpdlogDeserializer.class);

    /**
    * Returns the union of table and partition properties,
    * with partition properties taking precedence.
    * @param tblProps table properties
    * @param partProps partitioning properties
    * @return the overlayed properties
    */
    private static Properties createOverlayedProperties(Properties tblProps, Properties partProps) {
        Properties props = new Properties();
        props.putAll(tblProps);
        if (partProps != null) {
            props.putAll(partProps);
        }
        return props;
    }

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
    public void testBasicParse() throws Throwable {
        // Create the SerDe
        AbstractDeserializer serDe = getTestSerDe();

        // Data
        Text t = new Text(testLogLine);

        // Deserialize
        Object row = serDe.deserialize(t);
//        ObjectInspector rowOI = serDe.getObjectInspector();

        assertTrue(row instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> rowArray = (List<Object>)row;
        LOG.debug("Deserialized row: {}", row);
        assertEquals("127.0.0.1",     rowArray.get(0));
        assertEquals(1351112444000L,  rowArray.get(1));
        assertEquals("Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0", rowArray.get(2));
        assertEquals(800L,            rowArray.get(3));
        assertEquals(600L,            rowArray.get(4));
        assertEquals("Desktop",       rowArray.get(5));
        assertEquals("Unknown",       rowArray.get(6));
        assertEquals("Browser",       rowArray.get(7));
        assertEquals("Firefox",       rowArray.get(8));
        assertEquals("11.0",          rowArray.get(9));
    }

    @Test (expected = SerDeException.class)
    public void testHighFailRatio1() throws Throwable {
        AbstractDeserializer serDe = getTestSerDe();

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
        for (int i = 0; i < 99; i++) {
            // Deserialize bad line
            row = serDe.deserialize(badLine);
            assertNull(row);

            // Deserialize good line
            row = serDe.deserialize(goodLine);
            assertNotNull(row);
        }
    }

    private AbstractDeserializer getTestSerDe() throws SerDeException {
        // Create the SerDe
        Properties schema = new Properties();
        schema.setProperty(serdeConstants.LIST_COLUMNS, "ip,timestamp,useragent,screenWidth,screenHeight,device_class,device_brand,agent_class,agent_name,agent_version");
        schema.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,bigint,string,bigint,bigint,string,string,string,string,string");

        schema.setProperty("logformat",           logformat);
        schema.setProperty("field:timestamp",     "TIME.EPOCH:request.receive.time.epoch");
        schema.setProperty("field:ip",            "IP:connection.client.host");
        schema.setProperty("field:useragent",     "HTTP.USERAGENT:request.user-agent");
        schema.setProperty("load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector", "x");
        schema.setProperty("map:request.firstline.uri.query.s", "SCREENRESOLUTION");
        schema.setProperty("field:screenWidth",   "SCREENWIDTH:request.firstline.uri.query.s.width");
        schema.setProperty("field:screenHeight",  "SCREENHEIGHT:request.firstline.uri.query.s.height");

        schema.setProperty("load:nl.basjes.parse.useragent.dissector.UserAgentDissector", "");
        schema.setProperty("field:device_class",   "STRING:request.user-agent.device_class");
        schema.setProperty("field:device_brand",   "STRING:request.user-agent.device_brand");
        schema.setProperty("field:agent_class",    "STRING:request.user-agent.agent_class");
        schema.setProperty("field:agent_name",     "STRING:request.user-agent.agent_name");
        schema.setProperty("field:agent_version",  "STRING:request.user-agent.agent_version");
        AbstractDeserializer serDe = new ApacheHttpdlogDeserializer();
        serDe.initialize(new Configuration(), createOverlayedProperties(schema, null));
        return serDe;
    }


}
