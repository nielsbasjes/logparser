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

package nl.basjes.parse.httpdlog;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.text.ParseException;
import java.util.List;

public class PojoGenerator {
    @Option(name = "-logformat", usage = "<Apache HTTPD Logformat>", required = true)
    public static final String logformat = "common";

    class MyRecord {
        public void setter(String name, String value) {
            System.out.println("SETTER CALLED FOR \"" + name + "\" = \"" + value + "\"");
        }
    }

    public static void main(String[] args) throws ParseException, InvalidDissectorException, MissingDissectorsException, NoSuchMethodException {
        PojoGenerator generator = new PojoGenerator();
        CmdLineParser parser = new CmdLineParser(generator);
        try {
            parser.parseArgument(args);
            generator.run();
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }

    public void run() throws ParseException, InvalidDissectorException, MissingDissectorsException, NoSuchMethodException {
        ApacheHttpdLoglineParser<MyRecord> parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);

        List<String> allPossiblePaths = parser.getPossiblePaths();
        parser.addParseTarget(MyRecord.class.getMethod("setter", String.class, String.class), allPossiblePaths);

        System.out.println("class MyRecord {\n");

        for (String field : parser.getPossiblePaths()) {
            for (Casts cast : parser.getCasts(field)) {
                System.out.println("    @Field{\"" + field + "\"}\n" +
                        "    public void setter(String name, " + castToJavaType(cast) + " value) {\n" +
                        "        System.out.println(\"SETTER CALLED FOR \\\"\" + name + \"\\\" = \\\"\" + value + \"\\\"\");\n" +
                        "    }\n");
            }
        }
        System.out.println("}\n");
    }

    private String castToJavaType(Casts casts) {
        switch (casts) {
            case STRING:
                return "String";
            case LONG:
                return "Long";
            case DOUBLE:
                return "Double";
            default:
                return null;
        }
    }

}

