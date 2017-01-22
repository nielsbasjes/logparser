/*
 * Apache HTTPD & NGINX Access log parsing made easy
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
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import nl.basjes.parse.core.Casts;

import java.util.EnumSet;

public class TokenOutputField {
    private final String type;
    private final String name;
    private final EnumSet<Casts> casts;

    public TokenOutputField(String type, String name, EnumSet<Casts> casts) {
        // RFC 2616 Section 4.2 states: "Field names are case-insensitive."
        this.name = name.toLowerCase();
        this.type = type;
        this.casts = casts;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public EnumSet<Casts> getCasts() {
        return casts;
    }

    @Override
    public String toString() {
        return "{ "+getType()+':'+getName()+" --> "+casts+" }";
    }
}

