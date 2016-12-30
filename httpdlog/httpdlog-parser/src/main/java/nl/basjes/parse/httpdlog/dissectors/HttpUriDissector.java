/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

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
        String name = extractFieldName(inputname, outputname);
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

    private static BitSet badUriChars = new BitSet(256);
    static {
        badUriChars.set(0, 255, true);
        badUriChars.andNot(org.apache.commons.httpclient.URI.unwise);
        badUriChars.andNot(org.apache.commons.httpclient.URI.space);
        badUriChars.andNot(org.apache.commons.httpclient.URI.control);
        badUriChars.set('<', false);
        badUriChars.set('>', false);
        badUriChars.set('"', false);
    }

    // Match % encoded chars that are NOT followed by hex chars (may be at the end of the string)
    private static final Pattern BAD_EXCAPE_PATTERN = Pattern.compile("%([^0-9a-fA-F]|[0-9a-fA-F][^0-9a-fA-F]|.$|$)");

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String uriString = field.getValue().getString();
        if (uriString == null || uriString.isEmpty()) {
            return; // Nothing to do here
        }

        // First we cleanup the URI so we fail less often over 'garbage' URIs.
        // See: http://stackoverflow.com/questions/11038967/brackets-in-a-request-url-are-legal-but-not-in-a-uri-java
        try {
            uriString = URIUtil.encode(uriString, badUriChars, "UTF-8");
        } catch (URIException e) {
            throw new DissectionFailure("Failed to parse URI >>" + field.getValue().getString()+"<< because of : " +e.getMessage());
        }

        // Before we hand it to the standard parser we hack it around a bit so we can parse
        // nasty edge cases that are illegal yet do occur in real clickstreams.
        // Also we force the query string to start with ?& so the returned query string starts with &
        // Which leads to more consistent output after parsing.
        int firstQuestionMark = uriString.indexOf('?');
        int firstAmpersand = uriString.indexOf('&');
        // Now we can have one of 3 situations:
        // 1) No query string
        // 2) Query string starts with a '?'
        //      (and optionally followed by one or more '&' or '?' )
        // 3) Query string starts with a '&'. This is invalid but does occur!
        // We may have ?x=x&y=y?z=z so we normalize it always
        // to:  ?&x=x&y=y&z=z
        if (firstAmpersand != -1 || firstQuestionMark != -1) {
            uriString = uriString.replaceAll("\\?", "&");
            uriString = uriString.replaceFirst("&", "?&");
        }

        // We find that people muck up the URL by putting % signs in the URLs that are NOT escape sequences
        // So any % that is not followed by a two 'hex' letters is fixed
        uriString = BAD_EXCAPE_PATTERN.matcher(uriString).replaceAll("%25$1");
        uriString = BAD_EXCAPE_PATTERN.matcher(uriString).replaceAll("%25$1");

        boolean isUrl = true;
        URI uri;
        try {
            if (uriString.charAt(0) == '/') {
                uri = URI.create("dummy-protocol://dummy.host.name" + uriString);
                isUrl = false; // I.e. we do not return the values we just faked.
            } else {
                uri = URI.create(uriString);
            }
        } catch (IllegalArgumentException e) {
            throw new DissectionFailure("Failed to parse URI >>" + field.getValue().getString()+"<< because of : " +e.getMessage());
        }

        if (wantQuery || wantPath || wantRef) {
            if (wantQuery) {
                String query = uri.getRawQuery();
                if (query == null) {
                    query = "";
                }
                parsable.addDissection(inputname, "HTTP.QUERYSTRING", "query", query);
            }
            if (wantPath) {
                parsable.addDissection(inputname, "HTTP.PATH", "path", uri.getPath());
            }
            if (wantRef) {
                parsable.addDissection(inputname, "HTTP.REF", "ref", uri.getFragment());
            }
        }

        if (isUrl) {
            if (wantProtocol) {
                parsable.addDissection(inputname, "HTTP.PROTOCOL", "protocol", uri.getScheme());
            }
            if (wantUserinfo) {
                parsable.addDissection(inputname, "HTTP.USERINFO", "userinfo", uri.getUserInfo());
            }
            if (wantHost) {
                parsable.addDissection(inputname, "HTTP.HOST", "host", uri.getHost());
            }
            if (wantPort) {
                if (uri.getPort() != -1) {
                    parsable.addDissection(inputname, "HTTP.PORT", "port", uri.getPort());
                }
            }
        }
    }
    // --------------------------------------------

}
