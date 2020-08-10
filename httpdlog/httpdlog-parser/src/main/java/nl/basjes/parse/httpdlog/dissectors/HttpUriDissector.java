/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.basjes.parse.core.Casts.NO_CASTS;
import static nl.basjes.parse.core.Casts.STRING_ONLY;
import static nl.basjes.parse.core.Casts.STRING_OR_LONG;
import static nl.basjes.parse.httpdlog.Utils.makeHTMLEncodedInert;

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
            return STRING_ONLY;
        }
        if ("userinfo".equals(name)) {
            wantUserinfo = true;
            return STRING_ONLY;
        }
        if ("host".equals(name)) {
            wantHost = true;
            return STRING_ONLY;
        }
        if ("port".equals(name)) {
            wantPort = true;
            return STRING_OR_LONG;
        }
        if ("path".equals(name)) {
            wantPath = true;
            return STRING_ONLY;
        }
        if ("query".equals(name)) {
            wantQuery = true;
            return STRING_ONLY;
        }
        if ("ref".equals(name)) {
            wantRef = true;
            return STRING_ONLY;
        }
        return NO_CASTS;
    }

    // --------------------------------------------


    // ---------------------------- Characters disallowed within the URI syntax
    // Excluded US-ASCII Characters are like control, space, delims and unwise

    private static final BitSet BAD_URI_CHARS = new BitSet(256);
    static {
        BAD_URI_CHARS.set(0, 255);
        // Unwise
        BAD_URI_CHARS.clear('{');
        BAD_URI_CHARS.clear('}');
        BAD_URI_CHARS.clear('|');
        BAD_URI_CHARS.clear('\\');
        BAD_URI_CHARS.clear('^');
        BAD_URI_CHARS.clear('[');
        BAD_URI_CHARS.clear(']');
        BAD_URI_CHARS.clear('`');
        // Space
        BAD_URI_CHARS.clear(0x20);
        // Control
        BAD_URI_CHARS.clear(0, 0x1F);
        BAD_URI_CHARS.clear(0x7F);
        // Extra
        BAD_URI_CHARS.clear('<');
        BAD_URI_CHARS.clear('>');
        BAD_URI_CHARS.clear('"');
    }

    // Match % encoded chars that are NOT followed by hex chars (may be at the end of the string)
    private static final Pattern BAD_EXCAPE_PATTERN = Pattern.compile("%([^0-9a-fA-F]|[0-9a-fA-F][^0-9a-fA-F]|.$|$)");
    private static final Pattern EQUALS_HASH_PATTERN = Pattern.compile("=#");
    private static final Pattern HASH_AMP_PATTERN = Pattern.compile("#&");
    private static final Pattern DOUBLE_HASH_PATTERN = Pattern.compile("#(.*)#");
    private static final Pattern ALMOST_HTML_ENCODED = Pattern.compile("([^&])(#x[0-9a-fA-F][0-9a-fA-F];)");

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String uriString = field.getValue().getString();
        if (uriString == null || uriString.isEmpty()) {
            return; // Nothing to do here
        }

        // First we cleanup the URI so we fail less often over 'garbage' URIs.
        // See: https://stackoverflow.com/questions/11038967/brackets-in-a-request-url-are-legal-but-not-in-a-uri-java
        uriString = new String(URLCodec.encodeUrl(BAD_URI_CHARS, uriString.getBytes(UTF_8)), US_ASCII);

        // Now we translate any HTML encoded entities/characters into URL UTF-8 encoded characters
        uriString = makeHTMLEncodedInert(uriString);

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

        // We have URIs with fragments like this:
        //    /path/?_requestid=1234#x3D;12341234&Referrer&#x3D;blablabla
        // So first we repair the broken encoded char
        uriString = ALMOST_HTML_ENCODED.matcher(uriString).replaceAll("$1&$2");
        uriString = StringEscapeUtils.unescapeHtml4(uriString);
        // And we see URIs with this:
        //    /path/?Referrer=ADV1234#&f=API&subid=#&name=12341234
        uriString = EQUALS_HASH_PATTERN.matcher(uriString).replaceAll("=");
        uriString = HASH_AMP_PATTERN.matcher(uriString).replaceAll("&");

        // If we still have multiple '#' in here we replace them with something else: '~'
        while (true) {
            Matcher doubleHashMatcher = DOUBLE_HASH_PATTERN.matcher(uriString);
            if (!doubleHashMatcher.find()) {
                break;
            }
            uriString = doubleHashMatcher.replaceAll("~$1#");
        }

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
                String value = uri.getRawQuery();
                if (value != null && !value.isEmpty()) {
                    parsable.addDissection(inputname, "HTTP.QUERYSTRING", "query", value);
                }
            }
            if (wantPath) {
                String value = uri.getPath();
                if (value != null && !value.isEmpty()) {
                    parsable.addDissection(inputname, "HTTP.PATH", "path", value);
                }
            }
            if (wantRef) {
                String value = uri.getFragment();
                if (value != null && !value.isEmpty()) {
                    parsable.addDissection(inputname, "HTTP.REF", "ref", value);
                }
            }
        }

        if (isUrl) {
            if (wantProtocol) {
                String value = uri.getScheme();
                if (value != null && !value.isEmpty()) {
                    parsable.addDissection(inputname, "HTTP.PROTOCOL", "protocol", value);
                }
            }
            if (wantUserinfo) {
                String value = uri.getUserInfo();
                if (value != null && !value.isEmpty()) {
                    parsable.addDissection(inputname, "HTTP.USERINFO", "userinfo", value);
                }
            }
            if (wantHost) {
                String value = uri.getHost();
                if (value != null && !value.isEmpty()) {
                    parsable.addDissection(inputname, "HTTP.HOST", "host", value);
                }
            }
            if (wantPort) {
                int value = uri.getPort();
                if (value != -1) {
                    parsable.addDissection(inputname, "HTTP.PORT", "port", value);
                }
            }
        }
    }
    // --------------------------------------------

}
