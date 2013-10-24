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
import java.util.List;

import nl.basjes.hadoop.input.ApacheHttpdLogfileInputFormat;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.Expression;
import org.apache.pig.LoadFunc;
import org.apache.pig.LoadMetadata;
import org.apache.pig.PigException;
import org.apache.pig.ResourceSchema;
import org.apache.pig.ResourceSchema.ResourceFieldSchema;
import org.apache.pig.ResourceStatistics;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class Loader 
       extends LoadFunc 
       implements LoadMetadata {

    @SuppressWarnings("rawtypes")
    private RecordReader        reader;
    private String              logformat;
    private List<String>        requestedFields = new ArrayList<String>();
    private TupleFactory        tupleFactory;

    // ------------------------------------------

    /**
     * Pig Loaders only take string parameters. The CTOR is really the only
     * interaction the user has with the Loader from the script.
     *
     * @param parameters
     */
    public Loader(String... parameters) {

        for (String param : parameters) {
            if (logformat == null) {
                logformat = param;
            } else {
                requestedFields.add(param);
            }
        }

        tupleFactory = TupleFactory.getInstance();
    }

    // ------------------------------------------
    
    @Override
    public InputFormat<?, ?> getInputFormat() 
        throws IOException {
        return new ApacheHttpdLogfileInputFormat(logformat, requestedFields);
    }

    // ------------------------------------------

    @Override
    public Tuple getNext() 
        throws IOException {
        Tuple tuple = null;

        try {
            boolean notDone = reader.nextKeyValue();
            if (!notDone) {
                return null;
            }
            MapWritable value = (MapWritable) reader.getCurrentValue();

            if (value != null) {
                List<Object> values = new ArrayList<Object>();
                Text fieldNameText = new Text();
                for (String fieldName : requestedFields) {
                    fieldNameText.set(fieldName);
                    Object theValue = value.get(fieldNameText);
                    if (theValue == null) {
                        values.add(null);
                    } else {
                        if (isNumerical(fieldName)){
                            values.add(Long.parseLong(theValue.toString()));
                        } else {
                            values.add(theValue.toString());
                        }

                    }
                }
                tuple = tupleFactory.newTuple(values);
            }

        } catch (InterruptedException e) {
            // add more information to the runtime exception condition.
            int errCode = 6018;
            String errMsg = "Error while reading input";
            throw new ExecException(errMsg, errCode,
                    PigException.REMOTE_ENVIRONMENT, e);
        }

        return tuple;

    }

    // ------------------------------------------

    @Override
    public void prepareToRead(@SuppressWarnings("rawtypes") RecordReader newReader, PigSplit pigSplit)
        throws IOException {
        // Note that for this Loader, we don't care about the PigSplit.
        this.reader = newReader; 
    }

    // ------------------------------------------

    @Override
    public void setLocation(String location, Job job) 
        throws IOException {
        // The location is assumed to be comma separated paths.
        FileInputFormat.setInputPaths(job, location);
    }

    // ------------------------------------------

    private boolean isNumerical(String fieldName){
        return (
                // FIXME: This property should come from the disector that created the specific field.
                fieldName.startsWith("NUMBER:") || 
                fieldName.startsWith("BYTES:") || 
                fieldName.startsWith("MICROSECONDS:") ||
                fieldName.startsWith("SECONDS:") || 
                fieldName.startsWith("TIME.DAY:") || 
                fieldName.startsWith("TIME.HOUR:") || 
                fieldName.startsWith("TIME.MINUTE:") || 
                fieldName.startsWith("TIME.MONTH:") || 
                fieldName.startsWith("TIME.SECOND:") || 
                fieldName.startsWith("TIME.YEAR:") ||
                fieldName.startsWith("COUNT:")
            );
    }
    
    @Override
    public ResourceSchema getSchema(String location, Job job) throws IOException {
        ResourceSchema rs = new ResourceSchema();
        List<ResourceFieldSchema> fieldSchemaList = new ArrayList<ResourceFieldSchema>();

        for (String fieldName : requestedFields) {
            ResourceFieldSchema rfs = new ResourceFieldSchema();
            rfs.setName(fieldName);
            rfs.setDescription(fieldName);

            if (isNumerical(fieldName)){
                rfs.setType(DataType.LONG);
            } else {
                rfs.setType(DataType.CHARARRAY);
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
    
    // ------------------------------------------

}
