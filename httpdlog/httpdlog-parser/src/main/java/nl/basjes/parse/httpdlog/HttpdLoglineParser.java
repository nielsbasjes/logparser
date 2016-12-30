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
package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.dissectors.HttpFirstLineDissector;
import nl.basjes.parse.httpdlog.dissectors.HttpUriDissector;
import nl.basjes.parse.httpdlog.dissectors.ModUniqueIdDissector;
import nl.basjes.parse.httpdlog.dissectors.QueryStringFieldDissector;
import nl.basjes.parse.httpdlog.dissectors.RequestCookieListDissector;
import nl.basjes.parse.httpdlog.dissectors.ResponseSetCookieDissector;
import nl.basjes.parse.httpdlog.dissectors.ResponseSetCookieListDissector;
import nl.basjes.parse.httpdlog.dissectors.TimeStampDissector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.basjes.parse.httpdlog.Version.getBuildTimestamp;
import static nl.basjes.parse.httpdlog.Version.getGitCommitIdDescribeShort;
import static nl.basjes.parse.httpdlog.Version.getProjectVersion;

public class HttpdLoglineParser<RECORD> extends Parser<RECORD> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpdLoglineParser.class);

    // --------------------------------------------

    public HttpdLoglineParser(
            final Class<RECORD> clazz,
            final String logformat) {
        super(clazz);
        LOG.info("Loading {}", getVersion());
        setupDissectors(logformat, null);
    }

    public static String getVersion() {
        return "HttpdLoglineParser " + getProjectVersion() + " (" + getGitCommitIdDescribeShort() + " @ " + getBuildTimestamp() + ")";
    }

    public HttpdLoglineParser(
            final Class<RECORD> clazz,
            final String logformat,
            final String timestampFormat) {
        super(clazz);
        setupDissectors(logformat, timestampFormat);
    }

    private void setupDissectors(
            final String logformat,
            final String timestampFormat) {
        // The pieces we have to get there
        addDissector(new HttpdLogFormatDissector(logformat));
        addDissector(new TimeStampDissector(timestampFormat));
        addDissector(new HttpFirstLineDissector());
        addDissector(new HttpUriDissector());
        addDissector(new QueryStringFieldDissector());
        addDissector(new RequestCookieListDissector());
        addDissector(new ResponseSetCookieListDissector());
        addDissector(new ResponseSetCookieDissector());
        addDissector(new ModUniqueIdDissector());

        // And we define the input for this parser
        setRootType(HttpdLogFormatDissector.INPUT_TYPE);
    }

    // --------------------------------------------

}
