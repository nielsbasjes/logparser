package nl.basjes.parse;

import java.io.IOException;

import nl.basjes.pig.input.apachehttpdlog.Loader;

import org.apache.hadoop.mapreduce.InputFormat;

public class OmnitureLoader extends Loader {
    @Override
    public InputFormat<?, ?> getInputFormat() 
        throws IOException {
        return new OmnitureInputFormat(getLogformat(), getRequestedFields());
    }
}
