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

package nl.basjes.parse.apachehttpdlog;

import nl.basjes.parse.dissectors.http.HttpFirstLineDissector;
import nl.basjes.parse.dissectors.http.HttpUriDissector;
import nl.basjes.parse.dissectors.http.QueryStringFieldDissector;
import nl.basjes.parse.dissectors.http.RequestCookieListDissector;
import nl.basjes.parse.dissectors.http.ResponseSetCookieDissector;
import nl.basjes.parse.dissectors.http.ResponseSetCookieListDissector;
import nl.basjes.parse.dissectors.http.TimeStampDissector;
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
