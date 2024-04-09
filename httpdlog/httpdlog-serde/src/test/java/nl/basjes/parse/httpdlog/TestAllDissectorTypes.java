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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAllDissectorTypes {

    private static final Logger LOG = LoggerFactory.getLogger(TestAllDissectorTypes.class);

    @Test
    void testAllDissectorOutputTypes() throws Throwable {
        // Create the SerDe
        AbstractSerDe serDe = getTestSerDe();

        // Data
        Text t = new Text("Doesn't matter");

        // Deserialize
        Object row = serDe.deserialize(t);
//        ObjectInspector rowOI = serDe.getObjectInspector();

        assertTrue(row instanceof List);

        @SuppressWarnings("unchecked")
        List<Object> rowArray = (List<Object>)row;
        LOG.debug("Deserialized row: {}", row);

        int index = -1;
        assertEquals("42",            rowArray.get(++index)); // any_string
        assertEquals(42L,             rowArray.get(++index)); // any_long
        assertEquals(42D,             rowArray.get(++index)); // any_double

        assertEquals("FortyTwo",      rowArray.get(++index)); // string_string
        assertEquals(null,            rowArray.get(++index)); // string_long
        assertEquals(null,            rowArray.get(++index)); // string_double

        assertEquals("42",            rowArray.get(++index)); // int_string
        assertEquals(42L,             rowArray.get(++index)); // int_long
        assertEquals(null,            rowArray.get(++index)); // int_double

        assertEquals("42",            rowArray.get(++index)); // long_string
        assertEquals(42L,             rowArray.get(++index)); // long_long
        assertEquals(null,            rowArray.get(++index)); // long_double

        assertEquals("42.0",          rowArray.get(++index)); // float_string
        assertEquals(null,            rowArray.get(++index)); // float_long
        assertEquals(42D,             rowArray.get(++index)); // float_double

        assertEquals("42.0",          rowArray.get(++index)); // double_string
        assertEquals(null,            rowArray.get(++index)); // double_long
        assertEquals(42D,             rowArray.get(++index)); // double_double
    }

    private AbstractSerDe getTestSerDe() throws SerDeException {
        // Create the SerDe
        Properties schema = new Properties();
        schema.setProperty(serdeConstants.LIST_COLUMNS,
            "any_string," +
            "any_long," +
            "any_double," +
            "string_string," +
            "string_long," +
            "string_double," +
            "int_string," +
            "int_long," +
            "int_double," +
            "long_string," +
            "long_long," +
            "long_double," +
            "float_string," +
            "float_long," +
            "float_double," +
            "double_string," +
            "double_long," +
            "double_double"
        );

        schema.setProperty(serdeConstants.LIST_COLUMN_TYPES,
            "string," +
            "bigint," +
            "double," +
            "string," +
            "bigint," +
            "double," +
            "string," +
            "bigint," +
            "double," +
            "string," +
            "bigint," +
            "double," +
            "string," +
            "bigint," +
            "double," +
            "string," +
            "bigint," +
            "double,"
        );

        schema.setProperty("logformat",           "%t");
        schema.setProperty("load:nl.basjes.parse.core.test.NormalValuesDissector", HttpdLogFormatDissector.INPUT_TYPE);

        schema.setProperty("field:any_string",     "ANY:any");
        schema.setProperty("field:any_long",       "ANY:any");
        schema.setProperty("field:any_double",     "ANY:any");
        schema.setProperty("field:string_string",  "STRING:string");
        schema.setProperty("field:string_long",    "STRING:string");
        schema.setProperty("field:string_double",  "STRING:string");
        schema.setProperty("field:int_string",     "INT:int");
        schema.setProperty("field:int_long",       "INT:int");
        schema.setProperty("field:int_double",     "INT:int");
        schema.setProperty("field:long_string",    "LONG:long");
        schema.setProperty("field:long_long",      "LONG:long");
        schema.setProperty("field:long_double",    "LONG:long");
        schema.setProperty("field:float_string",   "FLOAT:float");
        schema.setProperty("field:float_long",     "FLOAT:float");
        schema.setProperty("field:float_double",   "FLOAT:float");
        schema.setProperty("field:double_string",  "DOUBLE:double");
        schema.setProperty("field:double_long",    "DOUBLE:double");
        schema.setProperty("field:double_double",  "DOUBLE:double");

        AbstractSerDe serDe = new ApacheHttpdlogDeserializer();
        serDe.initialize(new Configuration(), schema, null);
        return serDe;
    }

}

