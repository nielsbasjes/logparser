/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2013 Niels Basjes
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

import java.util.List;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Parser;

public final class Main {


    private Main() {
        // Nothing
    }

    /**
     * @param args
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws MissingDisectorsException
     */
    @SuppressWarnings({ "PMD.SystemPrintln" })
    public static void main(final String[] args) throws Exception {

        String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";
        String logline = "84.105.31.162 - - [05/Sep/2010:11:27:50 +0200] "
                + "\"GET /fotos/index.html?img=geboorte-kaartje&foo=foofoo&bar=barbar HTTP/1.1\" 200 23617 "
                + "\"http://www.google.nl/imgres?imgurl=http://daniel_en_sander.basjes.nl/fotos/geboorte-kaartje/"
                + "geboortekaartje-binnenkant.jpg&imgrefurl=http://daniel_en_sander.basjes.nl/fotos/geboorte-kaartje"
                + "&usg=__LDxRMkacRs6yLluLcIrwoFsXY6o=&h=521&w=1024&sz=41&hl=nl&start=13&zoom=1&um=1&itbs=1&"
                + "tbnid=Sqml3uGbjoyBYM:&tbnh=76&tbnw=150&prev=/images%3Fq%3Dbinnenkant%2Bgeboortekaartje%26um%3D1%26hl%3D"
                + "nl%26sa%3DN%26biw%3D1882%26bih%3D1014%26tbs%3Disch:1\" "
                + "\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) "
                + "Version/5.0.1 Safari/533.17.8\"";

        // To figure out what values we CAN get from this line we instantiate the parser with a dummy class
        // that does not have ANY @Field annotations.
        Parser<Object> dummyParser = new ApacheHttpdLoglineParser<>(Object.class, logformat);
        System.out.println("==================================");
        List<String> possiblePaths = dummyParser.getPossiblePaths();
        for (String path: possiblePaths) {
            System.out.println(path);
        }
        System.out.println("==================================");

        // Once we have the list of possible values we create a separate class that uses these values for the setters
        Parser<MyRecord> parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);

        MyRecord record = parser.parse(logline);
        System.out.println("INPUT = "+ logline);
        if (record != null) {
            System.out.println("================= \n" + record.toString() + "================= \n");
        }


        // And then we can repeat this for each next line

        String logline2 = "2001:980:91c0:1:8d31:a232:25e5:85d - - [11/Jun/2012:21:34:49 +0200] "
                + "\"GET /portal_javascripts/Plone%20Default/event-registration-cachekey2064.js "
                + "HTTP/1.1\" 200 53614 \"http://niels.basj.es/\" "
                + "\"Mozilla/5.0 (Linux; Android 4.0.3; GT-I9100 Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) "
                + "Chrome/18.0.1025.166 Mobile Safari/535.19\"";
        MyRecord record2 = parser.parse(logline2);
        System.out.println("INPUT = "+ logline2);
        if (record != null) {
            System.out.println("RECORD2 = \n" + record2.toString());
        }

    }

}
