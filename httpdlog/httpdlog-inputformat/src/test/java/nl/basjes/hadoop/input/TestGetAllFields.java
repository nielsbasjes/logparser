/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
package nl.basjes.hadoop.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestGetAllFields {

    @Test
    public void testGetAllField() throws IOException, InterruptedException {
        ApacheHttpdLogfileInputFormat inputFormat =
            new ApacheHttpdLogfileInputFormat(
                "common",
                Collections.singleton(ApacheHttpdLogfileRecordReader.FIELDS), Collections.emptyMap(), Collections.emptyList());
        RecordReader<LongWritable, ParsedRecord> reader = inputFormat.getRecordReader();

        Set<String> possibleFields = new HashSet<>(100);
        while (reader.nextKeyValue()){
            ParsedRecord currentValue = reader.getCurrentValue();
            String value = currentValue.getString(ApacheHttpdLogfileRecordReader.FIELDS);
            if (value == null) {
                continue;
            }
            possibleFields.add(value);
        }
        assertTrue(possibleFields.containsAll(Arrays.asList(
            // A subset of all fields that come out
            "STRING:connection.client.user",
            "IP:connection.client.host.last",
            "TIME.STAMP:request.receive.time.last",
            "TIME.EPOCH:request.receive.time.epoch",
            "STRING:request.status.last",
            "STRING:connection.client.user.last",
            "HTTP.METHOD:request.firstline.original.method",
            "HTTP.URI:request.firstline.uri",
            "HTTP.PATH:request.firstline.uri.path",
            "HTTP.QUERYSTRING:request.firstline.uri.query",
            "STRING:request.firstline.uri.query.*",
            "BYTES:response.body.bytesclf",
            "BYTESCLF:response.body.bytesclf",
            "IP:connection.client.host")));
    }

    @Test
    public void checkPossibleFields(){
        List<String> possibleFields = new ApacheHttpdLogfileInputFormat().listPossibleFields("common");
        assertTrue(possibleFields.containsAll(Arrays.asList(
            // A subset of all fields that come out
            "STRING:connection.client.user",
            "IP:connection.client.host.last",
            "TIME.STAMP:request.receive.time.last",
            "TIME.EPOCH:request.receive.time.epoch",
            "STRING:request.status.last",
            "STRING:connection.client.user.last",
            "HTTP.METHOD:request.firstline.original.method",
            "HTTP.URI:request.firstline.uri",
            "HTTP.PATH:request.firstline.uri.path",
            "HTTP.QUERYSTRING:request.firstline.uri.query",
            "STRING:request.firstline.uri.query.*",
            "BYTES:response.body.bytesclf",
            "BYTESCLF:response.body.bytesclf",
            "IP:connection.client.host")));
    }

}
