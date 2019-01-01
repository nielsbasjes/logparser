/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.hadoop.input;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParsedRecord implements Writable {

    private final Map<String, String> stringValues = new HashMap<>();
    private final Map<String, Long> longValues = new HashMap<>();
    private final Map<String, Double> doubleValues = new HashMap<>();
    private final Map<String, Map<String, String>> stringSetValues = new HashMap<>();
    private final Map<String, String> stringSetPrefixes = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ParsedRecord)) {
            return false;
        }

        ParsedRecord that = (ParsedRecord) o;

        return
            stringValues.equals(that.stringValues)              &&
            longValues.equals(that.longValues)                  &&
            doubleValues.equals(that.doubleValues)              &&
            stringSetPrefixes.equals(that.stringSetPrefixes)    &&
            stringSetValues.equals(that.stringSetValues);
    }

    @Override
    public int hashCode() {
        int result = stringValues.hashCode();
        result = 31 * result + longValues.hashCode();
        result = 31 * result + doubleValues.hashCode();
        result = 31 * result + stringSetValues.hashCode();
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(stringValues.size());
        for (Map.Entry<String, String> e : stringValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }

        out.writeInt(longValues.size());
        for (Map.Entry<String, Long> e : longValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeLong(e.getValue());
        }

        out.writeInt(doubleValues.size());
        for (Map.Entry<String, Double> e : doubleValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeDouble(e.getValue());
        }

        out.writeInt(stringSetPrefixes.size());
        for (Map.Entry<String, String> e : stringSetPrefixes.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }

        out.writeInt(stringSetValues.size());
        for (Map.Entry<String, Map<String, String>> e : stringSetValues.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue().size());
            for (Map.Entry<String, String> s : e.getValue().entrySet()) {
                out.writeUTF(s.getKey());
                out.writeUTF(s.getValue());
            }
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        // String
        int nrOfValues = in.readInt();
        for (int count = 0; count < nrOfValues; count++) {
            stringValues.put(in.readUTF(), in.readUTF());
        }

        // Long
        nrOfValues = in.readInt();
        for (int count = 0; count < nrOfValues; count++) {
            longValues.put(in.readUTF(), in.readLong());
        }

        // Double
        nrOfValues = in.readInt();
        for (int count = 0; count < nrOfValues; count++) {
            doubleValues.put(in.readUTF(), in.readDouble());
        }

        // String Prefixes
        nrOfValues = in.readInt();
        for (int count = 0; count < nrOfValues; count++) {
            stringSetPrefixes.put(in.readUTF(), in.readUTF());
        }

        // String Map
        int nrOfFields = in.readInt();
        for (int fieldNr = 0; fieldNr < nrOfFields; fieldNr++) {
            String fieldName = in.readUTF();
            nrOfValues = in.readInt();
            Map<String, String> values =  new HashMap<>(nrOfValues);
            for (int valueNr = 0; valueNr < nrOfValues; valueNr++) {
                String key = in.readUTF();
                String value = in.readUTF();
                values.put(key, value);
            }
            stringSetValues.put(fieldName, values);
        }
    }

    public ParsedRecord() {
    }

    public void clear() {
        stringValues.clear();
        longValues.clear();
        doubleValues.clear();
        for (Map.Entry<String, Map<String, String>> stringMap : stringSetValues.entrySet()) {
            stringMap.getValue().clear();
        }
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
     *
     * @param name the name of the requested multivalue
     */
    public void declareRequestedFieldname(String name) {
        if (name.endsWith(".*")) {
            stringSetValues.put(name, new HashMap<>());
            stringSetPrefixes.put(name.substring(0, name.length() - 1), name);
        }
    }

    public void setMultiValueString(String name, String value) {
        if (value != null) {
            for (Map.Entry<String, String> stringSetPrefix : stringSetPrefixes.entrySet()) {
                String prefix = stringSetPrefix.getKey();
                if (name.startsWith(prefix)) {
                    stringSetValues
                        .get(stringSetPrefix.getValue())
                            .put(name.substring(prefix.length()), value);
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

    public Map<String, String> getStringSet(String name) {
        return stringSetValues.get(name);
    }

}
