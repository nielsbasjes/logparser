package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FooDissector extends Dissector {
    private static final String INPUT_TYPE = "FOOINPUT";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("STRING:foostring");
        result.add("LONG:foolong");
        result.add("DOUBLE:foodouble");
        return result;
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        parsable.addDissection(inputname, "STRING", "foostring", "42");
        parsable.addDissection(inputname, "LONG",   "foolong",   42L);
        parsable.addDissection(inputname, "DOUBLE", "foodouble", 42D);
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        return Casts.STRING_OR_LONG_OR_DOUBLE;
    }

    @Override
    public void prepareForRun() throws InvalidDissectorException {
        // We do not do anything extra here
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        // We do not do anything extra here
    }
}
