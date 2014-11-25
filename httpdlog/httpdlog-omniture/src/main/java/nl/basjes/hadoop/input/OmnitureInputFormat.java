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
