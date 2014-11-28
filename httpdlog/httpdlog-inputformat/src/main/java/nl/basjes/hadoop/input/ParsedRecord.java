/*
* Apache HTTPD logparsing made easy
* Copyright (C) 2013 Niels Basjes
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


import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;

public class ParsedRecord extends MapWritable {

    private final MapWritable longValues;
    private final MapWritable doubleValues;

    public static final Text STRING = new Text("String");
    public static final Text LONG = new Text("Long");
    public static final Text DOUBLE = new Text("Double");

    public ParsedRecord() {
        longValues = new MapWritable();
        doubleValues = new MapWritable();

        // All names we put in here must contain a ':'.
        // So these two are safe to put in there like this.
        super.put(LONG, longValues);
        super.put(DOUBLE, doubleValues);
    }

    @Override
    public void clear() {
        super.clear();
        longValues.clear();
        doubleValues.clear();
    }

    public void set(String name, String value) {
        if (value != null) {
            put(new Text(name), new Text(value));
        }
    }

    public void set(String name, Long value) {
        if (value != null) {
            longValues.put(new Text(name), new LongWritable(value));
        }
    }

    public void set(String name, Double value) {
        if (value != null) {
            doubleValues.put(new Text(name), new DoubleWritable(value));
        }
    }

    private final Text nameText = new Text();

    public String getString(String name) {
        nameText.set(name);
        Text value = (Text) get(nameText);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public Long getLong(String name) {
        nameText.set(name);
        LongWritable value = (LongWritable) longValues.get(nameText);
        if (value == null) {
            return null;
        }
        return value.get();
    }

    public Double getDouble(String name) {
        nameText.set(name);
        DoubleWritable value = (DoubleWritable) doubleValues.get(nameText);
        if (value == null) {
            return null;
        }
        return value.get();
    }

}
