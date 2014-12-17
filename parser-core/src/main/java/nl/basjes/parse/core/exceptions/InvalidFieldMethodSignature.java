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
package nl.basjes.parse.core.exceptions;

import java.lang.reflect.Method;

public class InvalidFieldMethodSignature extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidFieldMethodSignature(final Method method) {
        super("The method " + method.getDeclaringClass().getName() + "." + method.getName()
                + " does not conform to \"" + method.getName() + "(String name, String value)\".");
    }
}
