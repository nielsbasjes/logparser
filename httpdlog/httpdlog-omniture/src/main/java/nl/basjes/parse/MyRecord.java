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
        "IP:connection.client.host",
        "TIME.STAMP:request.receive.time",
        "STRING:request.firstline.uri.query.g.query.referrer",
        "STRING:request.firstline.uri.query.g.query.promo",
        "STRING:request.firstline.uri.query.g.query.*",
        "STRING:request.firstline.uri.query.s",
        "STRING:request.firstline.uri.query.r.query.q",
        "HTTP.USERAGENT:request.user-agent",
        "STRING:request.firstline.uri.query.g",
        "HTTP.URI:request.firstline.uri.query.g"
        })
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
