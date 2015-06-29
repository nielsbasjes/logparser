/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.basjes.storm.bolt;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

@SuppressWarnings("serial")
public class ApacheHttpdLoglineParserBolt extends BaseBasicBolt {

    private String logformat = "";
    private String fieldName = "apachelogline";

    private static class RequestedField implements Serializable {
        public RequestedField(String field, String name) {
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
