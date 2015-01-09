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

package nl.basjes.parse;

import java.text.ParseException;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Casts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class OmnitureLogLineParser<RECORD> extends ApacheHttpdLoglineParser<RECORD> {

    private Logger LOG = LoggerFactory.getLogger(OmnitureLogLineParser.class);

    public OmnitureLogLineParser(Class<RECORD> clazz, String logformat)
        throws ParseException {
        super(clazz, logformat);

        String deprecation = " ---------- [DEPRECATED] ";
        LOG.warn("\n" +
                deprecation + "\n" +
                deprecation + "The Omniture specific parser has been deprecated.\n" +
                deprecation + "Use the normal Apache parser in conjunction with these two mappings:\n" +
                deprecation + "    request.firstline.uri.query.g ==> HTTP.URI\n"+
                deprecation + "    request.firstline.uri.query.r ==> HTTP.URI\n"+
                deprecation );

        addTypeRemapping("request.firstline.uri.query.g", "HTTP.URI", Casts.STRING_ONLY);
        addTypeRemapping("request.firstline.uri.query.r", "HTTP.URI", Casts.STRING_ONLY);
    }
}
