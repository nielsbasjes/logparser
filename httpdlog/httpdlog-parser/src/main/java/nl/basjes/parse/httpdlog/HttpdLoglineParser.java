/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2016 Niels Basjes
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

public class HttpdLoglineParser<RECORD> extends Parser<RECORD> {

    // --------------------------------------------

    public HttpdLoglineParser(
            final Class<RECORD> clazz,
            final String logformat) {
        super(clazz);
        setupDissectors(logformat, null);
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
