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

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

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
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("TIME.DAY:day");
        result.add("TIME.MONTHNAME:monthname");
        result.add("TIME.MONTH:month");
        result.add("TIME.WEEK:weekofweekyear");
        result.add("TIME.YEAR:weekyear");
        result.add("TIME.YEAR:year");
        result.add("TIME.HOUR:hour");
        result.add("TIME.MINUTE:minute");
        result.add("TIME.SECOND:second");
        result.add("TIME.MILLISECOND:millisecond");
        result.add("TIME.ZONE:timezone");
        result.add("TIME.EPOCH:epoch");
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
            parsable.addDisection(inputname, "TIME.DAY", "day",
                    dateTime.dayOfMonth().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantMonthname) {
            parsable.addDisection(inputname, "TIME.MONTHNAME", "monthname",
                    dateTime.monthOfYear().getAsText(Locale.getDefault()), EnumSet.of(Casts.STRING));
        }
        if (wantMonth) {
            parsable.addDisection(inputname, "TIME.MONTH", "month",
                    dateTime.monthOfYear().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantWeekOfWeekYear) {
            parsable.addDisection(inputname, "TIME.WEEK", "weekofweekyear",
                    dateTime.weekOfWeekyear().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantWeekYear) {
            parsable.addDisection(inputname, "TIME.YEAR", "weekyear",
                    dateTime.weekyear().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantYear) {
            parsable.addDisection(inputname, "TIME.YEAR", "year",
                    dateTime.year().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantHour) {
            parsable.addDisection(inputname, "TIME.HOUR", "hour",
                    dateTime.hourOfDay().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantMinute) {
            parsable.addDisection(inputname, "TIME.MINUTE", "minute",
                    dateTime.minuteOfHour().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantSecond) {
            parsable.addDisection(inputname, "TIME.SECOND", "second",
                    dateTime.secondOfMinute().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantMillisecond) {
            parsable.addDisection(inputname, "TIME.MILLISECOND", "millisecond",
                    dateTime.millisOfSecond().getAsString(), EnumSet.of(Casts.STRING, Casts.LONG));
        }
        if (wantTimezone) {
            parsable.addDisection(inputname, "TIME.TIMEZONE", "timezone",
                    dateTime.getZone().getID(), EnumSet.of(Casts.STRING));
        }
        if (wantEpoch) {
            parsable.addDisection(inputname, "TIME.EPOCH", "epoch",
                    Long.toString(dateTime.getMillis()), EnumSet.of(Casts.STRING, Casts.LONG));
        }
    }

    // --------------------------------------------

}
