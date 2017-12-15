/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

package nl.basjes.parse.httpdlog.storm;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector;
import nl.basjes.parse.useragent.dissector.UserAgentDissector;

// CHECKSTYLE.OFF: LineLength
// CHECKSTYLE.OFF: LeftCurly
// CHxECKSTYLE.OFF: ParamPad
// CHExCKSTYLE.OFF: MethodParamPad
// CHECKSTYLE.OFF: HideUtilityClassConstructor
public final class TestCase {

    public static String getLogFormat() {
        return "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\"";
    }

    public static String getInputLine() {
        // CHECKSTYLE.OFF: LineLength
        return "2001:980:91c0:1:8d31:a232:25e5:85d - - [05/Sep/2010:11:27:50 +0200] \"GET /b/ss/advbolprod2/1/H.22.1/s73176445413647?AQB=1&pccr=true&vidn=27F07A1B85012045-4000011500517C43&&ndh=1&t=19%2F5%2F2012%2023%3A51%3A27%202%20-120&ce=UTF-8&ns=bol&pageName=%2Fnl%2Fp%2Ffissler-speciaal-pannen-grillpan-28-x-28-cm%2F9200000002876066%2F&g=http%3A%2F%2Fwww.bol.com%2Fnl%2Fp%2Ffissler-speciaal-pannen-grillpan-28-x-28-cm%2F9200000002876066%2F%3Fpromo%3Dkoken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066%26bltg.pg_nm%3Dkoken-pannen%26bltg.slt_id%3D303%26bltg.slt_nm%3Dhs-koken-pannen-afj-120601%26bltg.slt_p&r=http%3A%2F%2Fwww.bol.com%2Fnl%2Fm%2Fkoken-tafelen%2Fkoken-pannen%2FN%2F11766%2Findex.html%3Fblabla%3Dblablawashere&cc=EUR&ch=D%3Dv3&server=ps316&events=prodView%2Cevent1%2Cevent2%2Cevent31&products=%3B9200000002876066%3B%3B%3B%3Bevar3%3Dkth%7Cevar8%3D9200000002876066_Fissler%20Speciaal%20Pannen%20-%20Grillpan%20-%2028%20x%2028%20cm%7Cevar35%3D170%7Cevar47%3DKTH%7Cevar9%3DNew%7Cevar40%3Dno%20reviews%2C%3B%3B%3B%3Bevent31%3D423&c1=catalog%3Akth%3Aproduct-detail&v1=D%3Dc1&h1=catalog%2Fkth%2Fproduct-detail&h2=D%3DpageName&v3=kth&l3=endeca_001-mensen_default%2Cendeca_exact-boeken_default%2Cendeca_verschijningsjaar_default%2Cendeca_hardgoodscategoriesyn_default%2Cendeca_searchrank-hadoop_default%2Cendeca_genre_default%2Cendeca_uitvoering_default&v4=ps316&v6=koken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066&v10=Tu%2023%3A30&v12=logged%20in&v13=New&c25=niet%20ssl&c26=3631&c30=84.106.227.113.1323208998208762&v31=2000285551&c45=20120619235127&c46=20120501%204.3.4.1&c47=D%3Ds_vi&c49=%2Fnl%2Fcatalog%2Fproduct-detail.jsp&c50=%2Fnl%2Fcatalog%2Fproduct-detail.jsp&v51=www.bol.com&s=1280x800&c=24&j=1.7&v=N&k=Y&bw=1280&bh=272&p=Shockwave%20Flash%3B&AQE=1 HTTP/1.1\" 200 23617 \"http://www.google.nl/imgres?imgurl=http://daniel_en_sander.basjes.nl/fotos/geboorte-kaartje/geboortekaartje-binnenkant.jpg&imgrefurl=http://daniel_en_sander.basjes.nl/fotos/geboorte-kaartje&usg=__LDxRMkacRs6yLluLcIrwoFsXY6o=&h=521&w=1024&sz=41&hl=nl&start=13&zoom=1&um=1&itbs=1&tbnid=Sqml3uGbjoyBYM:&tbnh=76&tbnw=150&prev=/images%3Fq%3Dbinnenkant%2Bgeboortekaartje%26um%3D1%26hl%3Dnl%26sa%3DN%26biw%3D1882%26bih%3D1014%26tbs%3Disch:1\" \"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8\" \"jquery-ui-theme=Eggplant; BuI=SomeThing; Apache=127.0.0.1.1351111543699529\"";
        // CHECKSTYLE.ON: LineLength
    }

