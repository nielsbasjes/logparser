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

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Disector;
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

    private String logFormat = null;
    private final Set<String> requestedFields = new HashSet<>();
    private Map<String, Set<String>> typeRemappings;
    private List<Disector> additionalDisectors;
    private ApacheHttpdLogfileRecordReader theRecordReader;
    // --------------------------------------------

    public List<String> listPossibleFields(String logformat)
            throws MissingDisectorsException, InvalidDisectorException, ParseException {
        return listPossibleFields(logformat, typeRemappings, additionalDisectors);
    }

    public static List<String> listPossibleFields(String logformat, Map<String, Set<String>> typeRemappings, List<Disector> additionalDisectors)
            throws MissingDisectorsException, InvalidDisectorException, ParseException {
        ApacheHttpdLoglineParser parser = new ApacheHttpdLoglineParser<>(ParsedRecord.class, logformat);
        parser.setTypeRemappings(typeRemappings);
        parser.addDisectors(additionalDisectors);
        return parser.getPossiblePaths();
    }


    public String getLogFormat() {
        return logFormat;
    }

    public Set<String> getRequestedFields() {
        return requestedFields;
    }

    public Map<String,Set<String>> getTypeRemappings() {
        return typeRemappings;
    }

    public List<Disector> getAdditionalDisectors() {
        return additionalDisectors;
    }

    public ApacheHttpdLogfileInputFormat() {
        super();
    }

    public ApacheHttpdLogfileInputFormat(String logformat, Collection<String> requestedFields, Map<String, Set<String>> typeRemappings, List<Disector> additionalDisectors) {
        super();
        this.logFormat = logformat;
        this.requestedFields.addAll(requestedFields);
        this.typeRemappings = typeRemappings;
        this.additionalDisectors = additionalDisectors;
    }

    // --------------------------------------------

    public ApacheHttpdLogfileRecordReader createRecordReader() {
        return new ApacheHttpdLogfileRecordReader(getLogFormat(), getRequestedFields(), getTypeRemappings(), getAdditionalDisectors());
    }

    public ApacheHttpdLogfileRecordReader getRecordReader() {
        if (theRecordReader == null) {
            theRecordReader = createRecordReader();
        }
        return theRecordReader;
    }

    @Override
    public RecordReader<LongWritable, MapWritable> createRecordReader(
            final InputSplit split, final TaskAttemptContext context) {
        return getRecordReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        final CompressionCodec codec =
            new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
        return (null == codec) || codec instanceof SplittableCompressionCodec;
    }

    public void setTypeRemappings(Map<String, Set<String>> typeRemappings) {
        this.typeRemappings = typeRemappings;
    }
}
