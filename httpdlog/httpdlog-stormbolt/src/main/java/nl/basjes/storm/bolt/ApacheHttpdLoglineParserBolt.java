/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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
package nl.basjes.storm.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class ApacheHttpdLoglineParserBolt extends BaseBasicBolt {

    private String logformat = "";
    private String fieldName = "apachelogline";

    private static class RequestedField implements Serializable {
        RequestedField(String field, String name) {
            this.field=field;
            this.name=name;
        }
        private final String field;
        private final String name;

        public String getField() {
            return field;
        }

        public String getName() {
            return name;
        }
    }

    private final List<RequestedField> requestedFields;

    public class ParsedRecord extends HashMap<String, String> {
        @Override
        public String put(String key, String value) {
            return super.put(key, value);
        }
    }

    private transient Parser<ParsedRecord> parser;

    private transient ParsedRecord parsedRecord;

    public ApacheHttpdLoglineParserBolt(String logformat, String fieldName) {
        super();
        this.logformat = logformat;
        this.fieldName = fieldName;
        this.requestedFields = new ArrayList<>();
    }

    private ParsedRecord getRecord() {
        if (parsedRecord == null) {
            parsedRecord = new ParsedRecord();
        }
        return parsedRecord;
    }

    public void requestField(String field, String name){
        requestedFields.add(new RequestedField(field, name));
    }

    private Parser<ParsedRecord> getParser() {
        if (parser == null) {
            try {
                parser = new ApacheHttpdLoglineParser<>(ParsedRecord.class, logformat);
                Method setterMethod = ParsedRecord.class.getMethod("put", String.class, String.class);

                List<String> fields = new ArrayList<>(requestedFields.size());

                for (RequestedField requestedField : requestedFields) {
                    fields.add(requestedField.getField());
                }
                parser.addParseTarget(setterMethod, fields);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return parser;
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String apacheLogLine = tuple.getStringByField(fieldName);
        ParsedRecord record = getRecord();
        try {
            record.clear();
            getParser().parse(record, apacheLogLine);
        } catch (MissingDissectorsException
                |InvalidDissectorException
                |DissectionFailure e) {
            e.printStackTrace();
        }

        List<Object> out = new ArrayList<>();
        for (RequestedField requestedField: requestedFields) {
            out.add(record.get(requestedField.field));
        }
        collector.emit(out);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        List<String> fields = new ArrayList<>();
        for (RequestedField requestedField: requestedFields) {
            fields.add(requestedField.getName());
        }
        ofd.declare(new Fields(fields));
    }

}
