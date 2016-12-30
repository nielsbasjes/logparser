/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2017 Niels Basjes
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
package nl.basjes.storm.bolt;

import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ParserBoltTest implements Serializable {
    // ========================================================================

    public static class TestApacheLogsSpout extends BaseRichSpout {
        private SpoutOutputCollector collector;

        public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collectorr) {
            this.collector = collectorr;
        }

        public void nextTuple() {
            Utils.sleep(10L);
            String logline = "84.105.31.162 - - [05/Sep/2010:11:27:50 +0200] "
                    + "\"GET /fotos/index.html?img=geboorte-kaartje&foo=foofoo&bar=barbar HTTP/1.1\" 200 23617 "
                    + "\"http://www.google.nl/imgres?imgurl=http://daniel_en_sander.basjes.nl/fotos/geboorte-kaartje/"
                    + "geboortekaartje-binnenkant.jpg&imgrefurl=http://daniel_en_sander.basjes.nl/fotos/geboorte-kaartje"
                    + "&usg=__LDxRMkacRs6yLluLcIrwoFsXY6o=&h=521&w=1024&sz=41&hl=nl&start=13&zoom=1&um=1&itbs=1&"
                    + "tbnid=Sqml3uGbjoyBYM:&tbnh=76&tbnw=150&prev=/images%3Fq%3Dbinnenkant%2Bgeboortekaartje%26um%3D1%26hl%3D"
                    + "nl%26sa%3DN%26biw%3D1882%26bih%3D1014%26tbs%3Disch:1\" "
                    + "\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) AppleWebKit/533.17.8 (KHTML, like Gecko) "
                    + "Version/5.0.1 Safari/533.17.8\"";
            collector.emit(new Values(logline));
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("apachelogline"));
        }
    }

    // ========================================================================

    public static class ValidateOutput extends BaseBasicBolt {
        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            Fields fields = tuple.getFields();
            Assert.assertEquals(7, fields.size());
            Assert.assertEquals("2010", tuple.getStringByField("year"));
            Assert.assertEquals("9", tuple.getStringByField("month"));
            Assert.assertEquals("5", tuple.getStringByField("day"));
            Assert.assertEquals("11", tuple.getStringByField("hour"));
            Assert.assertEquals("27", tuple.getStringByField("minute"));
            Assert.assertEquals("50", tuple.getStringByField("second"));
            Assert.assertEquals("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; nl-nl) " +
                                "AppleWebKit/533.17.8 (KHTML, like Gecko) Version/5.0.1 " +
                                "Safari/533.17.8", tuple.getStringByField("useragent"));
            System.out.print("Ok ");
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }


    // ========================================================================
    @Test
    public void runRest() throws InterruptedException {
        TopologyBuilder builder = new TopologyBuilder();

        // ----------
        builder.setSpout("Spout", new TestApacheLogsSpout());
        // ----------
        String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";
        ApacheHttpdLoglineParserBolt parserBolt = new ApacheHttpdLoglineParserBolt(logformat, "apachelogline");
        parserBolt.requestField("TIME.YEAR:request.receive.time.year",          "year");
        parserBolt.requestField("TIME.MONTH:request.receive.time.month",        "month");
        parserBolt.requestField("TIME.DAY:request.receive.time.day",            "day");
        parserBolt.requestField("TIME.HOUR:request.receive.time.hour",          "hour");
        parserBolt.requestField("TIME.MINUTE:request.receive.time.minute",      "minute");
        parserBolt.requestField("TIME.SECOND:request.receive.time.second",      "second");
        parserBolt.requestField("HTTP.USERAGENT:request.user-agent",            "useragent");

        builder.setBolt("Parser", parserBolt, 1).shuffleGrouping("Spout");
        // ----------
        builder.setBolt("Printer", new ValidateOutput(), 1).shuffleGrouping("Parser");
        // ----------

        StormTopology topology = builder.createTopology();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Unit test", new HashMap<String, String>(), topology);
        Thread.sleep(10000L); // Run for 10 seconds
        cluster.killTopology("Unit test");
        cluster.shutdown();

    }

}
