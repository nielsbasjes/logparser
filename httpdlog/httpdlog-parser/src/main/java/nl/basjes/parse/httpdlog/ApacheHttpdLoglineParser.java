/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
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

import nl.basjes.parse.httpdlog.dissectors.HttpFirstLineDissector;
import nl.basjes.parse.httpdlog.dissectors.HttpUriDissector;
import nl.basjes.parse.httpdlog.dissectors.QueryStringFieldDissector;
import nl.basjes.parse.httpdlog.dissectors.RequestCookieListDissector;
import nl.basjes.parse.httpdlog.dissectors.ResponseSetCookieDissector;
import nl.basjes.parse.httpdlog.dissectors.ResponseSetCookieListDissector;
import nl.basjes.parse.httpdlog.dissectors.TimeStampDissector;
import nl.basjes.parse.core.Parser;

public class ApacheHttpdLoglineParser<RECORD> extends Parser<RECORD> {

    // --------------------------------------------

    public ApacheHttpdLoglineParser(
            final Class<RECORD> clazz,
            final String logformat) {
        // This indicates what we need
        super(clazz);

        // The pieces we have to get there
        addDissector(new ApacheHttpdLogFormatDissector(logformat));
        // We set the default parser to what we find in the Apache httpd Logfiles
        //                                   [05/Sep/2010:11:27:50 +0200]
        addDissector(new TimeStampDissector("[dd/MMM/yyyy:HH:mm:ss ZZ]"));
        addDissector(new HttpFirstLineDissector());
        addDissector(new HttpUriDissector());
        addDissector(new QueryStringFieldDissector());
        addDissector(new RequestCookieListDissector());
        addDissector(new ResponseSetCookieListDissector());
        addDissector(new ResponseSetCookieDissector());

        // And we define the input for this parser
        setRootType(ApacheHttpdLogFormatDissector.INPUT_TYPE);
    }

    // --------------------------------------------


}
