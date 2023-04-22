/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.basjes.parse.core.Casts.STRING_ONLY;

public class HttpFirstLineDissector extends Dissector {
    // --------------------------------------------
    // The "first line" of a request can be split up a bit further
    // See for more details: http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
    // In https://tools.ietf.org/html/rfc7230#section-3.1.1 it says:
    //      Recipients typically parse the request-line into its component parts
    //      by splitting on whitespace (see Section 3.5), since no whitespace is
    //      allowed in the three components.  Unfortunately, some user agents
    //      fail to properly encode or exclude whitespace found in hypertext
    //      references, resulting in those disallowed characters being sent in a
    //      request-target.

    // So this means:
    // - Method = Single Word
    // - Request URI = String that can contain ANY letters
    // - HTTP version = HTTP/[0-9]+\.[0-9]+
    // The HTTP version has been made optional to allow parsing the log lines you get when the URI is > 8KB
    // In that scenario the HTTP/x.x part will not be logged at all.
    // In case of using mod_reqtimeout simply opening a connection and wait for the timeout without entering any data
    // results in an empty firstline. I.e. a "-"
    // The actual regex has been reduced to '.*' because we want to continue even if someone throws in complete garbage.
    public static final String FIRSTLINE_REGEX =
            ".*";

    private final Pattern firstlineSplitter = Pattern
            .compile("^([a-zA-Z-_]+) (.*) (HTTP/[0-9]+\\.[0-9]+)$");

    private final Pattern tooLongFirstlineSplitter = Pattern
            .compile("^([a-zA-Z-_]+) (.*)$");

    // --------------------------------------------

    private static final String HTTP_FIRSTLINE = "HTTP.FIRSTLINE";
    @Override
    public String getInputType() {
        return HTTP_FIRSTLINE;
    }

    // --------------------------------------------

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("HTTP.METHOD:method");
        result.add("HTTP.URI:uri");
        result.add("HTTP.PROTOCOL_VERSION:protocol");
        return result;
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(HTTP_FIRSTLINE, inputname);

        final String fieldValue = field.getValue().getString();
        if (fieldValue == null || fieldValue.isEmpty() || "-".equals(fieldValue)){
            return; // Nothing to do here
        }

        // Now we create a matcher for this line
        Matcher matcher = firstlineSplitter.matcher(fieldValue);

        // Is it all as expected?
        boolean matches = matcher.find();

        if (matches && matcher.groupCount() == 3) {
            outputDissection(parsable, inputname, "HTTP.METHOD", "method", matcher, 1);
            outputDissection(parsable, inputname, "HTTP.URI", "uri", matcher, 2);
            outputDissection(parsable, inputname, "HTTP.PROTOCOL_VERSION", "protocol", matcher, 3);
            return;
        }

        // In the scenario that the actual URI is too long the last part ("HTTP/1.1") may have been cut off by the
        // Apache HTTPD webserver. To still be able to parse these we try that pattern too

        // Now we create a matcher for this line
        matcher = tooLongFirstlineSplitter.matcher(fieldValue);

        // Is it all as expected?
        matches = matcher.find();

        if (matches && matcher.groupCount() == 2) {
            outputDissection(parsable, inputname, "HTTP.METHOD", "method", matcher, 1);
            outputDissection(parsable, inputname, "HTTP.URI", "uri", matcher, 2);
            parsable.addDissection(inputname, "HTTP.PROTOCOL_VERSION", "protocol", (String) null);
        }
    }

    private void outputDissection(Parsable<?> parsable,
                                  String inputname,
                                  String type,
                                  String name,
                                  Matcher matcher,
                                  int offset)
            throws DissectionFailure {
        if (requestedParameters.contains(name)) {
            parsable.addDissection(inputname, type, name, matcher.group(offset));
        }
    }

    // --------------------------------------------

    private final Set<String> requestedParameters = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        requestedParameters.add(extractFieldName(inputname, outputname));
        return STRING_ONLY;
    }

    // --------------------------------------------

}
