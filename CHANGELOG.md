This is intended as an overview of the major changes

v5.9-SNAPSHOT
===
- Print the parsed values of a wildcard in DissectorTester::printAllPossibleValues

v5.8
===
- getPossiblePaths sorts the list by fieldname.
- Removed GeoIP fields averageincome and populationdensity which are not part of any real mmdb file.
- Dropped the already disabled Storm example
- Fully switched to Junit 5
- Require JDK 11 or newer to build
- Workaround for change in Unicode CLDR(and thus Java 17): they changed the short name of "September" in Locale.UK to "Sept" which causes parse errors.
- Fixed bug regarding escaped characters in headers.

v5.7
===
- Updated dependencies
- When adding a type remapping with an explicit cast this cast was lost and replaced by "STRING_ONLY".

v5.6
===
- Fix bug that in some cases values would be reported multiple times.

v5.5
===
- Handle HTML encoded values in the URL better

v5.4
===
- Updated many dependencies
- Fixed extracting the timezone
- In url parsing missing values are now 'absent' (i.e. not set)
- Handle loglines when the upstream module is not running (i.e. fields are a '-')

v5.3
===
- Updated many dependencies

v5.2
===
- Improve regex performance
- Added basic support for the NGinx Upstream, SSL, GeoIP and all other documented modules
- Added basic support for parsing the Kubernetes Ingress logformat variables
- Disallow some Dissector methods to return a null.
- Updated many dependencies: Hadoop, Flink, Beam etc.

v5.1
===
- Parse epoch seconds `%{%s}t`
- Added GeoIP2 dissectors for City, Country and ASN data.
- Improved output of dissector testing framework.

v5.0
===
- The %u specifier allowed a space which broke parsing if the field after it also allowed space.
- If a custom time format does not contain a timezone we assume a default timezone (UTC).
- Fix extracting milliseconds, added extracting microseconds and nanoseconds.
- Allow `%{%usec_frac}t` in addition to `%{usec_frac}t` (same msec_frac).
- Introduce a setterPolicy to determine if the setter is called in case of a NULL or EMPTY value.
- Replace Cobertura with Jacoco because of Java 8
- Remove Yauaa from tests and examples because of circular dependency between the projects.
- Make Java API more fluent (breaks backwards compatibility with external dissectors).

v4.0
===
- Switching to require Java 8
- Parser instance is now serializable.
- Added example on using with Apache Flink
- Added example on using with Apache Beam
- Rewrote Apache Storm code and move to examples
- Many changes in Exception handling

v3.1
===
- Handle illegal data: Double # in the URL
- Handle illegal data: Firstline is rubbish (Reported by Yong Zhang - java8964).

v3.0
===
- Accept the NGinx logformat.
- Output the httpd parser git info, build timestamp and version info on startup.
- Fixed problem when using a different root dissector than provided by default.
- Created a testing toolkit for dissectors and improved several tests.
- Allow a dissector to add an additional dissector (needed for custom time format parsing)
- Support for parsing many (not all) of the possible custom time format fields.
- Allow changing the parser even after first use (makes it more flexible to use).
- Allow a dissector to return an empty 'extra part' to allow an alternate form of type remapping
- Improved test coverage
- Dissection the setcookies expire value was not deterministic and an absent 'expire' value is now returned as null instead of the 'now' timestamp.
- Implemented `%{UNIT}T`
- Implemented converters between several closely related formats (BYTES/BYTESCLF, time related formats)
- Token based parsers can output multiple values for the same parameter.
- Implemented all < and > directives for Apache logformat
- Implemented `%{VARNAME}^ti` and `%{VARNAME}^to`

**RELEVANT CHANGES COMPARED WITH THE 2.X VERSION**:
These fields are now reported as deprecated.
- `%b` changed from "BYTES:response.body.bytesclf" to "BYTESCLF:response.body.bytes"
- `%D` changed from "server.process.time" to "response.server.processing.time"
- `%{msec_frac}t` changed from "request.receive.time.begin.msec_frac" to "request.receive.time.msec_frac"
- `%{usec_frac}t` changed from "request.receive.time.begin.usec_frac" to "request.receive.time.usec_frac"
- `%{msec}t`      changed from "request.receive.time.begin.msec"      to "request.receive.time.msec"
- `%{usec}t`      changed from "request.receive.time.begin.usec"      to "request.receive.time.usec"

v2.8
===
- Allow parsing mixed case timeformats
- Added the ISO 8601 'date' output for parsed times (Is string "yyyy-MM-dd")
- Added the ISO 8601 'time' output for parsed times (Is string "HH:mm:ss"  )
- Solve parse error when a HTTP method like "VERSION-CONTROL" is used.
- Fixed NPE in specific combination of cast and setter type and a null value
- Improved test coverage

v2.7
===
- Handle the effects of mod_reqtimeout giving a http 408 (reported by Diogo Sant'Ana)
- Added option to optionally continue even when some requested dissectors are missing (Java only).

v2.6
===
- Buildin fix for the problems in Jetty logging

v2.5
===
- Simply treat the `%{...}t` as a text field you can retrieve (instead of failing if it occurs).
- Change URI dissector to allow URIs like android-app://...
- Change URI dissector to allow % in the URI when it is not an escape sequence (like in `?promo=Give-5%-discount`)

v2.4
===
- Rewrote the way parsed values are passed around. Improves accuracy and performance in specific cases.
- Now support parsing the first line even if it is chopped by Apache httpd because of an URI longer than 8000 bytes.
- Fixed an infinite recursion problem.
- Fixed Timestamp unit test (test was broken, code was fine).

v2.3
===
- The raw timestamp extracted from the Apache logfiles no longer contains the surrounding '[' ']'.

v2.2
===
- Accept multiple logformat lines as a single 'multiline' input string.

v2.1.1
===
- Fixed simple problem in the PIG example output

v2.1
===
- Dissect the unique ID from mod_unique_id.
- [PIG] Make getting the example code for PIG a bit easier

v2.0
===
- Fixed reading logfiles from before 2000
- Rearranged the Java packages to make the structure more logical.
- Changed license from GPL to Apache v2.0

v1.9.1
===
- Allow urls that have a space (which is incorrect but does happen).

v1.9
===
- [PIG] Support for getting a map[] of values (useful for cookies and query string parameters)
- [PIG] Output the possible values as a complete working Pig statement.

Older
===
Just see the commit logs


    Apache HTTPD & NGINX Access log parsing made easy
    Copyright (C) 2011-2021 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
