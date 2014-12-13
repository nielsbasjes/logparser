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
package nl.basjes.hadoop.io.serde;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.Writable;

/**
 * Hive SerDe for accessing Apache Access log files.
 * An example DDL statement
 * would be:
 * <pre>
 * CREATE EXTERNAL TABLE table_name (
 * id INT,
 * name STRING
 * )
 * ROW FORMAT SERDE 'com.cloudera.sqoop.contrib.FieldMappableSerDe'
 * WITH SERDEPROPERTIES (
 * "fieldmappable.classname" = "name.of.FieldMappable.generated.by.sqoop"
 * )
 * STORED AS SEQUENCEFILE
 * LOCATION "hdfs://hdfs.server/path/to/sequencefile";
 * </pre>
*/
public class HttpdLogFormat implements SerDe {
    public static final Log       LOG = LogFactory.getLog(HttpdLogFormat.class.getName());

    private int                   numColumns;

    private StructObjectInspector rowOI;
    private ArrayList<Object>     row;

    private List<String>          columnNames;
    private List<TypeInfo>        columnTypes;

    public class SerDeRecord {

    }

    private Parser<SerDeRecord> parser = null;

    @Override
    public void initialize(Configuration conf, Properties props)
        throws SerDeException {
        String columnNameProperty = props
                .getProperty(serdeConstants.LIST_COLUMNS);
        String columnTypeProperty = props
                .getProperty(serdeConstants.LIST_COLUMN_TYPES);
        String logformat = props.getProperty("logformat");

        try {
            parser = new ApacheHttpdLoglineParser<SerDeRecord>(
                    SerDeRecord.class, logformat);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MissingDissectorsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidDissectorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TestRecord record = new TestRecord();
        // parser.parse(record, line);
        // Map<String, String> results = record.getResults();

        columnNames = Arrays.asList(columnNameProperty.split(","));
        columnTypes = TypeInfoUtils
                .getTypeInfosFromTypeString(columnTypeProperty);
        assert columnNames.size() == columnTypes.size();
        numColumns = columnNames.size();

        List<ObjectInspector> columnOIs = new ArrayList<ObjectInspector>(
                columnNames.size());
        for (int c = 0; c < numColumns; c++) {
            columnOIs.add(TypeInfoUtils
                    .getStandardJavaObjectInspectorFromTypeInfo(columnTypes
                            .get(c)));
        }

        // StandardStruct uses ArrayList to store the row.
        rowOI = ObjectInspectorFactory.getStandardStructObjectInspector(
                columnNames, columnOIs);

        // Constructing the row object, etc, which will be reused for all rows.
        row = new ArrayList<Object>(numColumns);
        for (int c = 0; c < numColumns; c++) {
            row.add(null);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Writable> findWritableClassObject(
            String fieldMappableClassname) throws ClassNotFoundException {
        return (Class<? extends Writable>) Class.forName(fieldMappableClassname);
    }

    @Override
    public Object deserialize(Writable writable) throws SerDeException {
//    FieldMappable fieldMappable = (FieldMappable) writable;
//    Map<String, Object> fieldMap = fieldMappable.getFieldMap();
//    int columnIndex = 0;
//    for (String columnName : columnNames) {
//      if (fieldMap.containsKey(columnName)) {
//        Object value = fieldMap.get(columnName);
//        row.set(columnIndex, value);
//      } else {
//        row.set(columnIndex, null);
//        LOG.warn("Row does not contain column named '" + columnName + "'");
//      }
//      columnIndex++;
//    }
        return row;
    }

    @Override
    public Writable serialize(Object arg0, ObjectInspector arg1)
        throws SerDeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Serializer.serialize");
    }

    @Override
    public ObjectInspector getObjectInspector() throws SerDeException {
        return rowOI;
    }

    @Override
    public Class<? extends Writable> getSerializedClass() {
        return null; // FIXME fieldMappableClass;
    }

    @Override
    public SerDeStats getSerDeStats() {
        return new SerDeStats();
    }

}
