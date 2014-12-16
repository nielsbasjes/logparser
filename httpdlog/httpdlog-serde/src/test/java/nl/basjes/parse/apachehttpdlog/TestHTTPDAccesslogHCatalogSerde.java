package nl.basjes.parse.apachehttpdlog;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.io.Text;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestHTTPDAccesslogHCatalogSerde {


  /**
   * Returns the union of table and partition properties,
   * with partition properties taking precedence.
   * @param tblProps
   * @param partProps
   * @return the overlayed properties
   */
  public static Properties createOverlayedProperties(Properties tblProps, Properties partProps) {
    Properties props = new Properties();
    props.putAll(tblProps);
    if (partProps != null) {
      props.putAll(partProps);
    }
    return props;
  }

  private final String logformat = "%h %a %A %l %u %t \"%r\" " +
          "%>s %b %p \"%q\" \"%{Referer}i\" %D \"%{User-agent}i\" " +
          "\"%{Cookie}i\" " +
          "\"%{Set-Cookie}o\" " +
          "\"%{If-None-Match}i\" \"%{Etag}o\"";

  private final String testLogLine =
          "127.0.0.1 127.0.0.1 127.0.0.1 - - [24/Oct/2012:23:00:44 +0200] \"GET /index.php?s=800x600 HTTP/1.1\" " +
                  "200 - 80 \"\" \"-\" 80991 \"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:11.0) Gecko/20100101 Firefox/11.0\" " +
                  "\"jquery-ui-theme=Eggplant; Apache=127.0.0.1.1351111543699529\" " +
                  "\"" +
                  "NBA-1=1234, " +
                  "NBA-2=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT, " +
                  "NBA-3=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/, " +
                  "NBA-4=1234; expires=Wed, 01-Jan-2020 00:00:10 GMT; path=/; domain=.basj.es" +
                  "\" \"-\" \"-\"";


  /**
   * Test the LazySimpleSerDe class.
   */
  @Test
  public void testHTTPDAccesslogHCatalogSerde() throws Throwable {
    try {
      // Create the SerDe
      Properties schema = new Properties();
      schema.setProperty(serdeConstants.LIST_COLUMNS, "ip,timestamp,useragent,screenWidth,screenHeight");
      schema.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,bigint,string,bigint,bigint");

      schema.setProperty("logformat",           logformat);
      schema.setProperty("field:timestamp",     "TIME.EPOCH:request.receive.time.epoch");
      schema.setProperty("field:ip",            "IP:connection.client.host");
      schema.setProperty("field:useragent",     "HTTP.USERAGENT:request.user-agent");
      schema.setProperty("load:nl.basjes.parse.http.dissectors.ScreenResolutionDissector", "x");
      schema.setProperty("map:request.firstline.uri.query.s", "SCREENRESOLUTION");
      schema.setProperty("field:screenWidth",   "SCREENWIDTH:request.firstline.uri.query.s.width");
      schema.setProperty("field:screenHeight",  "SCREENHEIGHT:request.firstline.uri.query.s.height");

      AbstractDeserializer serDe = new nl.basjes.parse.apachehttpdlog.HTTPDAccesslogHCatalogSerde();
      serDe.initialize(new Configuration(), createOverlayedProperties(schema, null));

      // Data
      Text t = new Text(testLogLine);

      // Deserialize
      Object row = serDe.deserialize(t);
      ObjectInspector rowOI = serDe.getObjectInspector();

      System.out.println("Deserialized row: " + row);

    } catch (Throwable e) {
      e.printStackTrace();
      throw e;
    }
  }

}