    public static Parser<TestRecord> createTestParser() throws NoSuchMethodException {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, getLogFormat());

        parser.addDissector(new ScreenResolutionDissector());
        parser.addDissector(new UserAgentDissector());

        parser.addTypeRemapping("request.firstline.uri.query.g", "HTTP.URI");
        parser.addTypeRemapping("request.firstline.uri.query.r", "HTTP.URI");
        parser.addTypeRemapping("request.firstline.uri.query.s", "SCREENRESOLUTION");

        parser.addParseTarget("setConnectionClientHost", "IP:connection.client.host");
        parser.addParseTarget("setRequestReceiveTime",   "TIME.STAMP:request.receive.time");
        parser.addParseTarget("setReferrer",             "STRING:request.firstline.uri.query.g.query.promo");
        parser.addParseTarget("setScreenResolution",     "STRING:request.firstline.uri.query.s");
        parser.addParseTarget("setScreenWidth",          "SCREENWIDTH:request.firstline.uri.query.s.width");
        parser.addParseTarget("setScreenHeight",         "SCREENHEIGHT:request.firstline.uri.query.s.height");
        parser.addParseTarget("setGoogleQuery",          "STRING:request.firstline.uri.query.r.query.blabla");
        parser.addParseTarget("setBui",                  "HTTP.COOKIE:request.cookies.bui");
        parser.addParseTarget("setRequestUseragent",     "HTTP.USERAGENT:request.user-agent");
        parser.addParseTarget("setDevice_class",         "STRING:request.user-agent.device_class");
        parser.addParseTarget("setDevice_brand",         "STRING:request.user-agent.device_brand");
        parser.addParseTarget("setAgent_class",          "STRING:request.user-agent.agent_class");
        parser.addParseTarget("setAgent_name",           "STRING:request.user-agent.agent_name");
        parser.addParseTarget("setAgent_version",        "STRING:request.user-agent.agent_version");
        return parser;
    }

    // CHECKSTYLE.OFF: LineLength
    public static String getExpectedConnectionClientHost()      { return "2001:980:91c0:1:8d31:a232:25e5:85d"; }
    public static String getExpectedRequestReceiveTime()        { return "05/Sep/2010:11:27:50 +0200"; }
    public static Long   getExpectedRequestReceiveTimeEpoch()   { return 1283678870000L; }
    public static String getExpectedReferrer()                  { return "koken-pannen_303_hs-koken-pannen-afj-120601_B3_product_1_9200000002876066"; }
    public static String getExpectedScreenResolution()          { return "1280x800"; }
    public static Long   getExpectedScreenWidth()               { return 1280L; }
    public static Long   getExpectedScreenHeight()              { return 800L; }
    public static String getExpectedGoogleQuery()               { return "blablawashere"; }
    public static String getExpectedBui()                       { return "SomeThing"; }
    public static String getExpectedRequestUseragent()          { return "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 Safari/533.17.8"; }
    public static String getExpectedDeviceClass()               { return "Desktop"; }
    public static String getExpectedDeviceBrand()               { return "Apple"; }
    public static String getExpectedAgentClass()                { return "Browser"; }
    public static String getExpectedAgentName()                 { return "Safari"; }
    public static String getExpectedAgentVersion()              { return "5.0.1"; }

}
