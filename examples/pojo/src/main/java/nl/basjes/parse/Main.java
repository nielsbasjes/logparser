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

import java.text.ParseException;
import java.util.List;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private void printAllPossibles(String logformat) {
    // To figure out what values we CAN get from this line we instantiate the parser with a dummy class
    // that does not have ANY @Field annotations.
    Parser<Object> dummyParser;
    try {
      dummyParser = new ApacheHttpdLoglineParser<>(Object.class, logformat);
    } catch (ParseException e) {
      e.printStackTrace();
      return;
    }
    LOG.info("==================================");
    List<String> possiblePaths;
    try {
      possiblePaths = dummyParser.getPossiblePaths();
    } catch (MissingDisectorsException | InvalidDisectorException e) {
      e.printStackTrace();
      return;
    }
    for (String path: possiblePaths) {
      LOG.info(path);
    }
    LOG.info("==================================");
  }

  private void run() throws InvalidDisectorException, MissingDisectorsException, ParseException {

    // This format and logline originate from here:
    // http://stackoverflow.com/questions/20349184/java-parse-log-file
    String logformat = "%t %u [%D %h %{True-Client-IP}i %{UNIQUE_ID}e %r] %{Cookie}i %s \"%{User-Agent}i\" \"%{host}i\" %l %b %{Referer}i";
    String logline = "[02/Dec/2013:14:10:30 -0000] - [52075 10.102.4.254 177.43.52.210 UpyU1gpmBAwAACfd5W0AAAAW GET /SS14-VTam-ny_019.jpg.rendition.zoomable.jpg HTTP/1.1] hsfirstvisit=http%3A%2F%2Fwww.domain.com%2Fen-us||1372268254000; _opt_vi_3FNG8DZU=F870DCFD-CBA4-4B6E-BB58-4605A78EE71A; __ptca=145721067.0aDxsZlIuM48.1372279055.1379945057.1379950362.9; __ptv_62vY4e=0aDxsZlIuM48; __pti_62vY4e=0aDxsZlIuM48; __ptcz=145721067.1372279055.1.0.ptmcsr=(direct)|ptmcmd=(none)|ptmccn=(direct); __hstc=145721067.b86362bb7a1d257bfa2d1fb77e128a85.1372268254968.1379934256743.1379939561848.9; hubspotutk=b86362bb7a1d257bfa2d1fb77e128a85; USER_GROUP=julinho%3Afalse; has_js=1; WT_FPC=id=177.43.52.210-1491335248.30301337:lv=1385997780893:ss=1385997780893; dtCookie=1F2E0E1037589799D8D503EB8CFA12A1|_default|1; RM=julinho%3A5248423ad3fe062f06c54915e6cde5cb45147977; wcid=UpyKsQpmBAwAABURyNoAAAAS%3A35d8227ba1e8a9a9cebaaf8d019a74777c32b4c8; Carte::KerberosLexicon_getWGSN=82ae3dcd1b956288c3c86bdbed6ebcc0fd040e1e; UserData=Username%3AJULINHO%3AHomepage%3A1%3AReReg%3A0%3ATrialist%3A0%3ALanguage%3Aen%3ACcode%3Abr%3AForceReReg%3A0; UserID=1356673%3A12345%3A1234567890%3A123%3Accode%3Abr; USER_DATA=1356673%3Ajulinho%3AJulio+Jose%3Ada+Silva%3Ajulinho%40tecnoblu.com.br%3A0%3A1%3Aen%3Abr%3A%3AWGSN%3A1385990833.81925%3A82ae3dcd1b956288c3c86bdbed6ebcc0fd040e1e; MODE=FONTIS; SECTION=%2Fcontent%2Fsection%2Fhome.html; edge_auth=ip%3D177.43.52.210~expires%3D1385994522~access%3D%2Fapps%2F%2A%21%2Fbin%2F%2A%21%2Fcontent%2F%2A%21%2Fetc%2F%2A%21%2Fhome%2F%2A%21%2Flibs%2F%2A%21%2Freport%2F%2A%21%2Fsection%2F%2A%21%2Fwgsn%2F%2A~md5%3D90e73ee10161c1afacab12c6ea30b4ef; __utma=94539802.1793276213.1372268248.1385572390.1385990581.16; __utmb=94539802.52.9.1385991739764; __utmc=94539802; __utmz=94539802.1372268248.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); WT_FPC=id=177.43.52.210-1491335248.30301337:lv=1386000374581:ss=1386000374581; dtPC=-; NSC_wtfswfs_xfcgbsn40-41=ffffffff096e1a1d45525d5f4f58455e445a4a423660; akamai-edge=5ac6e5b3d0bbe2ea771bb2916d8bab34ea222a6a 200 \"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36\" \"www.domain.com\" - 463952 http://www.domain.com/content/report/shows/New_York/KSHK/trip/s_s_14_ny_ww/sheers.html";

    printAllPossibles(logformat);

    Parser<MyRecord> parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);
    MyRecord record = new MyRecord();

    LOG.info("==================================================================================");
    try {
      parser.parse(record, logline);
      LOG.info(record.toString());

    } catch (DisectionFailure disectionFailure) {
      disectionFailure.printStackTrace();
    }
    LOG.info("==================================================================================");
  }

  /**
   * @param args The commandline arguments
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  public static void main(final String[] args) throws Exception {
    new Main().run();
  }

}
