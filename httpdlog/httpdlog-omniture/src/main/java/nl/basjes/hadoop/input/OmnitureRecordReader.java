package nl.basjes.hadoop.input;

import java.text.ParseException;
import java.util.Set;

import nl.basjes.parse.OmnitureLogLineParser;
import nl.basjes.parse.core.Parser;

public class OmnitureRecordReader extends ApacheHttpdLogfileRecordReader {

    public OmnitureRecordReader(String newLogformat,
            Set<String> newRequestedFields) {
        super(newLogformat, newRequestedFields);
    }

    public Parser<ParsedRecord> getParser(String logFormat) throws ParseException {
        return new OmnitureLogLineParser<>(ParsedRecord.class, logFormat);
    }

}
