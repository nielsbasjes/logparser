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

import java.util.Locale;

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

    @SuppressWarnings("UnusedDeclaration")
    public TimeStampDisector() {
        // We set the default parser to what we find in the Apache httpd Logfiles
        //                  [05/Sep/2010:11:27:50 +0200]
        setDateTimePattern("[dd/MMM/yyyy:HH:mm:ss ZZ]");
    }

    public TimeStampDisector(String newDateTimePattern) {
        setDateTimePattern(newDateTimePattern);
    }

    public void setDateTimePattern(String newDateTimePattern) {
        dateTimePattern = newDateTimePattern;
        formatter = DateTimeFormat.forPattern(dateTimePattern);
    }

    @Override
    protected void initializeNewInstance(Disector newInstance) {
        ((TimeStampDisector)newInstance).setDateTimePattern(dateTimePattern);
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
        String[] result = new String[12];
        result[0]  = "TIME.DAY:day";
        result[1]  = "TIME.MONTHNAME:monthname";
        result[2]  = "TIME.MONTH:month";
        result[3]  = "TIME.WEEK:weekofweekyear";
        result[4]  = "TIME.YEAR:weekyear";
        result[5]  = "TIME.YEAR:year";
        result[6]  = "TIME.HOUR:hour";
        result[7]  = "TIME.MINUTE:minute";
        result[8]  = "TIME.SECOND:second";
        result[9]  = "TIME.MILLISECOND:millisecond";
        result[10] = "TIME.ZONE:timezone";
        result[11] = "TIME.EPOCH:epoch";
        return result;
    }

    // --------------------------------------------

    private boolean wantDay            = false;
    private boolean wantMonthname      = false;
    private boolean wantMonth          = false;
    private boolean wantWeekOfWeekYear = false;
    private boolean wantWeekYear       = false;
    private boolean wantYear           = false;
    private boolean wantHour           = false;
    private boolean wantMinute         = false;
    private boolean wantSecond         = false;
    private boolean wantMillisecond    = false;
    private boolean wantTimezone       = false;
    private boolean wantEpoch          = false;

    @Override
    public void prepareForDisect(final String inputname, final String outputname) {
        String name = outputname.substring(inputname.length() + 1);
        if ("day".equals(name))            { wantDay            = true; }
        if ("monthname".equals(name))      { wantMonthname      = true; }
        if ("month".equals(name))          { wantMonth          = true; }
        if ("weekofweekyear".equals(name)) { wantWeekOfWeekYear = true; }
        if ("weekyear".equals(name))       { wantWeekYear       = true; }
        if ("year".equals(name))           { wantYear           = true; }
        if ("hour".equals(name))           { wantHour           = true; }
        if ("minute".equals(name))         { wantMinute         = true; }
        if ("second".equals(name))         { wantSecond         = true; }
        if ("millisecond".equals(name))    { wantMillisecond    = true; }
        if ("timezone".equals(name))       { wantTimezone       = true; }
        if ("epoch".equals(name))          { wantEpoch          = true; }
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

        if (wantDay) {
            parsable.addDisection(inputname, "TIME.DAY", "day", dateTime.dayOfMonth().getAsString());
        }
        if (wantMonthname) {
            parsable.addDisection(inputname, "TIME.MONTHNAME", "monthname", dateTime.monthOfYear().getAsText(Locale.getDefault()));
        }
        if (wantMonth) {
            parsable.addDisection(inputname, "TIME.MONTH", "month", dateTime.monthOfYear().getAsString());
        }
        if (wantWeekOfWeekYear) {
            parsable.addDisection(inputname, "TIME.WEEK", "weekofweekyear", dateTime.weekOfWeekyear().getAsString());
        }
        if (wantWeekYear) {
            parsable.addDisection(inputname, "TIME.YEAR", "weekyear", dateTime.weekyear().getAsString());
        }
        if (wantYear) {
            parsable.addDisection(inputname, "TIME.YEAR", "year", dateTime.year().getAsString());
        }
        if (wantHour) {
            parsable.addDisection(inputname, "TIME.HOUR", "hour", dateTime.hourOfDay().getAsString());
        }
        if (wantMinute) {
            parsable.addDisection(inputname, "TIME.MINUTE", "minute", dateTime.minuteOfHour().getAsString());
        }
        if (wantSecond) {
            parsable.addDisection(inputname, "TIME.SECOND", "second", dateTime.secondOfMinute().getAsString());
        }
        if (wantMillisecond) {
            parsable.addDisection(inputname, "TIME.MILLISECOND", "millisecond", dateTime.millisOfSecond().getAsString());
        }
        if (wantTimezone) {
            parsable.addDisection(inputname, "TIME.TIMEZONE", "timezone", dateTime.getZone().getID());
        }
        if (wantEpoch) {
            parsable.addDisection(inputname, "TIME.EPOCH", "epoch", Long.toString(dateTime.getMillis()));
        }
    }

    // --------------------------------------------

}
