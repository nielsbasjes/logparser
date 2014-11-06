package nl.basjes.hadoop.input;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class OmnitureInputFormat extends ApacheHttpdLogfileInputFormat {
    public OmnitureInputFormat() {
        super();
    }

    public OmnitureInputFormat(String newLogformat, Collection<String> newRequestedFields) {
        super(newLogformat, newRequestedFields);
    }

    @Override
    public ApacheHttpdLogfileRecordReader createRecordReader() {
        return new OmnitureRecordReader(getLogFormat(), getRequestedFields());
    }

}
