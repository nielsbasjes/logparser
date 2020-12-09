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

grammar StrfTime;

// Comments copied from the strftime man 3 page.
// See: http://man7.org/linux/man-pages/man3/strftime.3.html


// %E    Modifier: use alternative format, see below.
// %O    Modifier: use alternative format, see below.

// Some conversion specifications can be modified by preceding the
// conversion specifier character by the E or O modifier to indicate
// that an alternative format should be used.  If the alternative format
// or specification does not exist for the current locale, the behavior
// will be as if the unmodified conversion specification were used.
// The Single UNIX Specification mentions %Ec, %EC, %Ex, %EX, %Ey, %EY,
// %Od, %Oe, %OH, %OI, %Om, %OM, %OS, %Ou, %OU, %OV, %Ow, %OW, %Oy,
// where the effect of the O modifier is to use alternative numeric
// symbols (say, roman numerals), and that of the E modifier is to use a
// locale-dependent alternative representation.

// We are simply ignoring all of these modifiers

fragment MOD: ( 'E' | 'O' )?;

MsecFrac : '%'? 'msec_frac' ; // Apache HTTPD specific: milliseconds fraction
UsecFrac : '%'? 'usec_frac' ; // Apache HTTPD specific: microseconds fraction

Pa : '%' MOD 'a' ; // The abbreviated name of the day of the week according to the current locale.
PA : '%' MOD 'A' ; // The full name of the day of the week according to the current locale.
Pb : '%' MOD 'b' ; // The abbreviated month name according to the current locale.
Ph : '%' MOD 'h' ; // Equivalent to %b.
PB : '%' MOD 'B' ; // The full month name according to the current locale.
Pc : '%' MOD 'c' ; // The preferred date and time representation for the current locale.
PC : '%' MOD 'C' ; // The century number (year/100) as a 2-digit integer.
Pd : '%' MOD 'd' ; // The day of the month as a decimal number (range 01 to 31).
PD : '%' MOD 'D' ; // Equivalent to %m/%d/%y. (Yecch—for Americans only)
Pe : '%' MOD 'e' ; // Like %d, the day of the month as a decimal number, but a leading zero is replaced by a space.
PF : '%' MOD 'F' ; // Equivalent to %Y-%m-%d (the ISO 8601 date format).
PG : '%' MOD 'G' ; // The ISO 8601 week-based year (see NOTES) with century as a decimal number. The 4-digit year corresponding to the ISO week number (see %V). This has the same format and value as %Y, except that if the ISO week number belongs to the previous or next year, that year is used instead.
Pg : '%' MOD 'g' ; // Like %G, but without century, that is, with a 2-digit year (00–99).
PH : '%' MOD 'H' ; // The hour as a decimal number using a 24-hour clock (range 00 to 23).
PI : '%' MOD 'I' ; // The hour as a decimal number using a 12-hour clock (range 01 to 12).
Pj : '%' MOD 'j' ; // The day of the year as a decimal number (range 001 to 366).
Pk : '%' MOD 'k' ; // The hour (24-hour clock) as a decimal number (range 0 to 23); single digits are preceded by a blank. (See also %H)
Pl : '%' MOD 'l' ; // The hour (12-hour clock) as a decimal number (range 1 to 12); single digits are preceded by a blank. (See also %I)
Pm : '%' MOD 'm' ; // The month as a decimal number (range 01 to 12).
PM : '%' MOD 'M' ; // The minute as a decimal number (range 00 to 59).
Pp : '%' MOD 'p' ; // Either "AM" or "PM" according to the given time value, or the corresponding strings for the current locale. Noon is treated as "PM" and midnight as "AM".
PP : '%' MOD 'P' ; // Like %p but in lowercase: "am" or "pm" or a corresponding string for the current locale.
Pr : '%' MOD 'r' ; // The time in a.m. or p.m. notation. In the POSIX locale this is equivalent to %I:%M:%S %p.
PR : '%' MOD 'R' ; // The time in 24-hour notation (%H:%M). For a version including the seconds, see %T below.
Ps : '%' MOD 's' ; // The number of seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC).
PS : '%' MOD 'S' ; // The second as a decimal number (range 00 to 60). (The range is up to 60 to allow for occasional leap seconds)
PT : '%' MOD 'T' ; // The time in 24-hour notation (%H:%M:%S).
Pu : '%' MOD 'u' ; // The day of the week as a decimal, range 1 to 7, Monday being 1. See also %w.
PU : '%' MOD 'U' ; // The week number of the current year as a decimal number, range 00 to 53, starting with the first Sunday as the first day of week 01. See also %V and %W.
PV : '%' MOD 'V' ; // The ISO 8601 week number (see NOTES) of the current year as a decimal number, range 01 to 53, where week 1 is the first week that has at least 4 days in the new year. See also %U and %W.
Pw : '%' MOD 'w' ; // The day of the week as a decimal, range 0 to 6, Sunday being 0. See also %u.
PW : '%' MOD 'W' ; // The week number of the current year as a decimal number, range 00 to 53, starting with the first Monday as the first day of week 01.
Px : '%' MOD 'x' ; // The preferred date representation for the current locale without the time.
PX : '%' MOD 'X' ; // The preferred time representation for the current locale without the date.
Py : '%' MOD 'y' ; // The year as a decimal number without a century (range 00 to 99).
PY : '%' MOD 'Y' ; // The year as a decimal number including the century.
Pz : '%' MOD 'z' ; // The +hhmm or -hhmm numeric timezone.
PZ : '%' MOD 'Z' ; // The timezone name or abbreviation.
Pplus : '%' MOD '+' ; // The date and time in date(1) format.

Pt : '%t' ; // A tab character.
Pn : '%n' ; // A newline character.
PERCENT : '%%' ; // A literal '%' character.

LITERAL : ~('%');

pattern
    : (literal|token)+
    ;

literal
    : text
    | tab
    | percent
    | newline
    ;

text: LITERAL
    ;

tab : Pt
    ;

percent
    : PERCENT
    ;

newline
    : Pn
    ;

token
    : pa | pA | pb | pB | pc | pC | pd | pD | pe | pF
    | pG | pg | pH | pI | pj | pk | pl | pm | pM
    | pp | pP | pr | pR | ps | pS | pT | pu | pU | pV
    | pw | pW | px | pX | py | pY | pz | pZ | pplus
    | msecFrac | usecFrac ;

msecFrac : MsecFrac ; // Apache HTTPD specific: milliseconds fraction
usecFrac : UsecFrac ; // Apache HTTPD specific: microseconds fraction

pa    : Pa    ;
pA    : PA    ;
pb    : Pb | Ph ;
pB    : PB    ;
pc    : Pc    ;
pC    : PC    ;
pd    : Pd    ;
pD    : PD    ;
pe    : Pe    ;
pF    : PF    ;
pG    : PG    ;
pg    : Pg    ;

pH    : PH    ;
pI    : PI    ;
pj    : Pj    ;
pk    : Pk    ;
pl    : Pl    ;
pm    : Pm    ;
pM    : PM    ;
pp    : Pp    ;
pP    : PP    ;
pr    : Pr    ;
pR    : PR    ;
ps    : Ps    ;
pS    : PS    ;
pT    : PT    ;
pu    : Pu    ;
pU    : PU    ;
pV    : PV    ;
pw    : Pw    ;
pW    : PW    ;
px    : Px    ;
pX    : PX    ;
py    : Py    ;
pY    : PY    ;
pz    : Pz    ;
pZ    : PZ    ;
pplus : Pplus ;
