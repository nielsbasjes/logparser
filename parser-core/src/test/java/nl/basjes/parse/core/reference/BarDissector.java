package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BarDissector extends Dissector {
    @Override
    public String getInputType() {
        return "BARINPUT";
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("ANY:barany");
        result.add("STRING:barstring");
        result.add("LONG:barlong");
        result.add("DOUBLE:bardouble");
        return result;
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        parsable.addDissection(inputname, "ANY",    "barany",   "42");
        parsable.addDissection(inputname, "STRING", "barstring", "42");
        parsable.addDissection(inputname, "LONG",   "barlong",   42L);
        parsable.addDissection(inputname, "DOUBLE", "bardouble", 42D);
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        String name = extractFieldName(inputname, outputname);
        switch (name) {
            case "barstring":
                return Casts.STRING_ONLY;
            case "barlong":
                return Casts.STRING_OR_LONG;
            case "bardouble":
                return Casts.STRING_OR_DOUBLE;
            default:
                return Casts.STRING_OR_LONG_OR_DOUBLE;
        }
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
