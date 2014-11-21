package http.disectors;

import nl.basjes.parse.apachehttpdlog.ApacheHttpdLoglineParser;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DisectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDisectorException;
import nl.basjes.parse.core.exceptions.MissingDisectorsException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class TestHttpUriDisector {
  public static class MyRecord {

    private Map<String, String> results = new HashMap<String, String>(32);

    @Field({
            "HTTP.URI:request.referer",
            "HTTP.PROTOCOL:request.referer.protocol",
            "HTTP.USERINFO:request.referer.userinfo",
            "HTTP.HOST:request.referer.host",
            "HTTP.PORT:request.referer.port",
            "HTTP.PATH:request.referer.path",
            "HTTP.QUERYSTRING:request.referer.query",
            "HTTP.REF:request.referer.ref",
    })
    public void setValue(final String name, final String value) {
      results.put(name, value);
    }

    public String getValue(final String name) {
      return results.get(name);
    }

    public void clear() {
      results.clear();
    }

  }

  static private Parser<MyRecord> parser;
  static private MyRecord record;

  @BeforeClass
  static public void setUp() throws ParseException {
    String logformat = "%{Referer}i";
    parser = new ApacheHttpdLoglineParser<>(MyRecord.class, logformat);
    record = new MyRecord();
  }

  @Test
  public void testFullUrl1() throws ParseException, InvalidDisectorException, MissingDisectorsException, DisectionFailure {
    parser.parse(record,"http://www.example.com/some/thing/else/index.html?foofoo=barbar");

    assertEquals("Full input","http://www.example.com/some/thing/else/index.html?foofoo=barbar", record.getValue("HTTP.URI:request.referer"));
    assertEquals("Protocol is wrong","http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
    assertEquals("Userinfo is wrong",null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
    assertEquals("Host is wrong","www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
    assertEquals("Port is wrong",null, record.getValue("HTTP.PORT:request.referer.port"));
    assertEquals("Path is wrong","/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
    assertEquals("QueryString is wrong","&foofoo=barbar", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
    assertEquals("Ref is wrong",null, record.getValue("HTTP.REF:request.referer.ref"));
  }

  @Test
  public void testFullUrl2() throws ParseException, InvalidDisectorException, MissingDisectorsException, DisectionFailure {
    record.clear();
    parser.parse(record, "http://www.example.com/some/thing/else/index.html&aap=noot?foofoo=barbar&");

    assertEquals("Full input","http://www.example.com/some/thing/else/index.html&aap=noot?foofoo=barbar&", record.getValue("HTTP.URI:request.referer"));
    assertEquals("Protocol is wrong","http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
    assertEquals("Userinfo is wrong",null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
    assertEquals("Host is wrong","www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
    assertEquals("Port is wrong",null, record.getValue("HTTP.PORT:request.referer.port"));
    assertEquals("Path is wrong","/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
    assertEquals("QueryString is wrong","&aap=noot&foofoo=barbar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
    assertEquals("Ref is wrong",null, record.getValue("HTTP.REF:request.referer.ref"));
  }

  @Test
  public void testFullUrl3() throws ParseException, InvalidDisectorException, MissingDisectorsException, DisectionFailure {
    record.clear();
    parser.parse(record, "http://www.example.com:8080/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla");

    assertEquals("Full input","http://www.example.com:8080/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla", record.getValue("HTTP.URI:request.referer"));
    assertEquals("Protocol is wrong","http", record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
    assertEquals("Userinfo is wrong",null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
    assertEquals("Host is wrong","www.example.com", record.getValue("HTTP.HOST:request.referer.host"));
    assertEquals("Port is wrong","8080", record.getValue("HTTP.PORT:request.referer.port"));
    assertEquals("Path is wrong","/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
    assertEquals("QueryString is wrong","&aap=noot&foofoo=barbar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
    assertEquals("Ref is wrong","blabla", record.getValue("HTTP.REF:request.referer.ref"));
  }

  @Test
  public void testFullUrl4() throws ParseException, InvalidDisectorException, MissingDisectorsException, DisectionFailure {
    record.clear();
    parser.parse(record,"/some/thing/else/index.html?foofoo=barbar#blabla");

    assertEquals("Full input","/some/thing/else/index.html?foofoo=barbar#blabla", record.getValue("HTTP.URI:request.referer"));
    assertEquals("Protocol is wrong",null, record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
    assertEquals("Userinfo is wrong",null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
    assertEquals("Host is wrong",null, record.getValue("HTTP.HOST:request.referer.host"));
    assertEquals("Port is wrong",null, record.getValue("HTTP.PORT:request.referer.port"));
    assertEquals("Path is wrong","/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
    assertEquals("QueryString is wrong","&foofoo=barbar", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
    assertEquals("Ref is wrong","blabla", record.getValue("HTTP.REF:request.referer.ref"));
  }

  @Test
  public void testFullUrl5() throws ParseException, InvalidDisectorException, MissingDisectorsException, DisectionFailure {
    record.clear();
    parser.parse(record, "/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla");

    assertEquals("Full input","/some/thing/else/index.html&aap=noot?foofoo=barbar&#blabla", record.getValue("HTTP.URI:request.referer"));
    assertEquals("Protocol is wrong",null, record.getValue("HTTP.PROTOCOL:request.referer.protocol"));
    assertEquals("Userinfo is wrong",null, record.getValue("HTTP.USERINFO:request.referer.userinfo"));
    assertEquals("Host is wrong",null, record.getValue("HTTP.HOST:request.referer.host"));
    assertEquals("Port is wrong",null, record.getValue("HTTP.PORT:request.referer.port"));
    assertEquals("Path is wrong","/some/thing/else/index.html", record.getValue("HTTP.PATH:request.referer.path"));
    assertEquals("QueryString is wrong","&aap=noot&foofoo=barbar&", record.getValue("HTTP.QUERYSTRING:request.referer.query"));
    assertEquals("Ref is wrong","blabla", record.getValue("HTTP.REF:request.referer.ref"));
  }


}
