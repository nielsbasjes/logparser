/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class TimeStampDissector extends Dissector {

    // The default parser to what we find in the Apache httpd Logfiles
    //                                                            [05/Sep/2010:11:27:50 +0200]
    public static final String DEFAULT_APACHE_DATE_TIME_PATTERN = "dd/MMM/yyyy:HH:mm:ss ZZ";

    // --------------------------------------------

    private DateTimeFormatter formatter;
    private DateTimeFormatter asParsedFormatter;
    private String dateTimePattern;

    @SuppressWarnings("UnusedDeclaration")
    public TimeStampDissector() {
        setDateTimePattern(DEFAULT_APACHE_DATE_TIME_PATTERN);
    }

    public TimeStampDissector(String newDateTimePattern) {
        if (newDateTimePattern == null ||
            newDateTimePattern.trim().isEmpty()) {
            setDateTimePattern(DEFAULT_APACHE_DATE_TIME_PATTERN);
        } else {
            setDateTimePattern(newDateTimePattern);
        }
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

        result.add("TIME.DATE:date"); // yyyy-MM-dd
        result.add("TIME.TIME:time"); // HH:mm:ss

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

        result.add("TIME.DATE:date_utc"); // yyyy-MM-dd
        result.add("TIME.TIME:time_utc"); // HH:mm:ss

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
    private boolean wantDate              = false;
    private boolean wantTime              = false;


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
    private boolean wantDateUTC           = false;
    private boolean wantTimeUTC           = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = extractFieldName(inputname, outputname);
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

            case "date":
                wantDate = true;
                return Casts.STRING_ONLY;

            case "time":
                wantTime = true;
                return Casts.STRING_ONLY;

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

            case "date_utc":
                wantDateUTC = true;
                return Casts.STRING_ONLY;

            case "time_utc":
                wantTimeUTC = true;
                return Casts.STRING_ONLY;

            default:
                return null;
        }
    }

    // --------------------------------------------

    @SuppressWarnings("ConstantConditions")
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
            || wantMillisecond
            || wantDate
            || wantTime;

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
            || wantMillisecondUTC
            || wantDateUTC
            || wantTimeUTC;
    }

    // --------------------------------------------

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss");

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);
        dissect(field, parsable, inputname);
    }

    protected void dissect(ParsedField field, final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        fieldValue = fieldValue.toLowerCase(Locale.getDefault());

        if (wantAnyAsParsed || wantAnyTZIndependent) {
            // YUCK ! Parsing the same thing TWICE just for the Zone ?!?!?

            DateTime dateTime;
            try {
                dateTime = asParsedFormatter.parseDateTime(fieldValue);
            } catch (IllegalArgumentException iae) {
                throw new DissectionFailure(iae.getMessage(), iae);
            }

            DateTimeZone zone = dateTime.getZone();
            DateTimeFormatter asParsedWithZoneFormatter = asParsedFormatter.withZone(zone);
            dateTime = asParsedWithZoneFormatter.parseDateTime(fieldValue);

            // As parsed
            if (wantDay) {
                parsable.addDissection(inputname, "TIME.DAY", "day",
                        dateTime.dayOfMonth().get());
            }
            if (wantMonthname) {
                parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname",
                        dateTime.monthOfYear().getAsText(Locale.getDefault()));
            }
            if (wantMonth) {
                parsable.addDissection(inputname, "TIME.MONTH", "month",
                        dateTime.monthOfYear().get());
            }
            if (wantWeekOfWeekYear) {
                parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear",
                        dateTime.weekOfWeekyear().get());
            }
            if (wantWeekYear) {
                parsable.addDissection(inputname, "TIME.YEAR", "weekyear",
                        dateTime.weekyear().get());
            }
            if (wantYear) {
                parsable.addDissection(inputname, "TIME.YEAR", "year",
                        dateTime.year().get());
            }
            if (wantHour) {
                parsable.addDissection(inputname, "TIME.HOUR", "hour",
                        dateTime.hourOfDay().get());
            }
            if (wantMinute) {
                parsable.addDissection(inputname, "TIME.MINUTE", "minute",
                        dateTime.minuteOfHour().get());
            }
            if (wantSecond) {
                parsable.addDissection(inputname, "TIME.SECOND", "second",
                        dateTime.secondOfMinute().get());
            }
            if (wantMillisecond) {
                parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond",
                        dateTime.millisOfSecond().get());
            }
            if (wantDate) {
                parsable.addDissection(inputname, "TIME.DATE", "date",
                        ISO_DATE_FORMATTER.print(dateTime));
            }

            if (wantTime) {
                parsable.addDissection(inputname, "TIME.TIME", "time",
                    ISO_TIME_FORMATTER.print(dateTime));
            }

            // Timezone independent
            if (wantTimezone) {
                parsable.addDissection(inputname, "TIME.TIMEZONE", "timezone",
                        dateTime.getZone().getID());
            }
            if (wantEpoch) {
                parsable.addDissection(inputname, "TIME.EPOCH", "epoch",
                        dateTime.getMillis());
            }
        }

        if (wantAnyUTC) {
            // In UTC timezone
            DateTime dateTime;
            try {
                dateTime = formatter.parseDateTime(fieldValue);
            } catch (IllegalArgumentException iae) {
                throw new DissectionFailure(iae.getMessage(), iae);
            }

            if (wantDayUTC) {
                parsable.addDissection(inputname, "TIME.DAY", "day_utc",
                        dateTime.dayOfMonth().get());
            }
            if (wantMonthnameUTC) {
                parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname_utc",
                        dateTime.monthOfYear().getAsText(Locale.getDefault()));
            }
            if (wantMonthUTC) {
                parsable.addDissection(inputname, "TIME.MONTH", "month_utc",
                        dateTime.monthOfYear().get());
            }
            if (wantWeekOfWeekYearUTC) {
                parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear_utc",
                        dateTime.weekOfWeekyear().get());
            }
            if (wantWeekYearUTC) {
                parsable.addDissection(inputname, "TIME.YEAR", "weekyear_utc",
                        dateTime.weekyear().get());
            }
            if (wantYearUTC) {
                parsable.addDissection(inputname, "TIME.YEAR", "year_utc",
                        dateTime.year().get());
            }
            if (wantHourUTC) {
                parsable.addDissection(inputname, "TIME.HOUR", "hour_utc",
                        dateTime.hourOfDay().get());
            }
            if (wantMinuteUTC) {
                parsable.addDissection(inputname, "TIME.MINUTE", "minute_utc",
                        dateTime.minuteOfHour().get());
            }
            if (wantSecondUTC) {
                parsable.addDissection(inputname, "TIME.SECOND", "second_utc",
                        dateTime.secondOfMinute().get());
            }
            if (wantMillisecondUTC) {
                parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond_utc",
                        dateTime.millisOfSecond().get());
            }
            if (wantDateUTC) {
                parsable.addDissection(inputname, "TIME.DATE", "date_utc",
                    ISO_DATE_FORMATTER.print(dateTime));
            }

            if (wantTimeUTC) {
                parsable.addDissection(inputname, "TIME.TIME", "time_utc",
                    ISO_TIME_FORMATTER.print(dateTime));
            }

        }
    }

    // --------------------------------------------

}
