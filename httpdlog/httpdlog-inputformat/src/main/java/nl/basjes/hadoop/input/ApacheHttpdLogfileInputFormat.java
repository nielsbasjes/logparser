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

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;


public class ApacheHttpdLogfileInputFormat extends
        FileInputFormat<LongWritable, MapWritable> {

    // --------------------------------------------

    public static List<String> listPossibleFields(String logformat)
        throws MissingDisectorsException, InvalidDisectorException, ParseException {
        return new ApacheHttpdLoglineParser<>(ParsedRecord.class, logformat).getPossiblePaths();
    }

    private String logFormat = null;
    public String getLogFormat() {
        return logFormat;
    }

    public Set<String> getRequestedFields() {
        return requestedFields;
    }

    private final Set<String> requestedFields = new HashSet<>();

    public ApacheHttpdLogfileInputFormat() {
        super();
    }

    public ApacheHttpdLogfileInputFormat(String newLogformat, Collection<String> newRequestedFields) {
        super();
        logFormat = newLogformat;
        requestedFields.addAll(newRequestedFields);
    }

    // --------------------------------------------

    @Override
    public RecordReader<LongWritable, MapWritable> createRecordReader(
            final InputSplit split, final TaskAttemptContext context)
        throws IOException, InterruptedException {
        return new ApacheHttpdLogfileRecordReader(getLogFormat(), getRequestedFields());
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        final CompressionCodec codec =
            new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
        if (null == codec) {
            return true;
        }
        return codec instanceof SplittableCompressionCodec;
    }
}
