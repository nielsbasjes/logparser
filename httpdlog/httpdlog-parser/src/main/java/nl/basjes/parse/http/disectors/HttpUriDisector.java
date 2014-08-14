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
import java.util.ArrayList;
import java.util.List;

public class HttpUriDisector extends Disector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.URI";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<String>();
        result.add("HTTP.PROTOCOL:protocol");
        result.add("HTTP.USERINFO:userinfo");
        result.add("HTTP.HOST:host");
        result.add("HTTP.PORT:port");
        result.add("HTTP.PATH:path");
        result.add("HTTP.QUERYSTRING:query");
        result.add("HTTP.REF:ref");
        return result;
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Disector newInstance) {
        // Nothing to do
    }

    private boolean wantProtocol = false;
    private boolean wantUserinfo = false;
    private boolean wantHost     = false;
    private boolean wantPort     = false;
    private boolean wantPath     = false;
    private boolean wantQuery    = false;
    private boolean wantRef      = false;

    @Override
    public void prepareForDisect(final String inputname, final String outputname) {
        String name = outputname.substring(inputname.length() + 1);
        if ("protocol".equals(name)) { wantProtocol = true; }
        if ("userinfo".equals(name)) { wantUserinfo = true; }
        if ("host".equals(name))     { wantHost     = true; }
        if ("port".equals(name))     { wantPort     = true; }
        if ("path".equals(name))     { wantPath     = true; }
        if ("query".equals(name))    { wantQuery    = true; }
        if ("ref".equals(name))      { wantRef      = true; }
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

        int questionMark   = fieldValue.indexOf('?');
        int firstAmpersand = fieldValue.indexOf('&');

        if (wantQuery || wantPath || wantRef) {
            String pathValue;
            String queryValue;
            String refValue;
            // Now we can have one of 3 situations:
            // 1) No query string
            // 2) Query string starts with a ? (and optionally followed by one or
            // more &)
            // 3) Query string starts with a &. This is invalid but does occur!
            if (questionMark == -1) {
                if (firstAmpersand == -1) {
                    pathValue = fieldValue;
                    queryValue = ""; // We do not have anything.
                } else {
                    pathValue = fieldValue.substring(0, firstAmpersand);
                    queryValue = fieldValue.substring(firstAmpersand, fieldValue.length());
                }
            } else if (firstAmpersand == -1) {
                // Replace the ? with a & to make parsing later easier
                pathValue = fieldValue.substring(0, questionMark);
                queryValue = "&" + fieldValue.substring(questionMark + 1, fieldValue.length());
            } else {
                // We have both. So we take the first one.
                int usedOffset = Math.min(questionMark, firstAmpersand);
                pathValue = fieldValue.substring(0, usedOffset);
                queryValue = "&" + fieldValue.substring(usedOffset + 1, fieldValue.length()).replaceAll("\\?","&");
            }

            int hashMark = queryValue.indexOf('#');
            if (hashMark != -1) {
                refValue = queryValue.substring(hashMark + 1);
                queryValue = queryValue.substring(0,hashMark);
            } else {
                refValue = "";
            }

            if (wantQuery) {
                parsable.addDisection(inputname, "HTTP.QUERYSTRING", "query", queryValue);
            }
            if (wantPath) {
                parsable.addDisection(inputname, "HTTP.PATH", "path", pathValue);
            }
            if (wantRef) {
                parsable.addDisection(inputname, "HTTP.REF", "ref", refValue);
            }
        }
        if (wantProtocol||wantUserinfo||wantHost||wantPort) {
            URI uri;
            try {
                uri = new URI(fieldValue);
            } catch (URISyntaxException e) {
                throw new DisectionFailure("Unable to parse the URI: >>>" + fieldValue + "<<< (" + e.getMessage() + ")");
            }
            if (wantProtocol) {
                parsable.addDisection(inputname, "HTTP.PROTOCOL", "protocol", uri.getRawSchemeSpecificPart());
            }
            if (wantUserinfo) {
                parsable.addDisection(inputname, "HTTP.USERINFO", "userinfo", uri.getUserInfo());
            }
            if (wantHost) {
                parsable.addDisection(inputname, "HTTP.HOST", "host", uri.getHost());
            }
            if (wantPort) {
                if (uri.getPort() != -1) {
                    parsable.addDisection(inputname, "HTTP.PORT", "port", String.valueOf(uri.getPort()));
                }
            }
        }
    }
    // --------------------------------------------

}
