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

package nl.basjes.parse.http.disectors;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DisectionFailure;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpFirstLineDisector extends Disector {
    // --------------------------------------------
    // The "first line" of a request can be split up a bit further
    // The format of the first line of an HTTP/1.x request looks like this:
    private final Pattern firstlineSplitter = Pattern
            .compile("^(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT) (.*) (HTTP)/(.*)$");

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
        result.add("HTTP.PROTOCOL:protocol");
        result.add("HTTP.PROTOCOL.VERSION:protocol.version");
        return result;
    }

    // --------------------------------------------

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) throws DisectionFailure {
        final ParsedField field = parsable.getParsableField(HTTP_FIRSTLINE, inputname);

        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        // Now we create a matcher for this line
        final Matcher matcher = firstlineSplitter.matcher(fieldValue);

        // Is it all as expected?
        final boolean matches = matcher.find();

        if (matches && matcher.groupCount() == 4) {
            outputDisection(parsable, inputname, "HTTP.METHOD",           "method",           matcher, 1);
            outputDisection(parsable, inputname, "HTTP.URI",              "uri",              matcher, 2);
            outputDisection(parsable, inputname, "HTTP.PROTOCOL",         "protocol",         matcher, 3);
            outputDisection(parsable, inputname, "HTTP.PROTOCOL.VERSION", "protocol.version", matcher, 4);
        }
    }

    private void outputDisection(Parsable<?> parsable, String inputname, String type, String name, Matcher matcher, int offset) throws DisectionFailure {
        if (requestedParameters.contains(name)) {
            parsable.addDisection(inputname, type, name, matcher.group(offset));
        }
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Disector newInstance) {
        // Nothing to do
    }

    // --------------------------------------------

    private final Set<String> requestedParameters = new HashSet<>(16);

    @Override
    public EnumSet<Casts> prepareForDisect(final String inputname, final String outputname) {
        requestedParameters.add(outputname.substring(inputname.length() + 1));
        return Casts.STRING_ONLY;
    }

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

}
