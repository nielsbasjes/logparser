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

package nl.basjes.pig.input.apachehttpdlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import nl.basjes.hadoop.input.ApacheHttpdLogfileInputFormat;

import nl.basjes.hadoop.input.ApacheHttpdLogfileRecordReader;
import nl.basjes.hadoop.input.ParsedRecord;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Parser;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.Expression;
import org.apache.pig.LoadFunc;
import org.apache.pig.LoadMetadata;
import org.apache.pig.ResourceSchema;
import org.apache.pig.ResourceSchema.ResourceFieldSchema;
import org.apache.pig.ResourceStatistics;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loader
        extends LoadFunc
        implements LoadMetadata {

    Logger LOG = LoggerFactory.getLogger(Loader.class);
    
    @SuppressWarnings("rawtypes")
    private ApacheHttpdLogfileRecordReader  reader;

    private boolean                         isBuildingFields;
    private boolean                         isBuildingExample;
    private String                          logformat;
    private final List<String>              requestedFields = new ArrayList<>();
    private final Map<String,Set<String>>   typeRemappings  = new HashMap<>();
    private final TupleFactory              tupleFactory;
    private ApacheHttpdLogfileInputFormat   theInputFormat;

    // ------------------------------------------

    /**
     * Pig Loaders only take string parameters. The CTOR is really the only
     * interaction the user has with the Loader from the script.
     *
     * @param parameters specified from the call within the pig code
     */
    public Loader(String... parameters) {

        for (String param : parameters) {
            if (logformat == null) {
                logformat = param;
                LOG.debug("Using logformat: {}", logformat);
                continue;
            }

            if (param.startsWith("-map:")) {
                String[] mapParams = param.split(":");
                if (mapParams.length != 3) {
                    throw new IllegalArgumentException("Found map with wrong number of parameters:" + param);
                }

                Set<String> remapping = typeRemappings.get(mapParams[1]);
                if (remapping == null) {
                    remapping = new HashSet<>();
                    typeRemappings.put(mapParams[1], remapping);
                }
                remapping.add(mapParams[2]);
                LOG.debug("Add Mapping: {} --> {}", mapParams[1], mapParams[2]);
                continue;
            }

            if (ApacheHttpdLogfileRecordReader.FIELDS.equals(param.toLowerCase(Locale.ENGLISH))) {
                isBuildingFields = true;
                requestedFields.add(ApacheHttpdLogfileRecordReader.FIELDS);
                LOG.debug("Requested ONLY the possible field values");
                continue;
            }

            if ("example".equals(param.toLowerCase(Locale.ENGLISH))) {
                isBuildingExample = true;
                requestedFields.add(ApacheHttpdLogfileRecordReader.FIELDS);
                LOG.debug("Requested ONLY the possible field values in EXAMPLE format");
                continue;
            }

            String cleanedFieldValue = Parser.cleanupFieldValue(param);
            LOG.debug("Add Requested field: {} ", cleanedFieldValue);
            requestedFields.add(cleanedFieldValue);
        }

        if (logformat == null) {
            throw new IllegalArgumentException("Must specify the logformat");
        }

        theInputFormat = new ApacheHttpdLogfileInputFormat(getLogformat(), getRequestedFields(), getTypeRemappings());
        reader = theInputFormat.getRecordReader();
        tupleFactory = TupleFactory.getInstance();
    }

    // ------------------------------------------

    @Override
    public InputFormat<?, ?> getInputFormat()
            throws IOException {
        return theInputFormat;
    }

    // ------------------------------------------

    public String getLogformat() {
        return logformat;
    }

    public List<String> getRequestedFields() {
        return requestedFields;
    }

    // ------------------------------------------

    @Override
    public Tuple getNext()
            throws IOException {
        Tuple tuple = null;

        if (isBuildingExample) {
            isBuildingExample = false; // Terminate on the next iteration
            return tupleFactory.newTuple(createPigExample());
        }

        boolean notDone = reader.nextKeyValue();
        if (!notDone) {
            return null;
        }

        ParsedRecord value = (ParsedRecord)reader.getCurrentValue();

        if (value != null) {
            List<Object> values = new ArrayList<>();
            for (String fieldName : requestedFields) {
                
                if (!ApacheHttpdLogfileRecordReader.FIELDS.equals(fieldName)) {
                    EnumSet<Casts> casts = reader.getParser().getCasts(fieldName);

                    if (casts != null) {
                        if (casts.contains(Casts.LONG)) {
                            values.add(value.getLong(fieldName));
                            continue;
                        }

                        if (casts.contains(Casts.DOUBLE)) {
                            values.add(value.getDouble(fieldName));
                            continue;
                        }
                    }
                }

                String theValue = value.getString(fieldName);
                if (theValue == null) {
                    values.add(null);
                    continue;
                }

                values.add(theValue);
            }
            tuple = tupleFactory.newTuple(values);
        }

        return tuple;
    }

    // ------------------------------------------

    private String createPigExample() throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        Text fieldName = new Text(requestedFields.get(0));

        ArrayList<String> fields = new ArrayList<>(128);
        ArrayList<String> names = new ArrayList<>(128);

        while (reader.nextKeyValue()) {
            MapWritable currentValue = reader.getCurrentValue();

            Writable value = currentValue.get(fieldName);
            if (value == null) {
                continue;
            }

            if (value.toString().contains("*")){
                fields.add(value.toString() + "', \t-- You cannot put a * here yet. You MUST specify a specific field.");
            } else {
                fields.add(value.toString());
            }

            String name = value.toString().split(":")[1].replace('.', '_');
            String nameComment = "";
            if (name.contains("*")){
                nameComment = ", \t-- You cannot put a * here yet. You MUST specify name.";
            }

            EnumSet<Casts> casts = reader.getCasts(value.toString());

            String cast = "bytearray";
            if (casts != null) {
                if (casts.contains(Casts.LONG)) {
                    cast = "long";
                } else {
                    if (casts.contains(Casts.DOUBLE)) {
                        cast = "double";
                    } else {
                        if (casts.contains(Casts.STRING)) {
                            cast = "chararray";
                        }
                    }
                }

                names.add(name + ':' + cast + nameComment);
            } else {
                names.add(name + nameComment);
            }
        }

        return sb.append("\n")
                .append("\n")
                .append("\n")
                .append("Clicks =\n")
                .append("    LOAD 'access.log'\n")
                .append("    USING ").append(this.getClass().getCanonicalName()).append("(\n")
                .append("    '").append(logformat).append("',\n")
                .append('\n')
                .append("        '").append(join(fields, "',\n        '")).append("')\n")
                .append("    AS (\n")
                .append("        ").append(join(names, ",\n        ")).append(");\n")
                .append("\n")
                .append("\n")
                .append("\n")
                .toString();
    }

    // ------------------------------------------

    @Override
    public void prepareToRead(@SuppressWarnings("rawtypes") RecordReader newReader, PigSplit pigSplit)
            throws IOException {
        // Note that for this Loader, we don't care about the PigSplit.

        if (newReader instanceof ApacheHttpdLogfileRecordReader) {
            this.reader = (ApacheHttpdLogfileRecordReader) newReader;
        } else {
            throw new IncorrectRecordReaderException();
        }
    }

    // ------------------------------------------

    @Override
    public void setLocation(String location, Job job)
            throws IOException {
        // The location is assumed to be comma separated paths.
        FileInputFormat.setInputPaths(job, location);
    }

    // ------------------------------------------

    @Override
    public ResourceSchema getSchema(String location, Job job) throws IOException {
        ResourceSchema rs = new ResourceSchema();
        List<ResourceFieldSchema> fieldSchemaList = new ArrayList<>();

        for (String fieldName : requestedFields) {
            ResourceFieldSchema rfs = new ResourceFieldSchema();
            rfs.setName(fieldName);
            rfs.setDescription(fieldName);

            EnumSet<Casts> casts = theInputFormat.getRecordReader().getCasts(fieldName);
            if (casts != null) {
                if (casts.contains(Casts.LONG)) {
                    rfs.setType(DataType.LONG);
                } else {
                    if (casts.contains(Casts.DOUBLE)) {
                        rfs.setType(DataType.DOUBLE);
                    } else {
                        rfs.setType(DataType.CHARARRAY);
                    }
                }
            } else {
                rfs.setType(DataType.BYTEARRAY);
            }
            fieldSchemaList.add(rfs);
        }

        rs.setFields(fieldSchemaList.toArray(new ResourceFieldSchema[fieldSchemaList.size()]));
        return rs;
    }

    // ------------------------------------------

    @Override
    public ResourceStatistics getStatistics(String location, Job job) throws IOException {
        return null;
    }

    // ------------------------------------------

    @Override
    public String[] getPartitionKeys(String location, Job job) throws IOException {
        return null;
    }

    // ------------------------------------------

    @Override
    public void setPartitionFilter(Expression partitionFilter) throws IOException {
    }

    public Map<String,Set<String>> getTypeRemappings() {
        return typeRemappings;
    }

    // ------------------------------------------

}
