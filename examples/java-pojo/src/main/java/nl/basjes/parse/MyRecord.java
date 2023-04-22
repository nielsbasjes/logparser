/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
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
package nl.basjes.parse;

import nl.basjes.parse.core.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class MyRecord {

    private final Map<String, String> results = new HashMap<>(32);

    @Field("STRING:request.firstline.uri.query.*")
    public void setQueryDeepMany(final String name, final String value) {
        results.put(name, value);
    }

    @Field("STRING:request.firstline.uri.query.img")
    public void setQueryImg(final String name, final String value) {
        results.put(name, value);
    }

    @Field("IP:connection.client.host")
    public void setIP(final String value) {
        results.put("IP:connection.client.host", value);
    }

    @Field({
        "HTTP.QUERYSTRING:request.firstline.uri.query",
        "NUMBER:connection.client.logname",
        "STRING:connection.client.user",
        "TIME.STAMP:request.receive.time",
        "HTTP.URI:request.firstline.uri",
        "BYTESCLF:response.body.bytes",
        "HTTP.URI:request.referer",
        "HTTP.USERAGENT:request.user-agent",
        "TIME.DAY:request.receive.time.day",
        "TIME.HOUR:request.receive.time.hour",
        "TIME.MONTHNAME:request.receive.time.monthname"
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
