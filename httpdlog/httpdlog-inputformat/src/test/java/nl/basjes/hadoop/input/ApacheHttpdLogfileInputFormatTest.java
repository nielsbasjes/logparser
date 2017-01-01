package nl.basjes.hadoop.input;
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApacheHttpdLogfileInputFormatTest {
    @Test
    public void checkInputFormat() throws IOException, InterruptedException {
        // CHECKSTYLE.OFF: LineLength
        String logformat = "%h %l %u %t \"%r\" %>s %O \"%{%D %F %R %T %r %a %A %b %B %C %d %G %h %H %I %j %k %l %m %M %p %S %u %Y %z}t\" \"%{User-Agent}i\"";
        // CHECKSTYLE.ON: LineLength

        Configuration conf = new Configuration(false);
        conf.set("fs.default.name", "file:///");

        conf.set("nl.basjes.parse.apachehttpdlogline.format", logformat);

        // A ',' separated list of fields
        conf.set("nl.basjes.parse.apachehttpdlogline.fields",
            "TIME.EPOCH:request.receive.time.epoch");

        File testFile = new File("src/test/resources/access.log");
        Path path = new Path(testFile.getAbsoluteFile().toURI());
        FileSplit split = new FileSplit(path, 0, testFile.length(), null);

        InputFormat inputFormat = ReflectionUtils.newInstance(ApacheHttpdLogfileInputFormat.class, conf);
        TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
        RecordReader reader = inputFormat.createRecordReader(split, context);

        reader.initialize(split, context);

        assertTrue(reader.nextKeyValue());

        Object value = reader.getCurrentValue();
        if (value instanceof ParsedRecord) {
            assertEquals("1483272081000", ((ParsedRecord) value).getString("TIME.EPOCH:request.receive.time.epoch"));
        } else {
            fail("Wrong return class type");
        }
    }
}
