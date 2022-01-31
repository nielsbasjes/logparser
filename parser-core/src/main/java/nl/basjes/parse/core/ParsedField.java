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

public class ParsedField {

    private final String  type;
    private final String  name;
    private final Value   value;

    public ParsedField(String type, String name, Value value) {
        this.type = type;
        this.name = name;
        if (value==null) {
            this.value = new Value((String)null);
        } else {
            this.value = value;
        }
    }

    public ParsedField(String type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = new Value(value);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Value getValue() {
        return value;
    }

    public static String makeId(String type, String name) {
        return type+':'+name;
    }

    public String getId() {
        return makeId(type, name);
    }

    public String toString(){
        return getId() + " = " + value;
    }

}
