package nl.basjes.hadoop.input;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

import nl.basjes.parse.OmnitureLogLineParser;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;

public class OmnitureRecordReader extends ApacheHttpdLogfileRecordReader {

    public OmnitureRecordReader(String newLogformat,
            Set<String> newRequestedFields) {
        super(newLogformat, newRequestedFields);
    }

    public Parser<ParsedRecord> getParser(String logFormat) throws IOException, MissingDisectorsException, InvalidDisectorException, ParseException {
        return new OmnitureLogLineParser<ParsedRecord>(ParsedRecord.class, logFormat);
    }

}
