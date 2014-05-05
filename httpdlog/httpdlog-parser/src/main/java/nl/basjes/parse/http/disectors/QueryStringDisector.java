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

import nl.basjes.parse.core.Disector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;

public class QueryStringDisector extends Disector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.URI";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public String[] getPossibleOutput() {
        String[] result = new String[1];
        result[0] = "HTTP.QUERYSTRING:query";
        return result;
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

    @Override
    public void disect(final Parsable<?> parsable, final String inputname) {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        int questionMark   = fieldValue.indexOf('?');
        int firstAmpersand = fieldValue.indexOf('&');

        String resultingValue;
        // Now we can have one of 3 situations:
        // 1) No query string
        // 2) Query string starts with a ? (and optionally followed by one or
        // more &)
        // 3) Query string starts with a &. This is invalid but does occur!
        if (questionMark == -1) {
            if (firstAmpersand == -1) {
                resultingValue = ""; // We do not have anything.
            } else {
                resultingValue = "&"+fieldValue.substring(firstAmpersand, fieldValue.length());
            }
        } else if (firstAmpersand == -1) {
            // Replace the ? with a & to make parsing later easier
            resultingValue = "&"+fieldValue.substring(questionMark+1, fieldValue.length());
        } else {
            // We have both. So we take the first one.
            int usedOffset = Math.min(questionMark, firstAmpersand) + 1;
            resultingValue = "&"+fieldValue.substring(usedOffset, fieldValue.length());
        }


        parsable.addDisection(inputname, "HTTP.QUERYSTRING", "query", resultingValue);
    }
    // --------------------------------------------

}
