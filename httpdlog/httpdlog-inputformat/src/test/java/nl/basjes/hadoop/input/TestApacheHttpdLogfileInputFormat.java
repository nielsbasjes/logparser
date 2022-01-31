package nl.basjes.hadoop.input;
/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
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

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.test.NormalValuesDissector;
import nl.basjes.parse.httpdlog.HttpdLogFormatDissector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestApacheHttpdLogfileInputFormat {
    // CHECKSTYLE.OFF: LineLength
    final String logformat = "%h %l %u %t \"%r\" %>s %O \"%{%D %F %R %T %r %a %A %b %B %C %d %G %h %H %I %j %k %l %m %M %p %S %u %Y %z}t\" \"%{User-Agent}i\"";
    // CHECKSTYLE.ON: LineLength

    @Test
    void checkInputFormat() throws IOException, InterruptedException {
        Configuration conf = new Configuration(false);
        conf.set("fs.default.name", "file:///");

        conf.set("nl.basjes.parse.apachehttpdlogline.format", logformat);

        // A ',' separated list of fields
        conf.set("nl.basjes.parse.apachehttpdlogline.fields",
            "TIME.EPOCH:request.receive.time.epoch," +
            "HTTP.USERAGENT:request.user-agent");

        File testFile = new File("src/test/resources/access.log");
        Path path = new Path(testFile.getAbsoluteFile().toURI());
        FileSplit split = new FileSplit(path, 0, testFile.length(), null);

        InputFormat<LongWritable, ParsedRecord> inputFormat = ReflectionUtils.newInstance(ApacheHttpdLogfileInputFormat.class, conf);
        TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
        RecordReader<LongWritable, ParsedRecord> reader = inputFormat.createRecordReader(split, context);

        reader.initialize(split, context);

        assertTrue(reader.nextKeyValue());

        ParsedRecord value = reader.getCurrentValue();
        assertEquals("1483272081000", value.getString("TIME.EPOCH:request.receive.time.epoch"));
        assertEquals("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
            value.getString("HTTP.USERAGENT:request.user-agent"));
    }

    @Test
    void checkAllOutputTypes() throws IOException, InterruptedException {
        Configuration conf = new Configuration(false);
        conf.set("fs.default.name", "file:///");

        // A ',' separated list of fields
        List<String> fields = Arrays.asList(
                "ANY:any",
                "ANY:any",
                "ANY:any",
                "STRING:string",
                "STRING:string",
                "STRING:string",
                "INT:int",
                "INT:int",
                "INT:int",
                "LONG:long",
                "LONG:long",
                "LONG:long",
                "FLOAT:float",
                "FLOAT:float",
                "FLOAT:float",
                "DOUBLE:double",
                "DOUBLE:double",
                "DOUBLE:double");

        File testFile = new File("src/test/resources/access.log");
        Path path = new Path(testFile.getAbsoluteFile().toURI());
        FileSplit split = new FileSplit(path, 0, testFile.length(), null);

        Map<String, Set<String>> typeRemappings = new HashMap<>();
        List<Dissector> dissectors = new ArrayList<>();
        dissectors.add(new NormalValuesDissector(HttpdLogFormatDissector.INPUT_TYPE));

        InputFormat<LongWritable, ParsedRecord> inputFormat = new ApacheHttpdLogfileInputFormat(
            logformat,
            fields,
            typeRemappings,
            dissectors);
        TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
        RecordReader<LongWritable, ParsedRecord> reader = inputFormat.createRecordReader(split, context);

        reader.initialize(split, context);

        assertTrue(reader.nextKeyValue());

        ParsedRecord value = reader.getCurrentValue();
        assertEquals("42",          value.getString("ANY:any"));             // any_string
        assertEquals(42L,           value.getLong("ANY:any").longValue());   // any_long
        assertEquals(42D,           value.getDouble("ANY:any"), 0.1D);       // any_double
        assertEquals("FortyTwo",    value.getString("STRING:string"));       // string_string
        assertEquals(null,          value.getLong("STRING:string"));         // string_long
        assertEquals(null,          value.getDouble("STRING:string"));       // string_double
        assertEquals("42",          value.getString("INT:int"));             // int_string
        assertEquals(42L,           value.getLong("INT:int").longValue());   // int_long
        assertEquals(null,          value.getDouble("INT:int"));             // int_double
        assertEquals("42",          value.getString("LONG:long"));           // long_string
        assertEquals(42L,           value.getLong("LONG:long").longValue()); // long_long
        assertEquals(null,          value.getDouble("LONG:long"));           // long_double
        assertEquals("42.0",        value.getString("FLOAT:float"));         // float_string
        assertEquals(null,          value.getLong("FLOAT:float"));           // float_long
        assertEquals(42D,           value.getDouble("FLOAT:float"), 0.1D);   // float_double
        assertEquals("42.0",        value.getString("DOUBLE:double"));       // double_string
        assertEquals(null,          value.getLong("DOUBLE:double"));         // double_long
        assertEquals(42D,           value.getDouble("DOUBLE:double"), 0.1D);  // double_double
    }
}
