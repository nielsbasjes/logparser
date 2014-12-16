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
package nl.basjes.parse.apachehttpdlog;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

import nl.basjes.hadoop.input.ApacheHttpdLogfileRecordReader;
import nl.basjes.hadoop.input.ParsedRecord;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.AbstractDeserializer;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.basjes.parse.core.Casts.*;
import static nl.basjes.parse.core.Casts.STRING;
import static org.apache.hadoop.hive.serde.serdeConstants.BIGINT_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.DOUBLE_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.STRING_TYPE_NAME;

/**
 * Hive SerDe for accessing Apache Access log files.
 * An example DDL statement
 * would be:
 * <pre>
 * CREATE EXTERNAL TABLE clicks (
 *     ip           STRING,
 *     timestamp    LONG,
 *     useragent    STRING,
 *     screenWidth  LONG,
 *     screenHeight LONG,
 * )
 * PARTITIONED BY(logdate STRING)
 * ROW FORMAT SERDE 'nl.basjes.parse.apachehttpdlog.HTTPDAccesslogHCatalogSerde'
 * WITH SERDEPROPERTIES (
 *     "logformat"       = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"",
 *     "field:timestamp" = "TIME.EPOCH:request.receive.time.epoch",
 *     "field:ip"        = "IP:connection.client.host",
 *     "field:useragent" = "HTTP.USERAGENT:request.user-agent",
 *     "load:nl.basjes.pig.input.apachehttpdlog.ScreenResolutionDissector" = "x",
 *     "map:request.firstline.uri.query.s" = "SCREENRESOLUTION",
 *     "field:screenWidth" = "SCREENWIDTH:request.firstline.uri.query.s.width",
 *     "field:screenHeight" = "SCREENHEIGHT:request.firstline.uri.query.s.height",
 * )
 * STORED AS TEXTFILE
 * LOCATION "hdfs://hdfs.server/path/to/access.log.gz";
 * </pre>
*/

//@SerDeSpec(schemaProps = {
//    serdeConstants.LIST_COLUMNS, serdeConstants.LIST_COLUMN_TYPES,
//    RegexSerDe.INPUT_REGEX, RegexSerDe.OUTPUT_FORMAT_STRING,
//    RegexSerDe.INPUT_REGEX_CASE_SENSITIVE
//})
public class HTTPDAccesslogHCatalogSerde extends AbstractDeserializer {
    public static final Logger      LOG = LoggerFactory.getLogger(HTTPDAccesslogHCatalogSerde.class);
    public static final String      FIELD = "field:";
    public static final int         FIELD_LENGTH = FIELD.length();

    public static final String      MAP_FIELD = "map:";
    public static final int         MAP_FIELD_LENGTH = MAP_FIELD.length();
    public static final String      LOAD_DISSECTOR = "load:";
    public static final int         LOAD_DISSECTOR_LENGTH = LOAD_DISSECTOR.length();

    private StructObjectInspector   rowOI;
    private ArrayList<Object>       row;

    private List<String>            columnNames;
    private List<TypeInfo>          columnTypes;
    private List<String>            fieldList;
    private int                     numColumns;

    private ApacheHttpdLoglineParser<ParsedRecord> parser;
    private ParsedRecord            currentValue;

    class ColumnToGetterMapping {
        int    index;
        Casts  casts;
        String fieldValue;
    }

    private List<ColumnToGetterMapping> columnToGetterMappings = new ArrayList<>();

