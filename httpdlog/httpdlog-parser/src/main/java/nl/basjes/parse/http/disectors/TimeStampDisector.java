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

package nl.basjes.parse.http.disectors;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TimeStampDisector extends Disector {

    // --------------------------------------------

    private DateTimeFormatter formatter;
    private String dateTimePattern;

    public TimeStampDisector() {
        // We set the default parser to what we find in the Apache httpd Logfiles
        //                  [05/Sep/2010:11:27:50 +0200]
        setDateTimePattern("[dd/MMM/yyyy:HH:mm:ss ZZ]");
    }

    public TimeStampDisector(String timestampFormat) {
        formatter = DateTimeFormat.forPattern(timestampFormat);
    }

    public void setDateTimePattern(String datetimePattern) {
        this.dateTimePattern = datetimePattern;
        formatter = DateTimeFormat.forPattern(this.dateTimePattern);
    }


    // --------------------------------------------

    private static final String INPUT_TYPE = "TIME.STAMP";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public String[] getPossibleOutput() {
        String[] result = new String[9];
        result[0] = "TIME.DAY:day";
        result[1] = "TIME.MONTHNAME:monthname";
        result[2] = "TIME.MONTH:month";
        result[3] = "TIME.YEAR:year";
        result[4] = "TIME.HOUR:hour";
        result[5] = "TIME.MINUTE:minute";
        result[6] = "TIME.SECOND:second";
        result[7] = "TIME.MILLISECOND:millisecond";
        result[8] = "TIME.ZONE:timezone";
        return result;
    }

    // --------------------------------------------

    private Set<String> requestedFields = new HashSet<String>(16);

    @Override
    public void prepareForDisect(final String inputname, final String outputname) {
        requestedFields.add(outputname.substring(inputname.length() + 1));
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);
        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }
        DateTime dateTime = formatter.parseDateTime(fieldValue);

        if (requestedFields.contains("day")) {
            parsable.addDisection(inputname, "TIME.DAY", "day", dateTime.dayOfMonth().getAsString());
        }
        if (requestedFields.contains("monthname")) {
            parsable.addDisection(inputname, "TIME.MONTHNAME", "monthname", dateTime.monthOfYear().getAsText(Locale.getDefault()));
        }
        if (requestedFields.contains("month")) {
            parsable.addDisection(inputname, "TIME.MONTH", "month", dateTime.monthOfYear().getAsString());
        }
        if (requestedFields.contains("year")) {
            parsable.addDisection(inputname, "TIME.YEAR", "year", dateTime.year().getAsString());
        }
        if (requestedFields.contains("hour")) {
            parsable.addDisection(inputname, "TIME.HOUR", "hour", dateTime.hourOfDay().getAsString());
        }
        if (requestedFields.contains("minute")) {
            parsable.addDisection(inputname, "TIME.MINUTE", "minute", dateTime.minuteOfHour().getAsString());
        }
        if (requestedFields.contains("second")) {
            parsable.addDisection(inputname, "TIME.SECOND", "second", dateTime.secondOfMinute().getAsString());
        }
        if (requestedFields.contains("millisecond")) {
            parsable.addDisection(inputname, "TIME.MILLISECOND", "millisecond", dateTime.millisOfSecond().getAsString());
        }
        if (requestedFields.contains("timezone")) {
            parsable.addDisection(inputname, "TIME.TIMEZONE", "timezone", dateTime.getZone().getID());
        }
    }

    // --------------------------------------------

}
