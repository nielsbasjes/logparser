/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2021 Niels Basjes
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

package nl.basjes.parse.core;

public class Value {

    enum Filled {
        STRING,
        LONG,
        DOUBLE
    }

    private final Filled filled;
    private String s = null;
    private Long l = null;
    private Double d = null;

    public Value(String p) {
        filled = Filled.STRING;
        this.s = p;
    }

    public Value(Long p) {
        filled = Filled.LONG;
        this.l = p;
    }

    public Value(Double p) {
        filled = Filled.DOUBLE;
        this.d = p;
    }

    public String getString() {
        switch (filled) {
            case LONG:
                return l == null ? null : Long.toString(l);
            case DOUBLE:
                return d == null ? null : Double.toString(d);
            default: // == case STRING:
                return s;
        }
    }

    public Long getLong() {
        switch (filled) {
            case STRING:
                try {
                    return s == null ? null : Long.parseLong(s);
                } catch (NumberFormatException e) {
                    return null;
                }
            case DOUBLE:
                return d == null ? null : (long) Math.floor(d + 0.5d); // Apply rounding
            default: // == case LONG:
                return l;
        }
    }

    public Double getDouble() {
        switch (filled) {
            case STRING:
                try {
                    return s == null ? null : Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    return null;
                }
            case LONG:
                return l == null ? null : Double.valueOf(l);
            default: // == case DOUBLE:
                return d;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb  .append("Value{")
            .append("filled=").append(filled);
        if (s == null) {
            sb.append(", s=null");
        } else {
            sb.append(", s='").append(s).append('\'');
        }
        sb
            .append(", l=").append(l)
            .append(", d=").append(d)
            .append('}');
        return sb.toString();
    }
}
