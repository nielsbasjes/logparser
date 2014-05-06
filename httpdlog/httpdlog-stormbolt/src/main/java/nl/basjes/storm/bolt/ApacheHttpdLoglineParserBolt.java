package nl.basjes.storm.bolt;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

@SuppressWarnings("serial")
public class ApacheHttpdLoglineParserBolt extends BaseBasicBolt {

    private String logformat = "";
    private String fieldName = "apachelogline";

    private class RequestedField implements Serializable {
        public RequestedField(String field, String name) {
            this.field=field;
            this.name=name;
        }
        private String field;
        private String name;

        public String getField() {
            return field;
        }

        public String getName() {
            return name;
        }
    }

    private List<RequestedField> requestedFields; 
    
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
        this.requestedFields = new ArrayList<RequestedField>(10);
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
                parser = new ApacheHttpdLoglineParser<ParsedRecord>(ParsedRecord.class, logformat);
                Method setterMethod = ParsedRecord.class.getMethod("put", String.class, String.class);

                String[] fields = new String[requestedFields.size()];
                for (int i = 0; i < requestedFields.size(); i++) {
                    fields[i] = requestedFields.get(i).getField();
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
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (DisectionFailure e) {
            e.printStackTrace();
        } catch (InvalidDisectorException e) {
            e.printStackTrace();
        } catch (MissingDisectorsException e) {
            e.printStackTrace();
        }

        List<Object> out = new ArrayList<Object>();
        for (RequestedField requestedField: requestedFields) {
            out.add(record.get(requestedField.field));
        }
        collector.emit(out);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        List<String> fields = new ArrayList<String>();
        for (RequestedField requestedField: requestedFields) {
            fields.add(requestedField.getName());
        }
        ofd.declare(new Fields(fields));
    }

}
