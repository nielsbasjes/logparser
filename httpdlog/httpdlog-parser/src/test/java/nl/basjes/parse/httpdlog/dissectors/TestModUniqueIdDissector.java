/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestModUniqueIdDissector {
    public static class MyRecord {

        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({"MOD_UNIQUE_ID:request.header.moduniqueid",
                "TIME.EPOCH:request.header.moduniqueid.epoch",
                "IP:request.header.moduniqueid.ip",
                "PROCESSID:request.header.moduniqueid.processid",
                "COUNTER:request.header.moduniqueid.counter",
                "THREAD_INDEX:request.header.moduniqueid.threadindex"})
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public String getValue(final String name) {
            return results.get(name);
        }

        public void clear() {
            results.clear();
        }

    }

    private static Parser<MyRecord> parser;
    private static MyRecord record;

    @BeforeClass
    public static void setUp() throws Exception {
        String logformat = "%{ModUniqueId}i";
        parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);
        parser.addTypeRemapping("request.header.moduniqueid", "MOD_UNIQUE_ID");
        for (String path: parser.getPossiblePaths()){
            System.out.println(path);
        }
        record = new MyRecord();
    }

    @Test
    public void testUniqueId1() throws Exception {
        parser.parse(record, "VaGTKApid0AAALpaNo0AAAAC");
        // This test case was verified using https://github.com/web-online/mod-unique-id-decode
        //$ ./mod_unique_id_uudecoder -i VaGTKApid0AAALpaNo0AAAAC
        //unique_id.stamp = Sun Jul 12 00:05:28 2015
        //unique_id.in_addr = 10.98.119.64
        //unique_id.pid = 47706
        //unique_id.counter = 13965
        //unique_id.thread_index = 2

        assertEquals("Full input"  , "VaGTKApid0AAALpaNo0AAAAC" , record.getValue("MOD_UNIQUE_ID:request.header.moduniqueid"));
        assertEquals("Timestamp"   , "1436652328000"            , record.getValue("TIME.EPOCH:request.header.moduniqueid.epoch"));
        assertEquals("IP Address"  , "10.98.119.64"             , record.getValue("IP:request.header.moduniqueid.ip"));
        assertEquals("ProcessID"   , "47706"                    , record.getValue("PROCESSID:request.header.moduniqueid.processid"));
        assertEquals("Counter"     , "13965"                    , record.getValue("COUNTER:request.header.moduniqueid.counter"));
        assertEquals("Thread index", "2"                        , record.getValue("THREAD_INDEX:request.header.moduniqueid.threadindex"));
    }

    @Test
    public void testUniqueId2() throws Exception {
        parser.parse(record, "Ucdv38CoEJwAAEusp6EAAADz");
        // This test case was verified using https://github.com/web-online/mod-unique-id-decode
        //$ ./mod_unique_id_uudecoder -i Ucdv38CoEJwAAEusp6EAAADz
        //unique_id.stamp = Sun Jun 23 23:59:59 2013
        //unique_id.in_addr = 192.168.16.156
        //unique_id.pid = 19372
        //unique_id.counter = 42913
        //unique_id.thread_index = 243

        assertEquals("Full input"  , "Ucdv38CoEJwAAEusp6EAAADz" , record.getValue("MOD_UNIQUE_ID:request.header.moduniqueid"));
        assertEquals("Timestamp"   , "1372024799000"            , record.getValue("TIME.EPOCH:request.header.moduniqueid.epoch"));
        assertEquals("IP Address"  , "192.168.16.156"           , record.getValue("IP:request.header.moduniqueid.ip"));
        assertEquals("ProcessID"   , "19372"                    , record.getValue("PROCESSID:request.header.moduniqueid.processid"));
        assertEquals("Counter"     , "42913"                    , record.getValue("COUNTER:request.header.moduniqueid.counter"));
        assertEquals("Thread index", "243"                      , record.getValue("THREAD_INDEX:request.header.moduniqueid.threadindex"));
    }


}
