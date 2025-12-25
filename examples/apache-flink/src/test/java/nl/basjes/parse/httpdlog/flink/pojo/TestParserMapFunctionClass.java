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

package nl.basjes.parse.httpdlog.flink.pojo;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.flink.TestCase;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestParserMapFunctionClass implements Serializable {

    public static class MyParserMapper extends RichMapFunction<String, MyRecord> {
        private Parser<MyRecord> parser;

        @Override
        public void open(OpenContext openContext) throws Exception {
            parser = TestCase.createTestParser();
        }

        @Override
        public MyRecord map(String line) throws Exception {
            return parser.parse(line);
        }
    }

    @Test
    void testClassDefinition() throws Exception {
        // set up the execution environment
        final StreamExecutionEnvironment env = LocalStreamEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<String> input = env.fromData(TestCase.getInputLine());

        DataStream<MyRecord> filledTestRecords = input
            .map(new MyParserMapper())
            .name("Extract Elements from logline");

        filledTestRecords.print();

        List<MyRecord> result = filledTestRecords.executeAndCollect(100);

        assertEquals(1, result.size());
        assertEquals(new MyRecord().setFullValid(), result.getFirst());
    }

}
