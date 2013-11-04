package nl.basjes.parse;

import java.io.IOException;

import nl.basjes.hadoop.input.ApacheHttpdLogfileRecordReader;
import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;

public class OmnitureRecordReader extends ApacheHttpdLogfileRecordReader {

    public ApacheHttpdLoglineParser<ParsedRecord> getParser(String logformat) throws IOException, MissingDisectorsException, InvalidDisectorException {
        return new ApacheHttpdLoglineParser<ParsedRecord>(
                ParsedRecord.class, logformat);
    }

}
