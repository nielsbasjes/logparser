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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import nl.basjes.parse.core.Field;

public class MyRecord {

    private final Map<String, String> results = new HashMap<>(32);

//   @Field("STRING:request.firstline.uri.query.g.query.*")
//   public void setQueryDeepMany(final String name, final String value) {
//       results.append("INPUT MANY: "+name + "=" + value + "\n");
//   }

    @Field({
        "HTTP.QUERYSTRING:request.firstline.uri.query",
        "STRING:request.firstline.uri.query.pageName",
        "STRING:request.firstline.uri.query.products",
        "PRODUCTS:request.firstline.uri.query.products",
        "HTTP.URI:request.firstline.uri.query.g",
        "HTTP.QUERYSTRING:request.firstline.uri.query.g.query",
        "STRING:request.firstline.uri.query.g.query.bltg.slt_nm",
        "HTTP.URI:request.firstline.uri.query.g",
        "HTTP.QUERYSTRING:request.firstline.uri.query.g.query",
        "HTTP.URI:request.firstline.uri.query.r",
        "IP:connection.client.ip",
        "NUMBER:connection.client.logname",
        "STRING:connection.client.user",
        "TIME.STAMP:request.receive.time",
        "HTTP.URI:request.firstline.uri",
        "STRING:request.status.last",
        "BYTES:response.body.bytesclf",
        "HTTP.HEADER:request.header.Referer",
        "HTTP.HEADER:request.header.User-Agent",
        "TIME.DAY:request.receive.time.day",
        "TIME.HOUR:request.receive.time.hour",
        "TIME.MONTHNAME:request.receive.time.monthname",
        "STRING:request.status.last"})
    public void setValue(final String name, final String value) {
        results.put(name, value);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        TreeSet<String> keys = new TreeSet<>(results.keySet());
        for (String key : keys) {
            sb.append(key).append(" = ").append(results.get(key)).append('\n');
        }

        return sb.toString();
    }

    public void clear() {
        results.clear();
    }
}
