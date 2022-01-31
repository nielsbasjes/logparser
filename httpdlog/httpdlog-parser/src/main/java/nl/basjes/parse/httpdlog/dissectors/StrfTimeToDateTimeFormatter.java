/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.strftime.StrfTimeBaseListener;
import nl.basjes.parse.strftime.StrfTimeLexer;
import nl.basjes.parse.strftime.StrfTimeParser;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class StrfTimeToDateTimeFormatter extends StrfTimeBaseListener implements ANTLRErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(StrfTimeToDateTimeFormatter.class);

    private static final WeekFields LOCAL_WEEK_FIELDS = WeekFields.of(Locale.getDefault());

    public static DateTimeFormatter convert(String strfformat) {
        return convert(strfformat, ZoneOffset.UTC);
    }

    public static DateTimeFormatter convert(String strfformat, ZoneId defaultZone) {
        CodePointCharStream input = CharStreams.fromString(strfformat);
        StrfTimeLexer lexer = new StrfTimeLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        StrfTimeParser parser = new StrfTimeParser(tokens);

        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        StrfTimeToDateTimeFormatter converter = new StrfTimeToDateTimeFormatter(strfformat, defaultZone);

        lexer.addErrorListener(converter);
        parser.addErrorListener(converter);

        StrfTimeParser.PatternContext pattern = parser.pattern();

        walker.walk(converter, pattern); // initiate walk of tree with listener

        if (converter.hasSyntaxError()) {
            return null;
        }

        return converter.build();
    }

    private final String strfformat;
    private final DateTimeFormatterBuilder builder;
    private final ZoneId defaultZone;
    private boolean zoneWasSpecified = false;

    private StrfTimeToDateTimeFormatter(String inputStrfformat, ZoneId newDefaultZone) {
        strfformat = inputStrfformat;
        defaultZone = newDefaultZone;
        builder = new DateTimeFormatterBuilder()
            .parseCaseInsensitive();
    }

    public DateTimeFormatter build() {
        DateTimeFormatter dateTimeFormatter = builder.toFormatter();
        if (!zoneWasSpecified) {
            dateTimeFormatter = dateTimeFormatter.withZone(defaultZone);
            LOG.warn("The timestamp format \"{}\" does NOT contain a timezone so we assume \"{}\".",
                strfformat, defaultZone.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        return dateTimeFormatter;
    }

    // ------------- Error handling --------------
    private boolean syntaxError = false;

    public boolean hasSyntaxError() {
        return syntaxError;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        syntaxError = true;
    }

    @Override
    public void reportAmbiguity(org.antlr.v4.runtime.Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
        // Ignoring this Antlr4 event
    }

    @Override
    public void reportAttemptingFullContext(org.antlr.v4.runtime.Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
        // Ignoring this Antlr4 event
    }

    @Override
    public void reportContextSensitivity(org.antlr.v4.runtime.Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
        // Ignoring this Antlr4 event
    }

    public static class UnsupportedStrfField extends RuntimeException {
        public UnsupportedStrfField(String s) {
            super("The field '" + s + "' cannot be converted towards a DateTimeFormatter field.");
        }
    }

    // ------------- Mapping --------------

    @Override
    public void enterMsecFrac(StrfTimeParser.MsecFracContext ctx) {
        // Apache HTTPD specific: milliseconds fraction
        builder.appendValue(ChronoField.MILLI_OF_SECOND, 3);
    }

    @Override
    public void enterUsecFrac(StrfTimeParser.UsecFracContext ctx) {
        // Apache HTTPD specific: microseconds fraction
        builder.appendValue(ChronoField.MICRO_OF_SECOND, 6);
    }

    @Override
    public void enterText(StrfTimeParser.TextContext ctx) {
        builder.appendLiteral(ctx.getText());
    }

    @Override
    public void enterTab(StrfTimeParser.TabContext ctx) {
        builder.appendLiteral('\t');
    }

    @Override
    public void enterPercent(StrfTimeParser.PercentContext ctx) {
        builder.appendLiteral('%');
    }

    @Override
    public void enterNewline(StrfTimeParser.NewlineContext ctx) {
        builder.appendLiteral('\n');
    }

    @Override
    public void enterPa(StrfTimeParser.PaContext ctx) {
        // %a   The abbreviated name of the day of the week according to the current locale.
        builder.appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT);
    }

    @Override
    public void enterPA(StrfTimeParser.PAContext ctx) {
        // %A   The full name of the day of the week according to the current locale.
        builder.appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL);
    }

    @Override
    public void enterPb(StrfTimeParser.PbContext ctx) {
        // %b   The abbreviated month name according to the current locale.
        // %h   Equivalent to %b.
        builder.appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT);
    }

    @Override
    public void enterPB(StrfTimeParser.PBContext ctx) {
        // %B   The full month name according to the current locale.
        builder.appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL);
    }

    @Override
    public void enterPc(StrfTimeParser.PcContext ctx) {
        // %c   The preferred date and time representation for the current locale.
        throw new UnsupportedStrfField("%c   The preferred date and time representation for the current locale.");
    }

    @Override
    public void enterPC(StrfTimeParser.PCContext ctx) {
        throw new UnsupportedStrfField("%C   The century number (year/100) as a 2-digit integer.");
    }

    @Override
    public void enterPd(StrfTimeParser.PdContext ctx) {
        // %d   The day of the month as a decimal number (range 01 to 31).
        builder.appendValue(ChronoField.DAY_OF_MONTH, 2);
    }

    @Override
    public void enterPD(StrfTimeParser.PDContext ctx) {
        // %D   Equivalent to %m/%d/%y. (Yecch—for Americans only)
        builder
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000);
    }

    @Override
    public void enterPe(StrfTimeParser.PeContext ctx) {
        // %e   Like %d, the day of the month as a decimal number, but a leading zero is replaced by a space.
        builder.padNext(2, ' ').appendValue(ChronoField.DAY_OF_MONTH);
    }

    @Override
    public void enterPF(StrfTimeParser.PFContext ctx) {
        // %F   Equivalent to %Y-%m-%d (the ISO 8601 date format).
        builder
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2);
    }

    @Override
    public void enterPG(StrfTimeParser.PGContext ctx) {
        // %G   The ISO 8601 week-based year (see NOTES) with century as a decimal number.
        //      The 4-digit year corresponding to the ISO week number (see %V).
        //      This has the same format and value as %Y, except that if the ISO week number
        //      belongs to the previous or next year, that year is used instead.
        builder.appendValue(LOCAL_WEEK_FIELDS.weekBasedYear(), 4);
    }

    @Override
    public void enterPg(StrfTimeParser.PgContext ctx) {
        // %g   Like %G, but without century, that is, with a 2-digit year (00–99).
        builder.appendValueReduced(LOCAL_WEEK_FIELDS.weekBasedYear(), 2, 2, 2000);
    }

    @Override
    public void enterPH(StrfTimeParser.PHContext ctx) {
        // %H   The hour as a decimal number using a 24-hour clock (range 00 to 23).
        builder.appendValue(ChronoField.CLOCK_HOUR_OF_DAY, 2);
    }

    @Override
    public void enterPI(StrfTimeParser.PIContext ctx) {
        // %I   The hour as a decimal number using a 12-hour clock (range 01 to 12).
        builder.appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 2);
    }

    @Override
    public void enterPj(StrfTimeParser.PjContext ctx) {
        // %j   The day of the year as a decimal number (range 001 to 366).
        builder.appendValue(ChronoField.DAY_OF_YEAR, 3);
    }

    @Override
    public void enterPk(StrfTimeParser.PkContext ctx) {
        // %k   The hour (24-hour clock) as a decimal number (range 0 to 23); single digits are preceded by a blank.
        //      (See also %H)
        builder.padNext(2, ' ').appendValue(ChronoField.CLOCK_HOUR_OF_DAY);
    }

    @Override
    public void enterPl(StrfTimeParser.PlContext ctx) {
        // %l   The hour (12-hour clock) as a decimal number (range 1 to 12); single digits are preceded by a blank.
        //      (See also %I)
        builder.padNext(2, ' ').appendValue(ChronoField.CLOCK_HOUR_OF_AMPM);
    }

    @Override
    public void enterPm(StrfTimeParser.PmContext ctx) {
        // %m   The month as a decimal number (range 01 to 12).
        builder.appendValue(ChronoField.MONTH_OF_YEAR, 2);
    }

    @Override
    public void enterPM(StrfTimeParser.PMContext ctx) {
        // %M   The minute as a decimal number (range 00 to 59).
        builder.appendValue(ChronoField.MINUTE_OF_HOUR, 2);
    }

    @Override
    public void enterPp(StrfTimeParser.PpContext ctx) {
        // %p   Either "AM" or "PM" according to the given time value, or the corresponding strings for the current locale.
        // Noon is treated as "PM" and midnight as "AM".
        builder.appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT);
    }

    private static final Map<Long, String> AMPM_LOWER_CASE_MAPPING = new HashMap<>();
    static {
        AMPM_LOWER_CASE_MAPPING.put(0L, "am");
        AMPM_LOWER_CASE_MAPPING.put(1L, "pm");
    }

    @Override
    public void enterPP(StrfTimeParser.PPContext ctx) {
        // %P   Like %p but in lowercase: "am" or "pm" or a corresponding string for the current locale.
        builder.appendText(ChronoField.AMPM_OF_DAY, AMPM_LOWER_CASE_MAPPING);
    }

    @Override
    public void enterPr(StrfTimeParser.PrContext ctx) {
        // %r   The time in a.m. or p.m. notation. In the POSIX locale this is equivalent to %I:%M:%S %p.
        builder
            .appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral(' ')
            .appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT);
    }

    @Override
    public void enterPR(StrfTimeParser.PRContext ctx) {
        // %R   The time in 24-hour notation (%H:%M). For a version including the seconds, see %T below.
        builder
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2);
    }

    @Override
    public void enterPs(StrfTimeParser.PsContext ctx) {
        // %s   The number of seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC).
        // Based upon https://stackoverflow.com/questions/36066155/datetimeformatter-for-epoch-milliseconds#answer-36069732
        builder
            .appendValue(ChronoField.INSTANT_SECONDS, 1, 19, SignStyle.NEVER);
    }

    @Override
    public void enterPS(StrfTimeParser.PSContext ctx) {
        // %S   The second as a decimal number (range 00 to 60). (The range is up to 60 to allow for occasional leap seconds)
        builder
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2);
    }

    @Override
    public void enterPT(StrfTimeParser.PTContext ctx) {
        // %T   The time in 24-hour notation (%H:%M:%S).
        builder
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2);
    }

    @Override
    public void enterPu(StrfTimeParser.PuContext ctx) {
        // %u   The day of the week as a decimal, range 1 to 7, Monday being 1. See also %w.
        builder.appendValue(WeekFields.ISO.dayOfWeek(), 1);
    }

    @Override
    public void enterPU(StrfTimeParser.PUContext ctx) {
        // %U   The week number of the current year as a decimal number, range 00 to 53, starting with
        //      the first Sunday as the first day of week 01. See also %V and %W.
        throw new UnsupportedStrfField("%U The week number of the current year ... ");
    }

    @Override
    public void enterPV(StrfTimeParser.PVContext ctx) {
        // %V   The ISO 8601 week number (see NOTES) of the current year as a decimal number, range 01 to 53,
        // where week 1 is the first week that has at least 4 days in the new year. See also %U and %W.
        builder.appendValue(WeekFields.ISO.weekOfYear());
    }

    @Override
    public void enterPw(StrfTimeParser.PwContext ctx) {
        // %w   The day of the week as a decimal, range 0 to 6, Sunday being 0. See also %u.
        throw new UnsupportedStrfField("%w   The day of the week as a decimal, range 0 to 6, Sunday being 0. See also %u.");
    }

    @Override
    public void enterPW(StrfTimeParser.PWContext ctx) {
        // %W   The week number of the current year as a decimal number, range 00 to 53,
        //      starting with the first Monday as the first day of week 01.
        builder.appendValue(WeekFields.ISO.weekOfYear(), 2);
    }

    @Override
    public void enterPx(StrfTimeParser.PxContext ctx) {
        // %x   The preferred date representation for the current locale without the time.
        throw new UnsupportedStrfField("%x   The preferred date representation for the current locale without the time.");
    }

    @Override
    public void enterPX(StrfTimeParser.PXContext ctx) {
        // %X   The preferred time representation for the current locale without the date.
        throw new UnsupportedStrfField("%X   The preferred time representation for the current locale without the date.");
    }

    @Override
    public void enterPy(StrfTimeParser.PyContext ctx) {
        // %y   The year as a decimal number without a century (range 00 to 99).
        builder.appendValueReduced(ChronoField.YEAR, 2, 2, 2000);
    }

    @Override
    public void enterPY(StrfTimeParser.PYContext ctx) {
        // %Y   The year as a decimal number including the century.
        builder.appendValue(ChronoField.YEAR, 4);
    }

    @Override
    public void enterPz(StrfTimeParser.PzContext ctx) {
        // %z   The +hhmm or -hhmm numeric timezone.
        builder.appendOffset("+HHMM", "+0000");
        zoneWasSpecified = true;
    }

    @Override
    public void enterPZ(StrfTimeParser.PZContext ctx) {
        // %Z   The timezone name or abbreviation.
        builder.appendZoneText(TextStyle.SHORT);
        zoneWasSpecified = true;
    }

    @Override
    public void enterPplus(StrfTimeParser.PplusContext ctx) {
        throw new UnsupportedStrfField("%p   The date and time in date(1) format.");
    }

}
