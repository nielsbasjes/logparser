package nl.basjes.pig.input.apachehttpdlog.omniture;

import java.io.IOException;

import nl.basjes.hadoop.input.OmnitureInputFormat;
import nl.basjes.pig.input.apachehttpdlog.Loader;

import org.apache.hadoop.mapreduce.InputFormat;

public class OmnitureLoader extends Loader {

    /**
     * Pig Loaders only take string parameters. The CTOR is really the only
     * interaction the user has with the Loader from the script.
     *
     * @param parameters The parameters entered from the PIG code.
     */
    public OmnitureLoader(String... parameters) {
        super(parameters);
    }


    @Override
    public InputFormat<?, ?> getInputFormat()
        throws IOException {
        return new OmnitureInputFormat(getLogformat(), getRequestedFields());
    }
}
