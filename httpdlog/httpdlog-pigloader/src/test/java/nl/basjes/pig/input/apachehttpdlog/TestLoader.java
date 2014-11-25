package nl.basjes.pig.input.apachehttpdlog;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;

public class TestLoader {

  @Test
  public void BasicLoaderTest() throws Exception {
    PigServer pigServer = new PigServer(ExecType.LOCAL);
    Storage.Data data = resetData(pigServer);

    pigServer.registerQuery(
      "Clicks = " +
      "    LOAD '" + getClass().getResource("/access.log").toString() + "' " +
      "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
      "            '%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"'," +
      "            'IP:connection.client.host'," +
      "            'HTTP.FIRSTLINE:request.firstline'," +
      "            'HTTP.METHOD:request.firstline.method'," +
      "            'HTTP.URI:request.firstline.uri'," +
      "            'HTTP.QUERYSTRING:request.firstline.uri.query'," +
      "            'STRING:request.firstline.uri.query.FOO'," +
      "            'HTTP.USERAGENT:request.user-agent'" +
      "            )" +
      "" +
      "         AS (" +
      "            ConnectionClientHost," +
      "            RequestFirstline," +
      "            RequestFirstlineMethod," +
      "            RequestFirstlineUri," +
      "            RequestFirstlineUriQuery," +
      "            RequestFirstlineUriQueryFoo," +
      "            RequestUseragent:chararray" +
      "            );"
    );

    pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

    List<Tuple> out = data.get("Clicks");

    assertEquals(1, out.size());
    assertEquals(tuple(
                    "172.21.13.88",
                    "GET /1-500e-KWh?FoO=bAr%20BaR HTTP/1.0",
                    "GET",
                    "/1-500e-KWh?FoO=bAr%20BaR",
                    "&FoO=bAr%20BaR",
                    "bAr BaR",
                    "Mozilla/5.0 Dummy UserAgent"),
            out.get(0));
  }
}
