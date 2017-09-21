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
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

public class StrfTimeStampDissector extends Dissector {

    private static final Logger LOG = LoggerFactory.getLogger(StrfTimeStampDissector.class);

    List<TimeStampDissector> timeStampDissectors = new ArrayList<>(8);
    String dateTimePattern = null;
    private String inputType = "TIME.?????";

    public StrfTimeStampDissector() {
    }

    public void setDateTimePattern(String newDateTimePattern) throws InvalidDissectorException {
        if (newDateTimePattern == null) {
            this.dateTimePattern = null;
            timeStampDissectors.clear();
            return; // Done
        }

        if (newDateTimePattern.equals(dateTimePattern)) {
            return; // Nothing to do
        }

        this.dateTimePattern = newDateTimePattern;
        timeStampDissectors.clear(); // Erase all previous dissectors
        for (String jodaPattern: convertStrfTimeToTimeFormat(newDateTimePattern)) {
            timeStampDissectors.add(new TimeStampDissector(jodaPattern));
        }
    }

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        try {
            setDateTimePattern(settings);
        } catch (InvalidDissectorException e) {
            LOG.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(inputType, inputname);

        DissectionFailure exception = null;
        for (TimeStampDissector timeStampDissector: timeStampDissectors) {
            try {
                timeStampDissector.dissect(field, parsable, inputname);
                return; // If we get here it worked and we can stop trying the other variants.
            } catch (DissectionFailure df) {
                exception = df;
            }
        }
        // Only throw if all variants failed.
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public List<String> getPossibleOutput() {
        return timeStampDissectors.get(0).getPossibleOutput();
    }

    @Override
    public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
        EnumSet<Casts> result = Casts.STRING_ONLY;
        for (Dissector dissector: timeStampDissectors) {
            result = dissector.prepareForDissect(inputname, outputname);
        }
        return result;
    }

    @Override
    public void prepareForRun() throws InvalidDissectorException {
        for (Dissector dissector: timeStampDissectors) {
            dissector.prepareForRun();
        }
    }

