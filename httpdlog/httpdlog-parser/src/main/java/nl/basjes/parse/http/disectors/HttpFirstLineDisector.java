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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;

public class HttpFirstLineDisector implements Disector {
    // --------------------------------------------
    // The "first line" of a request can be split up a bit further
    // The format of the first line of an HTTP/1.x request looks like this:
    private final Pattern firstlineSplitter = Pattern
            .compile("^(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT) ([^ ]*) (HTTP)/(.*)$");

    // --------------------------------------------

    private static final String HTTP_FIRSTLINE = "HTTP.FIRSTLINE";
    @Override
    public String getInputType() {
        return HTTP_FIRSTLINE;
    }

    // --------------------------------------------

    @Override
    public String[] getPossibleOutput() {
        String[] result = new String[4];
        result[0] = "HTTP.METHOD:method";
        result[1] = "HTTP.URI:uri";
        result[2] = "HTTP.PROTOCOL:protocol";
        result[3] = "HTTP.PROTOCOL.VERSION:protocol.version";
        return result;
    }

    // --------------------------------------------

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) {
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
            parsable.addDisection(inputname, "HTTP.METHOD",           "method",           matcher.group(1));
            parsable.addDisection(inputname, "HTTP.URI",              "uri",              matcher.group(2));
            parsable.addDisection(inputname, "HTTP.PROTOCOL",         "protocol",         matcher.group(3));
            parsable.addDisection(inputname, "HTTP.PROTOCOL.VERSION", "protocol.version", matcher.group(4));
        }
    }

    @Override
    public void prepareForDisect(final String inputname, final String outputname) {
        // We do not do anything extra here
    }

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

}
