/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
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
package nl.basjes.parse.httpdlog.storm;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParserBoltTest implements Serializable {
    // ========================================================================

    private static final String INPUT_FIELD_NAME = "apacheLogline";
    private static final String OUTPUT_FIELD_NAME = "resultingRecord";

    private static final Logger LOG = LoggerFactory.getLogger(ParserBoltTest.class);

    public static class TestApacheLogsSpout extends BaseRichSpout {
        private SpoutOutputCollector collector;

        public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collectorr) {
            this.collector = collectorr;
        }

        public void nextTuple() {
            collector.emit(new Values(TestCase.getInputLine()));
            Utils.sleep(1000L);
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields(INPUT_FIELD_NAME));
        }
    }

    // ========================================================================

    public static class ValidateOutput extends BaseBasicBolt {
        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            Fields fields = tuple.getFields();
            Assert.assertEquals(1, fields.size());
            TestRecord record = (TestRecord) tuple.getValueByField(OUTPUT_FIELD_NAME);
            record.assertIsValid();
            LOG.info("Test passed");
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }


    // ========================================================================
    @Test
    public void runRest() throws InterruptedException, NoSuchMethodException {
        TopologyBuilder builder = new TopologyBuilder();

        // ----------
        builder.setSpout("Spout", new TestApacheLogsSpout());
        // ----------
        HttpdLoglineParserBolt parserBolt = new HttpdLoglineParserBolt(TestCase.getLogFormat(), INPUT_FIELD_NAME, OUTPUT_FIELD_NAME);

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
