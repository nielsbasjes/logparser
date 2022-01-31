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
package nl.basjes.parse.httpdlog;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.List;

public class PojoGenerator {
    @Option(name = "-logformat", usage = "<Apache HTTPD Logformat>", required = true)
    private static final String LOG_FORMAT = "common";

    static class MyRecord {
        public void setter(String name, String value) {
            System.out.println("SETTER CALLED FOR \"" + name + "\" = \"" + value + "\"");
        }
    }

    public static void main(String[] args) throws NoSuchMethodException, MissingDissectorsException, InvalidDissectorException {
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

    public void run() throws NoSuchMethodException, MissingDissectorsException, InvalidDissectorException {
        HttpdLoglineParser<MyRecord> parser = new HttpdLoglineParser<>(MyRecord.class, LOG_FORMAT);

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