    @Override
    protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
        StrfTimeStampDissector newStrfTimeStampDissector = (StrfTimeStampDissector) newInstance;
        newStrfTimeStampDissector.setInputType(getInputType());
        newStrfTimeStampDissector.setDateTimePattern(dateTimePattern);
    }

    @Override
    public void setInputType(String newInputType) {
        inputType = newInputType;
    }

    List<String> convertStrfTimeToTimeFormat(String strftime) throws InvalidDissectorException {
        List<String> resultSet = new ArrayList<>();

        // In some cases these may be still in there
        strftime = strftime
            .replaceAll("begin:", "")
            .replaceAll("end:", "");

        resultSet.add(strftime);

        // Translating the strftime format into something java.time should understand.
        // Many fields can be translated. The ones that cannot will simply cause an exception.

        // In somecases we have multiple possible mappings (leading spaces problem) so we have a List<>
        // of all datetime strings to contain all possible permutations (usually only 1, sometimes more).

        // Comments copied from the strftime man 3 page.
        // See: http://man7.org/linux/man-pages/man3/strftime.3.html

        // CHECKSTYLE.OFF: LineLength
        // ======================================================================================================
        // 1) Handle the special cases
        resultSet = mapStrftimeToJavaTime(resultSet, "%%", "'%'");          // A literal '%' character.
        resultSet = mapStrftimeToJavaTime(resultSet, "%n", "'\n'");         // A newline character.
        resultSet = mapStrftimeToJavaTime(resultSet, "%t", "'\t'");         // A tab character.

        // ======================================================================================================
        // 2) Handle the modifiers (that we simply ignore)
        resultSet = mapStrftimeToJavaTime(resultSet, "%E", "%");            // Modifier: use alternative format, see below.
        resultSet = mapStrftimeToJavaTime(resultSet, "%O", "%");            // Modifier: use alternative format, see below.

        // ======================================================================================================
        // 3) Rewrite the shorthand cases to the full form
        resultSet = mapStrftimeToJavaTime(resultSet, "%D", "%m/%d/%y");     // Equivalent to %m/%d/%y. (Yecch—for Americans only.
        resultSet = mapStrftimeToJavaTime(resultSet, "%F", "%Y-%m-%d");     // Equivalent to %Y-%m-%d (the ISO 8601 date format). (C99)
        resultSet = mapStrftimeToJavaTime(resultSet, "%R", "%H:%M");        // The time in 24-hour notation (%H:%M).
        resultSet = mapStrftimeToJavaTime(resultSet, "%T", "%H:%M:%S");     // The time in 24-hour notation (%H:%M:%S).
        resultSet = mapStrftimeToJavaTime(resultSet, "%r", "%I:%M:%S %p");  // The time in a.m. or p.m. notation. In the POSIX locale equivalent to %I:%M:%S %p.

        // ======================================================================================================
        // 4) Now quote all fixed strings
        List<String> quotedResultSet = new ArrayList<>();
        for (String result: resultSet) {
            result = result.replaceAll("(%.)", "'$1'");                     // Quote all

            result = result.replaceAll("msec_frac", "'SSS'");    // Apache HTTPD specific: milliseconds fraction
//            result = result.replaceAll("usec_frac", "'SSSSSSS'");  // Apache HTTPD specific: microseconds fraction

            result = result.replaceAll("''", "");                           // Remove the quotes between two adjacent
            result = result.replaceAll("^'(%.)", "$1");                     // Remove the quote at the front IFF first is field
            result = result.replaceAll("(%.)'$", "$1");                     // Remove the quote at the end   IFF last  is field
            quotedResultSet.add(result);
        }
        resultSet = quotedResultSet;

        // ======================================================================================================
        // 5) Now fail if we have fields that are not supported (yet)
        resultSet = mapStrftimeToJavaTime(resultSet, "%s");                 // The number of seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC).
        resultSet = mapStrftimeToJavaTime(resultSet, "%P");                 // Like %p but in lowercase: "am" or "pm" or a corresponding string for the current locale.
        resultSet = mapStrftimeToJavaTime(resultSet, "%w");                 // The day of the week as a decimal, range 0 to 6, Sunday being 0. See also %u.
        resultSet = mapStrftimeToJavaTime(resultSet, "%U");                 // The week number of the current year as a decimal number, range 00 to 53, starting with the first Sunday as the first day of week 01.
        resultSet = mapStrftimeToJavaTime(resultSet, "%V");                 // The ISO 8601 week number (see NOTES) of the current year as a decimal number, range 01 to 53, where week 1 is the first week that has at least 4 days in the new year. See also %U and %W.
        resultSet = mapStrftimeToJavaTime(resultSet, "%W");                 // The week number of the current year as a decimal number, range 00 to 53, starting with the first Monday as the first day of week 01.
        resultSet = mapStrftimeToJavaTime(resultSet, "%c");                 // The preferred date and time representation for the current locale.
        resultSet = mapStrftimeToJavaTime(resultSet, "%x");                 // The preferred date representation for the current locale without the time.
        resultSet = mapStrftimeToJavaTime(resultSet, "%X");                 // The preferred time representation for the current locale without the date.
        resultSet = mapStrftimeToJavaTime(resultSet, "%Z");                 // The timezone name or abbreviation.
        resultSet = mapStrftimeToJavaTime(resultSet, "%+");                 // The date and time in date(1) format. (Not supported in glibc2.)
        resultSet = mapStrftimeToJavaTime(resultSet, "%C");                 // The century number (year/100) as a 2-digit integer.

        // ======================================================================================================
        // 6) Replace the field with the DateTimeFormatter equivalent if possible
        resultSet = mapStrftimeToJavaTime(resultSet, "%a", "EEE");          // The abbreviated name of the day of the week according to the current locale.
        resultSet = mapStrftimeToJavaTime(resultSet, "%A", "EEEE");         // The full name of the day of the week according to the current locale.
        resultSet = mapStrftimeToJavaTime(resultSet, "%b", "MMM");          // The abbreviated month name according to the current locale.
        resultSet = mapStrftimeToJavaTime(resultSet, "%B", "MMMM");         // The full month name according to the current locale.
        resultSet = mapStrftimeToJavaTime(resultSet, "%d", "dd");           // The day of the month as a decimal number (range 01 to 31).
        resultSet = mapStrftimeToJavaTime(resultSet, "%G", "YYYY");         // The ISO 8601 week-based year (see NOTES) with century as a decimal number.
                                                                            // The 4-digit year corresponding to the ISO week number (see %V).
                                                                            // Same as %Y except that if the ISO week number belongs to the previous or next year,
                                                                            // that year is used instead.
        resultSet = mapStrftimeToJavaTime(resultSet, "%g", "xx");           // Like %G, but without century, that is, with a 2-digit year (00-99).
        resultSet = mapStrftimeToJavaTime(resultSet, "%h", "MMM");          // Equivalent to %b.
        resultSet = mapStrftimeToJavaTime(resultSet, "%H", "HH");           // The hour as a decimal number using a 24-hour clock (range 00 to 23).
        resultSet = mapStrftimeToJavaTime(resultSet, "%I", "hh");           // The hour as a decimal number using a 12-hour clock (range 01 to 12).
        resultSet = mapStrftimeToJavaTime(resultSet, "%j", "DDD");          // The day of the year as a decimal number (range 001 to 366).
        resultSet = mapStrftimeToJavaTime(resultSet, "%m", "MM");           // The month as a decimal number (range 01 to 12).
        resultSet = mapStrftimeToJavaTime(resultSet, "%M", "mm");           // The minute as a decimal number (range 00 to 59).
        resultSet = mapStrftimeToJavaTime(resultSet, "%p", "a");            // Either "AM" or "PM" according to the given time value, or the corresponding
                                                                            // strings for the current locale. Noon is treated as "PM" and midnight as "AM".
        resultSet = mapStrftimeToJavaTime(resultSet, "%S", "ss");           // The second as a decimal number (range 00 to 60). (up to 60 for leap seconds.)

        // See: https://stackoverflow.com/questions/46341152/datetimeformatter-weekday-seems-off-by-one
        resultSet = mapStrftimeToJavaTime(resultSet, "%u", "ccccc");        // The day of the week as a decimal, range 1 to 7, Monday being 1. See also %w.

        resultSet = mapStrftimeToJavaTime(resultSet, "%Y", "yyyy");         // The year as a decimal number including the century. (Calculated from tm_year)
        resultSet = mapStrftimeToJavaTime(resultSet, "%y", "yy");           // %y  The year as a decimal number without a century (range 00 to 99).
        resultSet = mapStrftimeToJavaTime(resultSet, "%z", "ZZ");           // The +hhmm or -hhmm numeric timezone (that is, the hour and minute offset from UTC).

        // ======================================================================================================
        // 7) Handle the cases where a value below 10 results in " 5" (leading space) which cannot be parsed by the DateTimeFormatter.
        resultSet = mapStrftimeToJavaTime(resultSet, "%k", "HH", " H");     // The hour (24-hour clock) as a decimal number (range 0 to 23); (See also %H.)
        resultSet = mapStrftimeToJavaTime(resultSet, "%l", "hh", " h");     // The hour (12-hour clock) as a decimal number (range 1 to 12); (See also %I.)
        resultSet = mapStrftimeToJavaTime(resultSet, "%e", "dd", " d");     // Like %d, the day of the month as a decimal number, (range 1 to 31)
        // CHECKSTYLE.ON: LineLength

        return resultSet;
    }

    private List<String> mapStrftimeToJavaTime(List<String> dateTimeFormats, String strftimeField, String... timeFields)
        throws InvalidDissectorException {
        List<String> result = new ArrayList<>(dateTimeFormats.size());
        for (String dateTimeFormat: dateTimeFormats) {
            if (dateTimeFormat.contains(strftimeField)) {
                String usedStrftimeField = strftimeField;

                if (timeFields.length == 0) {
                    throw new InvalidDissectorException("Unsupported strfime parameter \'" + strftimeField + "\' (cannot be mapped to jodatime).");
                }
                for (String timeField: timeFields) {
                    if (timeField.startsWith(" ")) {
                        usedStrftimeField = "'" + strftimeField + "'";
                        timeField = "'" + timeField + "'";
                        timeField = timeField.replaceAll(Pattern.quote("' "), " '");
                    }
                    result.add(dateTimeFormat.replaceAll(Pattern.quote(usedStrftimeField), timeField));
                }
            } else {
                result.add(dateTimeFormat); // Unmodified
            }
        }
        return result;
    }

    @Override
    public <RECORD> void createAdditionalDissectors(Parser<RECORD> parser) {
        parser.addDissector(new LocalizedTimeDissector(inputType));
    }

    public static class LocalizedTimeDissector extends Dissector {

        String inputType = null;

        public LocalizedTimeDissector() {
        }

        public LocalizedTimeDissector(String inputType) {
            this.inputType = inputType;
        }

        @Override
        public void setInputType(String newInputType) {
            inputType = newInputType;
        }

        @Override
        public boolean initializeFromSettingsParameter(String settings) {
            setInputType(settings);
            return true;
        }

        @Override
        public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
            final ParsedField field = parsable.getParsableField(inputType, inputname);
            parsable.addDissection(inputname, "TIME.LOCALIZEDSTRING", "", field.getValue());
        }

        @Override
        public String getInputType() {
            return inputType;
        }

        @Override
        public List<String> getPossibleOutput() {
            List<String> result = new ArrayList<>();
            result.add("TIME.LOCALIZEDSTRING:");
            return result;
        }

        @Override
        public EnumSet<Casts> prepareForDissect(String inputname, String outputname) {
            return Casts.STRING_ONLY;
        }

        @Override
        public void prepareForRun() throws InvalidDissectorException {
        }

        @Override
        protected void initializeNewInstance(Dissector newInstance) throws InvalidDissectorException {
            newInstance.setInputType(inputType);
        }
    }
}
