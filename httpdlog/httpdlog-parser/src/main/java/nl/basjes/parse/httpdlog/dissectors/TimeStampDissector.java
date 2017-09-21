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
package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class TimeStampDissector extends Dissector {

    // The default parser to what we find in the Apache httpd Logfiles
    //                                                            [05/Sep/2010:11:27:50 +0200]
    public static final String DEFAULT_APACHE_DATE_TIME_PATTERN = "dd/MMM/yyyy:HH:mm:ss ZZ";

    // --------------------------------------------

    private transient DateTimeFormatter formatter;
    private String dateTimePattern;

    @SuppressWarnings("UnusedDeclaration")
    public TimeStampDissector() {
        this(DEFAULT_APACHE_DATE_TIME_PATTERN);
    }

    public TimeStampDissector(String newDateTimePattern) {
        this("TIME.STAMP", newDateTimePattern);
    }

    public TimeStampDissector(String inputType, String newDateTimePattern) {
        setInputType(inputType);
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
        formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern(dateTimePattern)
            .toFormatter()
            .withLocale(Locale.getDefault());
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        TimeStampDissector newTimeStampDissector = (TimeStampDissector) newInstance;
        newTimeStampDissector.setInputType(getInputType());
        newTimeStampDissector.setDateTimePattern(dateTimePattern);
    }

    // --------------------------------------------

    private String inputType = "TIME.STAMP";

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public final void setInputType(String nInputType) {
        inputType = nInputType;
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

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(getInputType(), inputname);
        dissect(field, parsable, inputname);
    }

    private static final TemporalField WEEK_OF_WEEK_BASED_YEAR = WeekFields.ISO.weekOfWeekBasedYear();
    private static final TemporalField YEAR_OF_WEEK_BASED_YEAR = WeekFields.ISO.weekBasedYear();

    protected void dissect(ParsedField field, final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

//        fieldValue = fieldValue.toLowerCase(Locale.getDefault());

        if (wantAnyAsParsed || wantAnyTZIndependent) {
            // YUCK ! Parsing the same thing TWICE just for the Zone ?!?!?

            ZonedDateTime dateTime;
            try {
                dateTime = formatter.parse(fieldValue, ZonedDateTime::from);
            } catch (DateTimeParseException dtpe) {
                throw new DissectionFailure(dtpe.getMessage()+
                    "\n123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_" +
                    "\n"+fieldValue+"\n"+dateTimePattern, dtpe);
            }

            ZoneId zone = dateTime.getZone();
            DateTimeFormatter asParsedWithZoneFormatter = formatter.withZone(zone);
            dateTime = asParsedWithZoneFormatter.parse(fieldValue, ZonedDateTime::from);

            // As parsed
            if (wantDay) {
                parsable.addDissection(inputname, "TIME.DAY", "day",
                        dateTime.getDayOfMonth());
            }
            if (wantMonthname) {
                parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname",
                        dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
            }
            if (wantMonth) {
                parsable.addDissection(inputname, "TIME.MONTH", "month",
                        dateTime.getMonth().getValue());
            }
            if (wantWeekOfWeekYear) {
                parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear",
                        dateTime.get(WEEK_OF_WEEK_BASED_YEAR));
            }
            if (wantWeekYear) {
                parsable.addDissection(inputname, "TIME.YEAR", "weekyear",
                        dateTime.get(YEAR_OF_WEEK_BASED_YEAR));
            }
            if (wantYear) {
                parsable.addDissection(inputname, "TIME.YEAR", "year",
                        dateTime.getYear());
            }
            if (wantHour) {
                parsable.addDissection(inputname, "TIME.HOUR", "hour",
                        dateTime.getHour());
            }
            if (wantMinute) {
                parsable.addDissection(inputname, "TIME.MINUTE", "minute",
                        dateTime.getMinute());
            }
            if (wantSecond) {
                parsable.addDissection(inputname, "TIME.SECOND", "second",
                        dateTime.getSecond());
            }
            if (wantMillisecond) {
                parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond",
                        dateTime.getNano() * 1000000);
            }
            if (wantDate) {
                parsable.addDissection(inputname, "TIME.DATE", "date",
                    dateTime.format(ISO_DATE_FORMATTER));
            }

            if (wantTime) {
                parsable.addDissection(inputname, "TIME.TIME", "time",
                    dateTime.format(ISO_TIME_FORMATTER));
            }

            // Timezone independent
            if (wantTimezone) {
                parsable.addDissection(inputname, "TIME.TIMEZONE", "timezone",
                        dateTime.getZone().getDisplayName(TextStyle.FULL, Locale.getDefault()));
            }
            if (wantEpoch) {
                parsable.addDissection(inputname, "TIME.EPOCH", "epoch",
                    (dateTime.toEpochSecond() * 1000) + (dateTime.getNano() / 1000000));
            }
        }

        if (wantAnyUTC) {
            // In UTC timezone
            ZonedDateTime dateTime;
            try {
                dateTime = formatter.parse(fieldValue, OffsetDateTime::from).atZoneSameInstant(ZoneOffset.UTC);
            } catch (IllegalArgumentException iae) {
                throw new DissectionFailure(iae.getMessage()+
                    "\n123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_" +
                    "\n"+fieldValue+"\n"+dateTimePattern, iae);
            }

            if (wantDayUTC) {
                parsable.addDissection(inputname, "TIME.DAY", "day_utc",
                        dateTime.getDayOfMonth());
            }
            if (wantMonthnameUTC) {
                parsable.addDissection(inputname, "TIME.MONTHNAME", "monthname_utc",
                        dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
            }
            if (wantMonthUTC) {
                parsable.addDissection(inputname, "TIME.MONTH", "month_utc",
                        dateTime.getMonthValue());
            }
            if (wantWeekOfWeekYearUTC) {
                parsable.addDissection(inputname, "TIME.WEEK", "weekofweekyear_utc",
                        dateTime.get(WEEK_OF_WEEK_BASED_YEAR));
            }
            if (wantWeekYearUTC) {
                parsable.addDissection(inputname, "TIME.YEAR", "weekyear_utc",
                        dateTime.get(YEAR_OF_WEEK_BASED_YEAR));
            }
            if (wantYearUTC) {
                parsable.addDissection(inputname, "TIME.YEAR", "year_utc",
                        dateTime.getYear());
            }
            if (wantHourUTC) {
                parsable.addDissection(inputname, "TIME.HOUR", "hour_utc",
                        dateTime.getHour());
            }
            if (wantMinuteUTC) {
                parsable.addDissection(inputname, "TIME.MINUTE", "minute_utc",
                        dateTime.getMinute());
            }
            if (wantSecondUTC) {
                parsable.addDissection(inputname, "TIME.SECOND", "second_utc",
                        dateTime.getSecond());
            }
            if (wantMillisecondUTC) {
                parsable.addDissection(inputname, "TIME.MILLISECOND", "millisecond_utc",
                        dateTime.getNano() * 1000000);
            }
            if (wantDateUTC) {
                parsable.addDissection(inputname, "TIME.DATE", "date_utc",
                    dateTime.format(ISO_DATE_FORMATTER));
            }

            if (wantTimeUTC) {
                parsable.addDissection(inputname, "TIME.TIME", "time_utc",
                    dateTime.format(ISO_TIME_FORMATTER));
            }

        }
    }

    // --------------------------------------------

}
