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
package nl.basjes.parse.core.exceptions;

import java.lang.reflect.Method;

public class InvalidFieldMethodSignature extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidFieldMethodSignature(final Method method) {
        super("The method " + method.getDeclaringClass().getName() + "." + method.getName()
                + " does not conform to \"" + method.getName() + "(String name, String value)\".");
    }
}
