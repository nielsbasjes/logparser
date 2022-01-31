/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.pig.input.apachehttpdlog;

import nl.basjes.hadoop.input.ApacheHttpdLogfileInputFormat;
import nl.basjes.hadoop.input.ApacheHttpdLogfileRecordReader;
import nl.basjes.hadoop.input.ParsedRecord;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.Expression;
import org.apache.pig.LoadFunc;
import org.apache.pig.LoadMetadata;
import org.apache.pig.LoadPushDown;
import org.apache.pig.ResourceSchema;
import org.apache.pig.ResourceSchema.ResourceFieldSchema;
import org.apache.pig.ResourceStatistics;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.util.UDFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Loader
        extends LoadFunc
        implements LoadMetadata,
                   LoadPushDown {

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    private ApacheHttpdLogfileRecordReader  reader;

    // If we ONLY want the example or the list of fields we set this to true.
    private boolean                         onlyWantListOfFields    = false;
    private boolean                         isBuildingExample       = false;
    private String                          logformat;
    private List<String>                    requestedFields         = new ArrayList<>();
    private List<String>                    originalRequestedFields = null;
    private RequiredFieldList               requiredFieldList       = null;
    private final Map<String, Set<String>>  typeRemappings          = new HashMap<>();
    private final List<Dissector>           additionalDissectors    = new ArrayList<>();
    private final TupleFactory              tupleFactory;
    private final ApacheHttpdLogfileInputFormat   theInputFormat;

    // These are purely retained to make it possible to create a working example
    private final ArrayList<String>         specialParameters    = new ArrayList<>();

    private static final String             PRUNE_PROJECTION_INFO = "prune.projection.info";

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
                specialParameters.add(param);
                String[] mapParams = param.split(":");
                if (mapParams.length != 3) {
                    throw new IllegalArgumentException("Found map with wrong number of parameters:" + param);
                }

                String mapField = mapParams[1];
                String mapType = mapParams[2];

                Set<String> remapping = typeRemappings.computeIfAbsent(mapField, k -> new HashSet<>());
                remapping.add(mapType);
                LOG.debug("Add mapping for field \"{}\" to type \"{}\"", mapField, mapType);
                continue;
            }

            if (param.startsWith("-load:")) {
                specialParameters.add(param);
                String[] loadParams = param.split(":", 3);
                if (loadParams.length != 3) {
                    throw new IllegalArgumentException("Found load with wrong number of parameters:" + param);
                }

                String dissectorClassName = loadParams[1];
                String dissectorParam = loadParams[2];

                try {
                    Class<?> clazz = Class.forName(dissectorClassName);
                    Constructor<?> constructor = clazz.getConstructor();
                    Dissector instance = (Dissector) constructor.newInstance();
                    if (!instance.initializeFromSettingsParameter(dissectorParam)) {
                        throw new IllegalArgumentException("Initialization failed of dissector instance of class " + dissectorClassName);
                    }
                    additionalDissectors.add(instance);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Found load with bad specification: No such class:" + param, e);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Found load with bad specification: Class does not have the required constructor", e);
                } catch (InvocationTargetException | InstantiationException e) {
                    throw new IllegalArgumentException("Unable to load specified dissector", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Found load with bad specification: Required constructor is not public", e);
                }
                LOG.debug("Loaded additional dissector: {}(\"{}\")", dissectorClassName, dissectorParam);
                continue;
            }

            if (ApacheHttpdLogfileRecordReader.FIELDS.equals(param.toLowerCase(Locale.ENGLISH))) {
                onlyWantListOfFields = true;
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

        if (requestedFields.isEmpty()) {
            isBuildingExample = true;
            requestedFields.add(ApacheHttpdLogfileRecordReader.FIELDS);
            LOG.debug("Requested ONLY the possible field values in EXAMPLE format");
        }

        theInputFormat = new ApacheHttpdLogfileInputFormat(getLogformat(), getRequestedFields(), getTypeRemappings(), getAdditionalDissectors());
        reader = theInputFormat.getRecordReader();
        tupleFactory = TupleFactory.getInstance();
    }

    // ------------------------------------------

    @Override
    public InputFormat<?, ?> getInputFormat() {
        return theInputFormat;
    }

    // ------------------------------------------

    public final String getLogformat() {
        return logformat;
    }

    public final List<String> getRequestedFields() {
        return requestedFields;
    }

    // ------------------------------------------

    @Override
    public Tuple getNext()
            throws IOException {
        Tuple tuple = null;
        try {
            if (isBuildingExample) {
                isBuildingExample = false; // Terminate on the next iteration
                return tupleFactory.newTuple(createPigExample());
            }

            boolean notDone = reader.nextKeyValue();
            if (!notDone) {
                return null;
            }

            ParsedRecord value = reader.getCurrentValue();

            if (value != null) {
                List<Object> values = new ArrayList<>();
                if (onlyWantListOfFields) {
                    values.add(value.getString(ApacheHttpdLogfileRecordReader.FIELDS));
                } else {
                    for (String fieldName : requestedFields) {
                        if (fieldName.endsWith(".*")) {
                            values.add(value.getStringSet(fieldName));
                            continue;
                        } else {
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
                        values.add(value.getString(fieldName));
                    }
                }
                tuple = tupleFactory.newTuple(values);
            }
        } catch (InvalidDissectorException | MissingDissectorsException e) {
            throw new IOException("Fatal error in the parser", e);
        }
        return tuple;
    }

    // ------------------------------------------

    private static final String MULTI_COMMENT = "  -- If you only want a single field replace * with name and change type to chararray";

    private String createPigExample() throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        String fieldName = requestedFields.get(0);

        ArrayList<String> fields = new ArrayList<>(128);
        ArrayList<String> names  = new ArrayList<>(128);

        while (reader.nextKeyValue()) {
            ParsedRecord currentValue = reader.getCurrentValue();

            String value = currentValue.getString(fieldName);
            if (value == null) {
                continue;
            }

            if (value.contains("*")) {
                fields.add(value + "'," + MULTI_COMMENT);
            } else {
                fields.add(value);
            }

            String name = value.split(":")[1]
                            .replace('.', '_')
                            .replace('-', '_')
                            .replace('*', '_');

            EnumSet<Casts> casts = reader.getCasts(value);

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
                            if (value.contains("*")) {
                                cast = "map[]," + MULTI_COMMENT;
                            }
                        }
                    }
                }

                names.add(name + ':' + cast);
            } else {
                names.add(name);
            }
        }


        sb  .append("\n")
            .append("\n")
            .append("\n")
            .append("Clicks =\n")
            .append("    LOAD 'access.log'\n")
            .append("    USING ").append(this.getClass().getCanonicalName()).append("(\n")
            .append("        '").append(logformat).append("',\n")
            .append('\n');

        if (!specialParameters.isEmpty()) {
            sb.append("        '").append(join(specialParameters, "',\n        '")).append("',\n");
        }

        sb  .append("        '").append(join(fields, "',\n        '")).append("')\n")
            .append("    AS (\n")
            .append("        ").append(join(names, ",\n        ")).append(");\n")
            .append("\n")
            .append("\n")
            .append("\n");
        return sb.toString();
    }

    // ------------------------------------------

    @Override
    public void prepareToRead(RecordReader newReader, PigSplit pigSplit) {
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

        requiredFieldList = (RequiredFieldList) getFromUDFContext(PRUNE_PROJECTION_INFO);

        // If we encounter a PushDown Projection we strip the requestedFields to only the needed ones
        // This pruning will very effectively push the projection down into the actual parser system.
        if (requiredFieldList != null &&
            originalRequestedFields == null) { // Avoid pruning twice !!
            Set<Integer> requestedFieldIndexes = new HashSet<>();
            for (RequiredField requiredField : requiredFieldList.getFields()) {
                requestedFieldIndexes.add(requiredField.getIndex());
            }
            List<String> prunedRequestedFields = new ArrayList<>(requestedFieldIndexes.size());
            int index = 0;
            for (String field : requestedFields) {
                if (requestedFieldIndexes.contains(index)) {
                    prunedRequestedFields.add(field);
                }
                ++index;
            }
            originalRequestedFields = requestedFields;
            requestedFields = prunedRequestedFields;
        }
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

            if(fieldName.endsWith(".*")) {
                rfs.setType(DataType.MAP);
            } else {
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
            }
            fieldSchemaList.add(rfs);
        }

        rs.setFields(fieldSchemaList.toArray(new ResourceFieldSchema[fieldSchemaList.size()]));
        return rs;
    }

    // ------------------------------------------

    @Override
    public ResourceStatistics getStatistics(String location, Job job) {
        return null;
    }

    // ------------------------------------------

    @Override
    public String[] getPartitionKeys(String location, Job job) {
        return null;
    }

    // ------------------------------------------

    @Override
    public void setPartitionFilter(Expression partitionFilter) {
    }

    public final Map<String, Set<String>> getTypeRemappings() {
        return typeRemappings;
    }

    public List<Dissector> getAdditionalDissectors() {
        return additionalDissectors;
    }

    public List<OperatorSet> getFeatures() {
        return Collections.singletonList(OperatorSet.PROJECTION);
    }

    @Override
    public RequiredFieldResponse pushProjection(RequiredFieldList pRequiredFieldList) {
        this.requiredFieldList = pRequiredFieldList;
        // Store the required fields information in the UDFContext.
        storeInUDFContext(PRUNE_PROJECTION_INFO, this.requiredFieldList);
        return new RequiredFieldResponse(true);
    }

    private String theUDFContextSignature;
    @Override
    public void setUDFContextSignature(String signature) {
        this.theUDFContextSignature = signature;
    }

    private void storeInUDFContext(String key, Object value) {
        UDFContext udfContext = UDFContext.getUDFContext();
        Properties props = udfContext.getUDFProperties(
                this.getClass(), new String[]{theUDFContextSignature});
        props.put(key, value);
    }

    private Object getFromUDFContext(String key) {
        UDFContext udfContext = UDFContext.getUDFContext();
        Properties udfProperties = udfContext.getUDFProperties(
                this.getClass(), new String[]{theUDFContextSignature});
        return udfProperties.get(key);
    }

    // ------------------------------------------

}
