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
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import nl.basjes.parse.core.Casts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.EnumSet;

public class TokenOutputField implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TokenOutputField.class);

    private final String type;
    private final String name;
    private final EnumSet<Casts> casts;

    /**
     * If this is not null the string is the name of the replacement field
     */
    private String deprecated = null;

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

    public TokenOutputField deprecateFor(String deprecatedFor) {
        deprecated = deprecatedFor;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated != null;
    }

    public void wasUsed() {
        if (deprecated != null) {
            LOG.warn("------------------------------------------------------------------------");
            LOG.warn("The field \"{}:{}\" is deprecated. Use \"{}\" instead.", type, name, deprecated);
            LOG.warn("------------------------------------------------------------------------");
        }
    }

    @Override
    public String toString() {
        String msg = "{ "+getType()+':'+getName()+" --> "+casts+" }";
        if (deprecated != null) {
            return "DEPRECATED: " + msg;
        }
        return msg;
    }
}

