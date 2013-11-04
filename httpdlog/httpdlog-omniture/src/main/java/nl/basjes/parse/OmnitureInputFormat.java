package nl.basjes.parse;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import nl.basjes.hadoop.input.ApacheHttpdLogfileInputFormat;
import nl.basjes.hadoop.input.ApacheHttpdLogfileRecordReader;

public class OmnitureInputFormat extends ApacheHttpdLogfileInputFormat {
    public OmnitureInputFormat() {
        super();
    }

    public OmnitureInputFormat(String newLogformat, Collection<String> newRequestedFields) {
        super(newLogformat,newRequestedFields);
    }

    @Override
    public RecordReader<LongWritable, MapWritable> createRecordReader(final InputSplit split, final TaskAttemptContext context) throws IOException,
            InterruptedException {
        return new ApacheHttpdLogfileRecordReader(getLogformat(), getRequestedFields());
    }

}
