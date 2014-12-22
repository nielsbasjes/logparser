/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

package nl.basjes.parse.dissectors.http;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class HttpUriDissector extends Dissector {
    // --------------------------------------------

    private static final String INPUT_TYPE = "HTTP.URI";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
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
    public boolean initializeFromSettingsParameter(String settings) {
        return true; // Everything went right.
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        // Nothing to do
    }

    private boolean wantProtocol = false;
    private boolean wantUserinfo = false;
    private boolean wantHost = false;
    private boolean wantPort = false;
    private boolean wantPath = false;
    private boolean wantQuery = false;
    private boolean wantRef = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname.substring(inputname.length() + 1);
        if ("protocol".equals(name)) {
            wantProtocol = true;
            return Casts.STRING_ONLY;
        }
        if ("userinfo".equals(name)) {
            wantUserinfo = true;
            return Casts.STRING_ONLY;
        }
        if ("host".equals(name)) {
            wantHost = true;
            return Casts.STRING_ONLY;
        }
        if ("port".equals(name)) {
            wantPort = true;
            return Casts.STRING_OR_LONG;
        }
        if ("path".equals(name)) {
            wantPath = true;
            return Casts.STRING_ONLY;
        }
        if ("query".equals(name)) {
            wantQuery = true;
            return Casts.STRING_ONLY;
        }
        if ("ref".equals(name)) {
            wantRef = true;
            return Casts.STRING_ONLY;
        }
        return null;
    }

    @Override
    public void prepareForRun() {
        // We do not do anything extra here
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        boolean isUrl = true;
        URL url;
        try {
            if (fieldValue.startsWith("/")) {
                url = new URL("http://xxx" + fieldValue);
                isUrl = false; // I.e. we do not return the values we just faked.
            } else {
                url = new URL(fieldValue);
            }
        } catch (MalformedURLException e) {
            throw new DissectionFailure("Unable to parse the URI: >>>" + fieldValue + "<<< (" + e.getMessage() + ")");
        }

        if (wantQuery || wantPath || wantRef) {
            String rawPath = url.getFile();
            String pathValue;
            String queryValue;

            int questionMark = rawPath.indexOf('?');
            int firstAmpersand = rawPath.indexOf('&');
            // Now we can have one of 3 situations:
            // 1) No query string
            // 2) Query string starts with a ? (and optionally followed by one or
            // more &)
            // 3) Query string starts with a &. This is invalid but does occur!
            if (questionMark == -1) {
                if (firstAmpersand == -1) {
                    pathValue = rawPath;
                    queryValue = ""; // We do not have anything.
                } else {
                    pathValue = rawPath.substring(0, firstAmpersand);
                    queryValue = rawPath.substring(firstAmpersand, rawPath.length());
                }
            } else if (firstAmpersand == -1) {
                // Replace the ? with a & to make parsing later easier
                pathValue = rawPath.substring(0, questionMark);
                queryValue = "&" + rawPath.substring(questionMark + 1, rawPath.length());
            } else {
                // We have both. So we take the first one.
                int usedOffset = Math.min(questionMark, firstAmpersand);
                pathValue = rawPath.substring(0, usedOffset);
                queryValue = "&" + rawPath.substring(usedOffset + 1, rawPath.length()).replaceAll("\\?", "&");
            }

            if (wantQuery) {
                parsable.addDissection(inputname, "HTTP.QUERYSTRING", "query", queryValue);
            }
            if (wantPath) {
                parsable.addDissection(inputname, "HTTP.PATH", "path", pathValue);
            }
            if (wantRef) {
                parsable.addDissection(inputname, "HTTP.REF", "ref", url.getRef());
            }
        }

        if (isUrl) {
            if (wantProtocol) {
                parsable.addDissection(inputname, "HTTP.PROTOCOL", "protocol", url.getProtocol());
            }
            if (wantUserinfo) {
                parsable.addDissection(inputname, "HTTP.USERINFO", "userinfo", url.getUserInfo());
            }
            if (wantHost) {
                parsable.addDissection(inputname, "HTTP.HOST", "host", url.getHost());
            }
            if (wantPort) {
                if (url.getPort() != -1) {
                    parsable.addDissection(inputname, "HTTP.PORT", "port", String.valueOf(url.getPort()));
                }
            }
        }
    }
    // --------------------------------------------

}
