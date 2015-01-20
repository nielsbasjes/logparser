/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

package nl.basjes.parse.dissectors.http;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TimeStampDissector extends Dissector {

    private static final Logger LOG = LoggerFactory.getLogger(TimeStampDissector.class);

    // --------------------------------------------

    private DateTimeFormatter formatter;
    private DateTimeFormatter asParsedFormatter;
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

    // --------------------------------------------

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        // There is only one setting for this dissector
        setDateTimePattern(settings);
        return true; // Everything went right.
    }

    // --------------------------------------------

    public void setDateTimePattern(String newDateTimePattern) {
        dateTimePattern = newDateTimePattern;
        formatter = DateTimeFormat.forPattern(dateTimePattern).withZone(DateTimeZone.UTC);
        asParsedFormatter = formatter.withOffsetParsed();
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
        // As parsed
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

        // Timezone independent
        result.add("TIME.ZONE:timezone");
        result.add("TIME.EPOCH:epoch");

        // In GMT timezone
        result.add("TIME.DAY:day_gmt");
        result.add("TIME.MONTHNAME:monthname_gmt");
        result.add("TIME.MONTH:month_gmt");
        result.add("TIME.WEEK:weekofweekyear_gmt");
        result.add("TIME.YEAR:weekyear_gmt");
        result.add("TIME.YEAR:year_gmt");
        result.add("TIME.HOUR:hour_gmt");
        result.add("TIME.MINUTE:minute_gmt");
        result.add("TIME.SECOND:second_gmt");
        result.add("TIME.MILLISECOND:millisecond_gmt");


        return result;
    }

    // --------------------------------------------

    private boolean wantAnyAsParsed       = false;
    private boolean wantAnyGMT            = false;
    private boolean wantAnyTZIndependent  = false;

    // As parsed
    private boolean wantDay               = false;
    private boolean wantMonthname         = false;
    private boolean wantMonth             = false;
    private boolean wantWeekOfWeekYear    = false;
    private boolean wantWeekYear          = false;
    private boolean wantYear              = false;
    private boolean wantHour              = false;
    private boolean wantMinute            = false;
    private boolean wantSecond            = false;
    private boolean wantMillisecond       = false;

    // Timezone independent
    private boolean wantTimezone          = false;
    private boolean wantEpoch             = false;

    // In GMT timezone
    private boolean wantDayGMT            = false;
    private boolean wantMonthnameGMT      = false;
    private boolean wantMonthGMT          = false;
    private boolean wantWeekOfWeekYearGMT = false;
    private boolean wantWeekYearGMT       = false;
    private boolean wantYearGMT           = false;
    private boolean wantHourGMT           = false;
    private boolean wantMinuteGMT         = false;
    private boolean wantSecondGMT         = false;
    private boolean wantMillisecondGMT    = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname.substring(inputname.length() + 1);
        switch (name) {

            // As parsed
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

            // Timezone independent
            case "timezone":
                wantTimezone = true;
                return Casts.STRING_ONLY;

            case "epoch":
                wantEpoch = true;
                return Casts.STRING_OR_LONG;

            // In GMT timezone
            case "day_gmt":
                wantDayGMT = true;
                return Casts.STRING_OR_LONG;

            case "monthname_gmt":
                wantMonthnameGMT = true;
                return Casts.STRING_ONLY;

            case "month_gmt":
                wantMonthGMT = true;
                return Casts.STRING_OR_LONG;

            case "weekofweekyear_gmt":
                wantWeekOfWeekYearGMT = true;
                return Casts.STRING_OR_LONG;

            case "weekyear_gmt":
                wantWeekYearGMT = true;
                return Casts.STRING_OR_LONG;

            case "year_gmt":
                wantYearGMT = true;
                return Casts.STRING_OR_LONG;

            case "hour_gmt":
                wantHourGMT = true;
                return Casts.STRING_OR_LONG;

            case "minute_gmt":
                wantMinuteGMT = true;
                return Casts.STRING_OR_LONG;

            case "second_gmt":
                wantSecondGMT = true;
                return Casts.STRING_OR_LONG;

            case "millisecond_gmt":
                wantMillisecondGMT = true;
                return Casts.STRING_OR_LONG;
        }
        return null;
    }

    // --------------------------------------------

    @Override
    public void prepareForRun() {
        // As parsed
        wantAnyAsParsed =
               wantDay
            || wantMonthname
            || wantMonth
            || wantWeekOfWeekYear
            || wantWeekYear
            || wantYear
            || wantHour
            || wantMinute
            || wantSecond
            || wantMillisecond;

        // Timezone independent
        wantAnyTZIndependent =
               wantTimezone
            || wantEpoch;

        // In GMT timezone
        wantAnyGMT =
               wantDayGMT
            || wantMonthnameGMT
            || wantMonthGMT
            || wantWeekOfWeekYearGMT
            || wantWeekYearGMT
            || wantYearGMT
            || wantHourGMT
            || wantMinuteGMT
            || wantSecondGMT
            || wantMillisecondGMT;
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);
        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        if (wantAnyAsParsed || wantAnyTZIndependent) {

            // YUCK !
            DateTime dateTime = asParsedFormatter.parseDateTime(fieldValue);
            DateTimeZone zone = dateTime.getZone();
//            LOG.error(dateTime.toString());
//            LOG.error(zone.toString());
            DateTimeFormatter asParsedWithZoneFormatter = asParsedFormatter.withZone(zone);
            dateTime = asParsedWithZoneFormatter.parseDateTime(fieldValue);

            // As parsed
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

            // Timezone independent
            if (wantTimezone) {
                parsable.addDissection(inputname, "TIME.TIMEZONE", "timezone",
                        dateTime.getZone().getID());
            }
            if (wantEpoch) {
                parsable.addDissection(inputname, "TIME.EPOCH", "epoch",
                        Long.toString(dateTime.getMillis()));
            }
        }

        if (wantAnyGMT) {
            // In GMT timezone
            DateTime dateTime = formatter.parseDateTime(fieldValue);

            if (wantDayGMT) {
                parsable.addDissection(inputname, "TIME.DAY", "day_gmt",
                        dateTime.dayOfMonth().getAsString());
            }
            if (wantMonthnameGMT) {
                parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname_gmt",
                        dateTime.monthOfYear().getAsText(Locale.getDefault()));
            }
            if (wantMonthGMT) {
                parsable.addDissection(inputname, "TIME.MONTH", "month_gmt",
                        dateTime.monthOfYear().getAsString());
            }
            if (wantWeekOfWeekYearGMT) {
                parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear_gmt",
                        dateTime.weekOfWeekyear().getAsString());
            }
            if (wantWeekYearGMT) {
                parsable.addDissection(inputname, "TIME.YEAR", "weekyear_gmt",
                        dateTime.weekyear().getAsString());
            }
            if (wantYearGMT) {
                parsable.addDissection(inputname, "TIME.YEAR", "year_gmt",
                        dateTime.year().getAsString());
            }
            if (wantHourGMT) {
                parsable.addDissection(inputname, "TIME.HOUR", "hour_gmt",
                        dateTime.hourOfDay().getAsString());
            }
            if (wantMinuteGMT) {
                parsable.addDissection(inputname, "TIME.MINUTE", "minute_gmt",
                        dateTime.minuteOfHour().getAsString());
            }
            if (wantSecondGMT) {
                parsable.addDissection(inputname, "TIME.SECOND", "second_gmt",
                        dateTime.secondOfMinute().getAsString());
            }
            if (wantMillisecondGMT) {
                parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond_gmt",
                        dateTime.millisOfSecond().getAsString());
            }
        }
    }

    // --------------------------------------------

}
