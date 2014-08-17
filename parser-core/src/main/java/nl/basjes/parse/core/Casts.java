package nl.basjes.parse.core;

import java.util.EnumSet;

public enum Casts {
    STRING, LONG, DOUBLE;
    public static final EnumSet<Casts> STRING_ONLY      = EnumSet.of(STRING);
    public static final EnumSet<Casts> LONG_ONLY        = EnumSet.of(LONG);
    public static final EnumSet<Casts> DOUBLE_ONLY      = EnumSet.of(DOUBLE);
    public static final EnumSet<Casts> STRING_OR_LONG   = EnumSet.of(STRING, LONG);
    public static final EnumSet<Casts> STRING_OR_DOUBLE = EnumSet.of(STRING, DOUBLE);
    public static final EnumSet<Casts> STRING_OR_LONG_OR_DOUBLE = EnumSet.of(STRING, LONG, DOUBLE);
}
