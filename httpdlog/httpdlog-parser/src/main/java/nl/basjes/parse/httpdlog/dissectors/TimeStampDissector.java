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

package nl.basjes.parse.httpdlog.dissectors;

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

        // In UTC timezone
        result.add("TIME.DAY:day_utc");
        result.add("TIME.MONTHNAME:monthname_utc");
        result.add("TIME.MONTH:month_utc");
        result.add("TIME.WEEK:weekofweekyear_utc");
        result.add("TIME.YEAR:weekyear_utc");
        result.add("TIME.YEAR:year_utc");
        result.add("TIME.HOUR:hour_utc");
        result.add("TIME.MINUTE:minute_utc");
        result.add("TIME.SECOND:second_utc");
        result.add("TIME.MILLISECOND:millisecond_utc");


        return result;
    }

    // --------------------------------------------

    private boolean wantAnyAsParsed       = false;
    private boolean wantAnyUTC            = false;
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

    // In UTC timezone
    private boolean wantDayUTC            = false;
    private boolean wantMonthnameUTC      = false;
    private boolean wantMonthUTC          = false;
    private boolean wantWeekOfWeekYearUTC = false;
    private boolean wantWeekYearUTC       = false;
    private boolean wantYearUTC           = false;
    private boolean wantHourUTC           = false;
    private boolean wantMinuteUTC         = false;
    private boolean wantSecondUTC         = false;
    private boolean wantMillisecondUTC    = false;

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

            // In UTC timezone
            case "day_utc":
                wantDayUTC = true;
                return Casts.STRING_OR_LONG;

            case "monthname_utc":
                wantMonthnameUTC = true;
                return Casts.STRING_ONLY;

            case "month_utc":
                wantMonthUTC = true;
                return Casts.STRING_OR_LONG;

            case "weekofweekyear_utc":
                wantWeekOfWeekYearUTC = true;
                return Casts.STRING_OR_LONG;

            case "weekyear_utc":
                wantWeekYearUTC = true;
                return Casts.STRING_OR_LONG;

            case "year_utc":
                wantYearUTC = true;
                return Casts.STRING_OR_LONG;

            case "hour_utc":
                wantHourUTC = true;
                return Casts.STRING_OR_LONG;

            case "minute_utc":
                wantMinuteUTC = true;
                return Casts.STRING_OR_LONG;

            case "second_utc":
                wantSecondUTC = true;
                return Casts.STRING_OR_LONG;

            case "millisecond_utc":
                wantMillisecondUTC = true;
                return Casts.STRING_OR_LONG;

            default:
                return null;
        }
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

        // In UTC timezone
        wantAnyUTC =
               wantDayUTC
            || wantMonthnameUTC
            || wantMonthUTC
            || wantWeekOfWeekYearUTC
            || wantWeekYearUTC
            || wantYearUTC
            || wantHourUTC
            || wantMinuteUTC
            || wantSecondUTC
            || wantMillisecondUTC;
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
            // FIXME: YUCK ! Parsing the same thing TWICE just for the Zone ?!?!?
            DateTime dateTime = asParsedFormatter.parseDateTime(fieldValue);
            DateTimeZone zone = dateTime.getZone();
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

        if (wantAnyUTC) {
            // In UTC timezone
            DateTime dateTime = formatter.parseDateTime(fieldValue);

            if (wantDayUTC) {
                parsable.addDissection(inputname, "TIME.DAY", "day_utc",
                        dateTime.dayOfMonth().getAsString());
            }
            if (wantMonthnameUTC) {
                parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname_utc",
                        dateTime.monthOfYear().getAsText(Locale.getDefault()));
            }
            if (wantMonthUTC) {
                parsable.addDissection(inputname, "TIME.MONTH", "month_utc",
                        dateTime.monthOfYear().getAsString());
            }
            if (wantWeekOfWeekYearUTC) {
                parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear_utc",
                        dateTime.weekOfWeekyear().getAsString());
            }
            if (wantWeekYearUTC) {
                parsable.addDissection(inputname, "TIME.YEAR", "weekyear_utc",
                        dateTime.weekyear().getAsString());
            }
            if (wantYearUTC) {
                parsable.addDissection(inputname, "TIME.YEAR", "year_utc",
                        dateTime.year().getAsString());
            }
            if (wantHourUTC) {
                parsable.addDissection(inputname, "TIME.HOUR", "hour_utc",
                        dateTime.hourOfDay().getAsString());
            }
            if (wantMinuteUTC) {
                parsable.addDissection(inputname, "TIME.MINUTE", "minute_utc",
                        dateTime.minuteOfHour().getAsString());
            }
            if (wantSecondUTC) {
                parsable.addDissection(inputname, "TIME.SECOND", "second_utc",
                        dateTime.secondOfMinute().getAsString());
            }
            if (wantMillisecondUTC) {
                parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond_utc",
                        dateTime.millisOfSecond().getAsString());
            }
        }
    }

    // --------------------------------------------

}
