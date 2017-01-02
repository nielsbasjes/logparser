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
    @Override
    public String getInputType() {
        return "FOOINPUT";
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("ANY:fooany");
        result.add("STRING:foostring");
        result.add("INT:fooint");
        result.add("LONG:foolong");
        result.add("FLOAT:foofloat");
        result.add("DOUBLE:foodouble");
        return result;
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        parsable.addDissection(inputname, "ANY",    "fooany",    "42");
        parsable.addDissection(inputname, "STRING", "foostring", "42");
        parsable.addDissection(inputname, "INT",    "fooint",    42);
        parsable.addDissection(inputname, "LONG",   "foolong",   42L);
        parsable.addDissection(inputname, "FLOAT",  "foofloat",  42F);
        parsable.addDissection(inputname, "DOUBLE", "foodouble", 42D);
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        String name = extractFieldName(inputname, outputname);
        switch (name) {
            case "foostring":
                return Casts.STRING_ONLY;
            case "fooint":
                return Casts.STRING_OR_LONG;
            case "foolong":
                return Casts.STRING_OR_LONG;
            case "foofloat":
                return Casts.STRING_OR_DOUBLE;
            case "foodouble":
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
