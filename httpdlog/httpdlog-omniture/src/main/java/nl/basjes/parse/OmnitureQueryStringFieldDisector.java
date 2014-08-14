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

package nl.basjes.parse;

import nl.basjes.parse.http.disectors.QueryStringFieldDisector;

import java.util.ArrayList;
import java.util.List;

public class OmnitureQueryStringFieldDisector extends QueryStringFieldDisector {
    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<String>();
        result.add("STRING:*");
        result.add("PRODUCTS:products");
        result.add("HTTP.URI:g");
        result.add("HTTP.URI:r");
        return result;
    }

    @Override
    public String getDisectionType(String basename, String name) {
        if ("products".equals(name)){
            return "PRODUCTS";
        }
        if ("g".equals(name)){
            return "HTTP.URI";
        }
        if ("r".equals(name)){
            return "HTTP.URI";
        }
        return "STRING";
    }
}
