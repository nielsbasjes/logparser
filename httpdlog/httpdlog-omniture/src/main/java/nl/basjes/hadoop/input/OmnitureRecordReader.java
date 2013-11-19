package nl.basjes.hadoop.input;

import java.io.IOException;
import java.util.Set;

import nl.basjes.parse.OmnitureLogLineParser;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;

public class OmnitureRecordReader extends ApacheHttpdLogfileRecordReader {

    public OmnitureRecordReader(String newLogformat,
            Set<String> newRequestedFields) {
        super(newLogformat,newRequestedFields);
    }

    public Parser<ParsedRecord> getParser(String logformat) throws IOException, MissingDisectorsException, InvalidDisectorException {
        return new OmnitureLogLineParser<ParsedRecord>(
                ParsedRecord.class, logformat);
    }

}
