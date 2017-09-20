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
package nl.basjes.parse.httpdlog.storm;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpdLoglineParserBolt extends BaseBasicBolt {

    private Parser<TestRecord> parser;

    private String readFieldName;
    private String writeFieldName;

    public HttpdLoglineParserBolt(String logformat, String readFieldName, String writeFieldName) throws NoSuchMethodException {
        super();
        this.readFieldName = readFieldName;
        this.writeFieldName = writeFieldName;
        parser = TestCase.createTestParser();
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String apacheLogLine = tuple.getStringByField(readFieldName);
        try {
            List<Object> out = new ArrayList<>();
            out.add(parser.parse(apacheLogLine));
            collector.emit(out);
        } catch (MissingDissectorsException
                |InvalidDissectorException
                |DissectionFailure e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        List<String> fields = new ArrayList<>();
        fields.add(this.writeFieldName);
        ofd.declare(new Fields(fields));
    }

}
