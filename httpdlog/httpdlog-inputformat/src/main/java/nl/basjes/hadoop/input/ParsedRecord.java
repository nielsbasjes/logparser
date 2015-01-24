/*
* Apache HTTPD logparsing made easy
* Copyright (C) 2011-2015 Niels Basjes
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package nl.basjes.hadoop.input;


import org.apache.hadoop.io.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedRecord implements Writable {

    private final Map<String,String> stringValues = new HashMap<>();
    private final Map<String,Long> longValues = new HashMap<>();
    private final Map<String,Double> doubleValues = new HashMap<>();
    private final Map<String,List<String>> stringSetValues = new HashMap<>();

    // FIXME: This implementation ONLY allows for END STAR situations
    private final List<String> requestedMultiValuePrefixes = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParsedRecord)) return false;

        ParsedRecord that = (ParsedRecord) o;

        if (!doubleValues.equals(that.doubleValues)) return false;
        if (!longValues.equals(that.longValues)) return false;
        if (!requestedMultiValuePrefixes.equals(that.requestedMultiValuePrefixes)) return false;
        if (!stringSetValues.equals(that.stringSetValues)) return false;
        if (!stringValues.equals(that.stringValues)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stringValues.hashCode();
        result = 31 * result + longValues.hashCode();
        result = 31 * result + doubleValues.hashCode();
        result = 31 * result + stringSetValues.hashCode();
        result = 31 * result + requestedMultiValuePrefixes.hashCode();
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(stringValues.size());
        for (Map.Entry<String, String> e: stringValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }

        out.writeInt(longValues.size());
        for (Map.Entry<String, Long> e: longValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeLong(e.getValue());
        }

        out.writeInt(doubleValues.size());
        for (Map.Entry<String, Double> e: doubleValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeDouble(e.getValue());
        }

        out.writeInt(stringSetValues.size());
        for (Map.Entry<String, List<String>> e: stringSetValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue().size());
            for (String s: e.getValue()) {
                out.writeUTF(s);
            }
        }

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        // String
        int nrOfValues = in.readInt();
        for (int count = 0 ; count < nrOfValues ; count++) {
            set(in.readUTF(), in.readUTF());
        }

        // Long
        nrOfValues = in.readInt();
        for (int count = 0 ; count < nrOfValues ; count++) {
            set(in.readUTF(), in.readLong());
        }

        // Double
        nrOfValues = in.readInt();
        for (int count = 0 ; count < nrOfValues ; count++) {
            set(in.readUTF(), in.readDouble());
        }

        // String Map
        nrOfValues = in.readInt();
        for (int count = 0 ; count < nrOfValues ; count++) {
            String key = in.readUTF();
            declareRequestedFieldname(key);
            for (int valuesCount = 0 ; valuesCount < nrOfValues ; valuesCount++) {
                setMultiValueString(in.readUTF(), in.readUTF());
            }
        }
    }

    public static final String STRING = "String";
    public static final String LONG = "Long";
    public static final String DOUBLE = "Double";
    public static final String STRING_MAP = "StringMap";
    public static final String REQUESTED_STRING_MAP = "RequestedStringMap";

    public ParsedRecord() {
    }

    public void clear() {
        stringValues.clear();
        longValues.clear();
        doubleValues.clear();
        stringSetValues.clear();
    }

    public void set(String name, String value) {
        if (value != null) {
            stringValues.put(name, value);
        }
    }

    public void set(String name, Long value) {
        if (value != null) {
            longValues.put(name, value);
        }
    }

    public void set(String name, Double value) {
        if (value != null) {
            doubleValues.put(name, value);
        }
    }

     /**
     * For multivalue things we need to know what the name is we are expecting.
     * For those patterns we match the values we get against
     * @param name the name of the requested multivalue
     */
    public void declareRequestedFieldname(String name) {
        if (name.endsWith(".*")) {
            stringSetValues.put(name, new ArrayList<String>());
        }
    }

    public void setMultiValueString(String name, String value) {
        if (value != null) {
            for (Map.Entry<String,List<String>> stringMap: stringSetValues.entrySet()) {
                String key = stringMap.getKey();
                if (name.startsWith(key, key.length()-2)) {
                    stringMap.getValue().add(value);
                }
            }
        }
    }

    public String getString(String name) {
        return stringValues.get(name);
    }

    public Long getLong(String name) {
        return longValues.get(name);
    }

    public Double getDouble(String name) {
        return doubleValues.get(name);
    }

    public List<String> getStringSet(String name) {
        return stringSetValues.get(name);
    }

}
