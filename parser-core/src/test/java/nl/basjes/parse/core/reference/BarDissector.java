package nl.basjes.parse.core.reference;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.SimpleDissector;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarDissector extends SimpleDissector {

    private static HashMap<String, EnumSet<Casts>> dissectorConfig = new HashMap<>();
    static {
        dissectorConfig.put("ANY:barany",         Casts.STRING_OR_LONG_OR_DOUBLE);
        dissectorConfig.put("STRING:barstring",   Casts.STRING_ONLY);
        dissectorConfig.put("INT:barint",         Casts.STRING_OR_LONG);
        dissectorConfig.put("LONG:barlong",       Casts.STRING_OR_LONG);
        dissectorConfig.put("FLOAT:barfloat",     Casts.STRING_OR_DOUBLE);
        dissectorConfig.put("DOUBLE:bardouble",   Casts.STRING_OR_DOUBLE);
    }

    public BarDissector() {
        super("BARINPUT", dissectorConfig);
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        parsable.addDissection(inputname, "ANY",    "barany",    "42");
        parsable.addDissection(inputname, "STRING", "barstring", "42");
        parsable.addDissection(inputname, "INT",    "barint",    42);
        parsable.addDissection(inputname, "LONG",   "barlong",   42L);
        parsable.addDissection(inputname, "FLOAT",  "barfloat",  42F);
        parsable.addDissection(inputname, "DOUBLE", "bardouble", 42D);
    }
}