    @Override
    public void initialize(Configuration conf, Properties props)
        throws SerDeException {

        boolean usable = true;

        String logformat = props.getProperty("logformat");

        Set<String> requestedFields = new HashSet<>();
        Map<String, Set<String>> typeRemappings = new HashMap<>();
        List<Dissector> additionalDissectors = new ArrayList<>();

        for (Map.Entry<Object, Object> property: props.entrySet()){
            String key = (String)property.getKey();
            String value = (String)property.getValue();

            if (key.startsWith(MAP_FIELD)) {
                String mapField = key.substring(MAP_FIELD_LENGTH);
                String mapType  = value;

                Set<String> remapping = typeRemappings.get(mapField);
                if (remapping == null) {
                    remapping = new HashSet<>();
                    typeRemappings.put(mapField, remapping);
                }
                remapping.add(mapType);
                LOG.info("Add mapping for field \"{}\" to type \"{}\"", mapField, mapType);
                continue;
            }

            if (key.startsWith(LOAD_DISSECTOR)) {
                String dissectorClassName = key.substring(LOAD_DISSECTOR_LENGTH);
                String dissectorParam = value;

                try {
                    Class<?> clazz = Class.forName(dissectorClassName);
                    Constructor<?> constructor = clazz.getConstructor();
                    Dissector instance = (Dissector) constructor.newInstance();
                    instance.initializeFromSettingsParameter(dissectorParam);
                    additionalDissectors.add(instance);
                } catch (ClassNotFoundException e) {
                    throw new SerDeException("Found load with bad specification: No such class:" + key);
                } catch (NoSuchMethodException e) {
                    throw new SerDeException("Found load with bad specification: Class does not have the required constructor");
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    throw new SerDeException("Found load with bad specification: Required constructor is not public");
                }
                LOG.debug("Loaded additional dissector: {}(\"{}\")", dissectorClassName, dissectorParam);
                continue;
            }
        }

        currentValue = new ParsedRecord();

        String columnNameProperty = props.getProperty(serdeConstants.LIST_COLUMNS);
        String columnTypeProperty = props.getProperty(serdeConstants.LIST_COLUMN_TYPES);
        columnNames = Arrays.asList(columnNameProperty.split(","));
        columnTypes = TypeInfoUtils.getTypeInfosFromTypeString(columnTypeProperty);
        assert columnNames.size() == columnTypes.size();
        numColumns = columnNames.size();


        parser = new ApacheHttpdLoglineParser<>(ParsedRecord.class, logformat);
        parser.setTypeRemappings(typeRemappings);
        parser.addDissectors(additionalDissectors);

        List<ObjectInspector> columnOIs = new ArrayList<ObjectInspector>(columnNames.size());
        fieldList = new ArrayList<String>(columnNames.size());

        try {
            for (int columnNr = 0; columnNr < numColumns; columnNr++) {
                columnOIs.add(TypeInfoUtils.getStandardJavaObjectInspectorFromTypeInfo(columnTypes.get(columnNr)));
                String columnName = columnNames.get(columnNr);
                TypeInfo columnType = columnTypes.get(columnNr);

                String fieldValue = props.getProperty(FIELD + columnName);

                if (fieldValue == null) {
                    LOG.error("MUST have Field value for column \"{}\".", columnName);
                    usable = false;
                    continue;
                }

                fieldList.add(fieldValue);

                ColumnToGetterMapping ctgm = new ColumnToGetterMapping();
                ctgm.index      = columnNr;
                ctgm.fieldValue = fieldValue;

                List<String> singleFieldValue= new ArrayList<>();
                singleFieldValue.add(fieldValue);
                switch (columnType.getTypeName()) {
                    case STRING_TYPE_NAME:
                        ctgm.casts = STRING;
                        parser.addParseTarget(ParsedRecord.class.getMethod("set", String.class, String.class), singleFieldValue);
                        break;
                    case BIGINT_TYPE_NAME:
                        ctgm.casts = LONG;
                        parser.addParseTarget(ParsedRecord.class.getMethod("set", String.class, Long.class), singleFieldValue);
                        break;
                    case DOUBLE_TYPE_NAME:
                        ctgm.casts = DOUBLE;
                        parser.addParseTarget(ParsedRecord.class.getMethod("set", String.class, Double.class), singleFieldValue);
                        break;
                    default:
                        LOG.error("Requested column type {} is not supported at this time.", columnType.getTypeName());
                        usable = false;
                        break;
                }
                columnToGetterMappings.add(ctgm);
            }
        } catch (NoSuchMethodException
                |SecurityException e) {
            throw new SerDeException("(Should not occur) Caught exception: {}", e);
        }

        // StandardStruct uses ArrayList to store the row.
        rowOI = ObjectInspectorFactory.getStandardStructObjectInspector(columnNames, columnOIs);

        // Constructing the row object, etc, which will be reused for all rows.
        row = new ArrayList<Object>(numColumns);
        for (int c = 0; c < numColumns; c++) {
            row.add(null);
        }

        if (!usable) {
            throw new SerDeException("Fatal config error. Check the logged error messages why.");
        }

    }

    @Override
    public ObjectInspector getObjectInspector() throws SerDeException {
        return rowOI;
    }

    @Override
    public Object deserialize(Writable writable) throws SerDeException {
        if (!(writable instanceof Text)) {
            return null; // FIXME: Report the error.
        }

        try {
            currentValue.clear();
            parser.parse(currentValue, writable.toString());
        } catch (DissectionFailure dissectionFailure) {
            return null; // FIXME: Handle better
        } catch (InvalidDissectorException e) {
            return null; // FIXME: Handle better
        } catch (MissingDissectorsException e) {
            return null; // FIXME: Handle better
        }

        for (ColumnToGetterMapping ctgm: columnToGetterMappings) {
            switch (ctgm.casts) {
                case STRING:
                    String currentValueString = currentValue.getString(ctgm.fieldValue);
                    row.set(ctgm.index, currentValueString);
                    break;
                case LONG:
                    Long currentValueLong = currentValue.getLong(ctgm.fieldValue);
                    row.set(ctgm.index, currentValueLong);
                    break;
                case DOUBLE:
                    Double currentValueDouble = currentValue.getDouble(ctgm.fieldValue);
                    row.set(ctgm.index, currentValueDouble);
                    break;
            }
        }

        return row;
    }

    @Override
    public SerDeStats getSerDeStats() {
        return new SerDeStats();
    }

}
