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
import nl.basjes.parse.core.exceptions.DisectionFailure;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpUriDisector extends Disector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.URI";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public String[] getPossibleOutput() {
        String[] result = new String[7];
        result[0] = "HTTP.PROTOCOL:protocol";
        result[1] = "HTTP.USERINFO:userinfo";
        result[2] = "HTTP.HOST:host";
        result[3] = "HTTP.PORT:port";
        result[4] = "HTTP.PATH:path";
        result[5] = "HTTP.QUERYSTRING:query";
        result[6] = "HTTP.REF:ref";
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
    public void disect(final Parsable<?> parsable, final String inputname) throws DisectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        final String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()){
            return; // Nothing to do here
        }

        URI uri;
        try {
            uri = new URI(fieldValue);
        } catch (URISyntaxException e) {
            throw new DisectionFailure("Unable to parse the URI: >>>" + fieldValue + "<<< (" + e.getMessage() + ")");
        }

        parsable.addDisection(inputname, "HTTP.PROTOCOL", "protocol", uri.getRawSchemeSpecificPart());
        parsable.addDisection(inputname, "HTTP.USERINFO", "userinfo", uri.getUserInfo());
        parsable.addDisection(inputname, "HTTP.HOST", "host", uri.getHost());
        if (uri.getPort() != -1) {
            parsable.addDisection(inputname, "HTTP.PORT", "port", String.valueOf(uri.getPort()));
        }
        parsable.addDisection(inputname, "HTTP.PATH", "path", uri.getRawPath());
        parsable.addDisection(inputname, "HTTP.QUERYSTRING", "query", uri.getRawQuery());
    }
    // --------------------------------------------

}
