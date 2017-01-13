/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.hadoop.input;

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApacheHttpdLogfileInputFormat extends
        FileInputFormat<LongWritable, ParsedRecord> {

    private String logFormat = null;
    private final Set<String> requestedFields = new HashSet<>();
    private Map<String, Set<String>> typeRemappings;
    private List<Dissector> additionalDissectors;
    private ApacheHttpdLogfileRecordReader theRecordReader;
    // --------------------------------------------

    public List<String> listPossibleFields(String logformat) {
        return listPossibleFields(logformat, typeRemappings, additionalDissectors);
    }

    public static List<String> listPossibleFields(String logformat, Map<String, Set<String>> typeRemappings, List<Dissector> additionalDissectors) {
        HttpdLoglineParser<ParsedRecord> parser = new HttpdLoglineParser<>(ParsedRecord.class, logformat);
        parser.setTypeRemappings(typeRemappings);
        parser.addDissectors(additionalDissectors);
        return parser.getPossiblePaths();
    }


    public String getLogFormat() {
        return logFormat;
    }

    public Set<String> getRequestedFields() {
        return requestedFields;
    }

    public Map<String, Set<String>> getTypeRemappings() {
        return typeRemappings;
    }

    public List<Dissector> getAdditionalDissectors() {
        return additionalDissectors;
    }

    public ApacheHttpdLogfileInputFormat() {
        super();
    }

    public ApacheHttpdLogfileInputFormat(
            String logformat,
            Collection<String> requestedFields,
            Map<String, Set<String>> typeRemappings,
            List<Dissector> additionalDissectors) {
        super();
        this.logFormat = logformat;
        this.requestedFields.addAll(requestedFields);
        this.typeRemappings = typeRemappings;
        this.additionalDissectors = additionalDissectors;
    }

    // --------------------------------------------

    public ApacheHttpdLogfileRecordReader createRecordReader() {
        return new ApacheHttpdLogfileRecordReader(getLogFormat(), getRequestedFields(), getTypeRemappings(), getAdditionalDissectors());
    }

    public ApacheHttpdLogfileRecordReader getRecordReader() {
        if (theRecordReader == null) {
            theRecordReader = createRecordReader();
        }
        return theRecordReader;
    }

    @Override
    public RecordReader<LongWritable, ParsedRecord> createRecordReader(
            final InputSplit split, final TaskAttemptContext context) {
        return getRecordReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        final CompressionCodec codec =
            new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
        return (null == codec) || codec instanceof SplittableCompressionCodec;
    }

    public void setTypeRemappings(Map<String, Set<String>> newTypeRemappings) {
        this.typeRemappings = newTypeRemappings;
    }
}
