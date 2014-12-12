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

package nl.basjes.parse.http.dissectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class TimeStampDissector extends Dissector {

    // --------------------------------------------

    private DateTimeFormatter formatter;
    private String dateTimePattern;

    @SuppressWarnings("UnusedDeclaration")
    public TimeStampDissector() {
        // We set the default parser to what we find in the Apache httpd Logfiles
        //                  [05/Sep/2010:11:27:50 +0200]
        setDateTimePattern("[dd/MMM/yyyy:HH:mm:ss ZZ]");
    }

    public TimeStampDissector(String newDateTimePattern) {
        setDateTimePattern(newDateTimePattern);
    }

    public void setDateTimePattern(String newDateTimePattern) {
        dateTimePattern = newDateTimePattern;
        formatter = DateTimeFormat.forPattern(dateTimePattern);
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        ((TimeStampDissector) newInstance).setDateTimePattern(dateTimePattern);
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

    private boolean wantDay = false;
    private boolean wantMonthname = false;
    private boolean wantMonth = false;
    private boolean wantWeekOfWeekYear = false;
    private boolean wantWeekYear = false;
    private boolean wantYear = false;
    private boolean wantHour = false;
    private boolean wantMinute = false;
    private boolean wantSecond = false;
    private boolean wantMillisecond = false;
    private boolean wantTimezone = false;
    private boolean wantEpoch = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname.substring(inputname.length() + 1);
        switch (name) {
            case "day":
                wantDay = true;
                return Casts.STRING_OR_LONG;

            case "monthname":
                wantMonthname = true;
                return Casts.STRING_ONLY;

            case "month":
                wantMonth = true;
                return Casts.STRING_OR_LONG;

            case "weekofweekyear":
                wantWeekOfWeekYear = true;
                return Casts.STRING_OR_LONG;

            case "weekyear":
                wantWeekYear = true;
                return Casts.STRING_OR_LONG;

            case "year":
                wantYear = true;
                return Casts.STRING_OR_LONG;

            case "hour":
                wantHour = true;
                return Casts.STRING_OR_LONG;

            case "minute":
                wantMinute = true;
                return Casts.STRING_OR_LONG;

            case "second":
                wantSecond = true;
                return Casts.STRING_OR_LONG;

            case "millisecond":
                wantMillisecond = true;
                return Casts.STRING_OR_LONG;

            case "timezone":
                wantTimezone = true;
                return Casts.STRING_ONLY;

            case "epoch":
                wantEpoch = true;
                return Casts.STRING_OR_LONG;
        }
        return null;
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);
        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }
        DateTime dateTime = formatter.parseDateTime(fieldValue);

        if (wantDay) {
            parsable.addDissection(inputname, "TIME.DAY", "day",
                    dateTime.dayOfMonth().getAsString());
        }
        if (wantMonthname) {
            parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname",
                    dateTime.monthOfYear().getAsText(Locale.getDefault()));
        }
        if (wantMonth) {
            parsable.addDissection(inputname, "TIME.MONTH", "month",
                    dateTime.monthOfYear().getAsString());
        }
        if (wantWeekOfWeekYear) {
            parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear",
                    dateTime.weekOfWeekyear().getAsString());
        }
        if (wantWeekYear) {
            parsable.addDissection(inputname, "TIME.YEAR", "weekyear",
                    dateTime.weekyear().getAsString());
        }
        if (wantYear) {
            parsable.addDissection(inputname, "TIME.YEAR", "year",
                    dateTime.year().getAsString());
        }
        if (wantHour) {
            parsable.addDissection(inputname, "TIME.HOUR", "hour",
                    dateTime.hourOfDay().getAsString());
        }
        if (wantMinute) {
            parsable.addDissection(inputname, "TIME.MINUTE", "minute",
                    dateTime.minuteOfHour().getAsString());
        }
        if (wantSecond) {
            parsable.addDissection(inputname, "TIME.SECOND", "second",
                    dateTime.secondOfMinute().getAsString());
        }
        if (wantMillisecond) {
            parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond",
                    dateTime.millisOfSecond().getAsString());
        }
        if (wantTimezone) {
            parsable.addDissection(inputname, "TIME.TIMEZONE", "timezone",
                    dateTime.getZone().getID());
        }
        if (wantEpoch) {
            parsable.addDissection(inputname, "TIME.EPOCH", "epoch",
                    Long.toString(dateTime.getMillis()));
        }
    }

    // --------------------------------------------

}
