package nl.basjes.parse.core;

import java.util.EnumSet;

public enum Castable {
    STRING, LONG, DOUBLE;
    public static final EnumSet<Castable> ALL_OPTS = EnumSet.allOf(Castable.class);
}
