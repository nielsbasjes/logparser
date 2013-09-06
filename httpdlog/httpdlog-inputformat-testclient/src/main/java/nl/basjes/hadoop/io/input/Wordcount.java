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
package nl.basjes.hadoop.io.input;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.basjes.hadoop.input.ApacheHttpdLogfileInputFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Wordcount extends Configured implements Tool {

    // ----------------------------------------------------------------------

    private static// httpd.conf has this next line:
          // LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined
    String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";

    // ----------------------------------------------------------------------

    public static class TokenizerMapper extends
            Mapper<Object, MapWritable, Text, IntWritable> {

        private static final IntWritable ONE  = new IntWritable(1);
        private Text                     word = new Text();

        @Override
        public void map(Object key, MapWritable value, Context context)
            throws IOException, InterruptedException {
            for (Map.Entry<Writable, Writable> entry : value.entrySet()) {
                word.set(entry.getValue().toString());
                context.write(word, ONE);
            }
        }
    }

    // ----------------------------------------------------------------------

    public static class IntSumReducer extends
            Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    // ----------------------------------------------------------------------

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: wordcount <in> <out>");
            System.exit(2);
        }

        conf.set("nl.basjes.parse.apachehttpdlogline.format", logformat);

        // A ',' separated list of fields
        conf.set("nl.basjes.parse.apachehttpdlogline.fields",
                "STRING:request.status.last");

        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(Wordcount.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

        job.setInputFormatClass(ApacheHttpdLogfileInputFormat.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        // configuration should contain reference to your namenode
        FileSystem fs = FileSystem.get(conf);
        // true stands for recursively deleting the folder you gave
        Path outputPath = new Path(otherArgs[1]);
        fs.delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, outputPath);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        if (job.waitForCompletion(true)) {
            return 0;
        }
        return 1;
    }

    // ----------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        // Developer suggestion:
        // This is what you do to find out what the possible fields are:
        List<String> possibleFields = ApacheHttpdLogfileInputFormat
                .listPossibleFields(logformat);
        System.out.println("----------------------------------------");
        System.out.println("All possible fields are:");
        for (String field : possibleFields) {
            System.out.println(field);
        }
        System.out.println("----------------------------------------");

        System.exit(ToolRunner.run(new Configuration(), new Wordcount(), args));
    }
}
