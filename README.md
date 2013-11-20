Apache HTTPD logparser
===
This is a Logparsing framework intended to make parsing Apache HTTPD logfiles much easier.

The basic idea is that you should be able to have a parser that you can construct by simply 
telling it with what configuration options the line was written.

Building
===
Simply type : mvn package
and the whole thing should build.

Java, Hadoop & PIG
===
I'm a big user of bigdata tools like pig and Hadoop.
So in here are also an Hadoop inputformat and a Pig Loader that are wrappers around this library.

Usage (Overview)
===
The framework needs two things:

- The format specification in which the logfile was written (straight from the original apache httpd config file).
- The identifiers for the fields that you want   

Usage (Java)
===
For the Java API there is an annotation based parser.

I assume we have a logformat variable that looks something like this:

    String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";

**Step 1: What CAN we get from this line?**

To figure out what values we CAN get from this line we instantiate the parser with a dummy class that does not have ANY @Field annotations.

    Parser<Dummy> dummyParser = new ApacheHttpdLoglineParser<Dummy>(Dummy.class, logformat);
    List<String> possiblePaths = dummyParser.getPossiblePaths();
    for (String path: possiblePaths) {
        System.out.println(path);
    }

You will get a list that looks something like this:

    IP:connection.client.host
    NUMBER:connection.client.logname
    STRING:connection.client.user
    TIME.STAMP:request.receive.time
    TIME.DAY:request.receive.time.day
    TIME.MONTHNAME:request.receive.time.monthname
    TIME.MONTH:request.receive.time.month
    TIME.YEAR:request.receive.time.year
    TIME.HOUR:request.receive.time.hour
    TIME.MINUTE:request.receive.time.minute
    TIME.SECOND:request.receive.time.second
    TIME.MILLISECOND:request.receive.time.millisecond
    TIME.ZONE:request.receive.time.timezone
    HTTP.FIRSTLINE:request.firstline
    HTTP.METHOD:request.firstline.method
    HTTP.URI:request.firstline.uri
    HTTP.QUERYSTRING:request.firstline.uri.query
    STRING:request.firstline.uri.query.*
    HTTP.PROTOCOL:request.firstline.protocol
    HTTP.PROTOCOL.VERSION:request.firstline.protocol.version
    STRING:request.status.last
    BYTES:response.body.bytesclf
    HTTP.URI:request.referer
    HTTP.QUERYSTRING:request.referer.query
    STRING:request.referer.query.*
    HTTP.USERAGENT:request.user-agent

Now some of these lines contain a * . 
This is a wildcard that can be replaced with any 'name'.

**Step 2 Create the receiving POJO** 

We need to create the receiving record class that is simply a POJO that does not need any interface or inheritance. 
In this class we create setters that will be called when the specified field has been found in the line.

So we can now add to this class a setter that simply receives a single value: 

    @Field("IP:connection.client.host")
    public void setIP(final String value) {
        ip = value;
    }

If we really want the name of the field we can also do this

    @Field("STRING:request.firstline.uri.query.img")
    public void setQueryImg(final String name, final String value) {
        results.put(name, value);
    }

This latter form is very handy because this way we can obtain all values for a wildcard field

    @Field("STRING:request.firstline.uri.query.*")
    public void setQueryStringValues(final String name, final String value) {
        results.put(name, value);
    }

Or a combination of the above examples

    @Field({"IP:connection.client.host", 
            "STRING:request.firstline.uri.query.*"})
    public void setValue(final String name, final String value) {
        results.put(name, value);
    }

**Step 3 Use the parser in your application.**

You create an instance of the parser

        Parser<MyRecord> parser = new ApacheHttpdLoglineParser<MyRecord>(MyRecord.class, logformat);

And then call the parse method repeatedly for each line.
You can do this like this (for each call a new instance of "MyRecord" is instantiated !!):

        MyRecord record = parser.parse(logline);
 
Or you can call it like this:
Only once:

        MyRecord record = new MyRecord(); 

And then for each logline:

        record.clear(); // Which is up to you to implement to 'reset' the record to it's initial state.
        parser.parse(record, logline);

Notes about the setters

- Only if a value exists in the actual logline the setter will be called (mainly relevant if you want to get a specific query param or cookie).
- If you specifiy the same field on several setters then each of these setters will be called.

Have a look at the 'httpdlog-testclient' for a working example.

Usage (PIG)
===
You simply register the httpdlog-pigloader-1.0-SNAPSHOT-job.jar

    REGISTER target/httpdlog-pigloader-1.0-SNAPSHOT-job.jar
    
And then call the loader with a dummy file (must exist, won't be read) and the parameter called 'fields'. This will return a list of all possible fields. Note that weher a '*' appears this means there are many possible values that can appear there (for example the keys of a query string in a URL).
As you can see there is a kinda sloppy type mechanism to stear the parsing, don't change that as the persing really relies on this.
    
    Fields = 
      LOAD 'test.pig' -- Any file as long as it exists 
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
        'Fields' ) AS (fields);
    
    DESCRIBE Fields;
    DUMP Fields;
    
Now that we have all the possible values that CAN be produced from this logformat we simply choose the ones we need and tell the Loader we want those.
    
    Clicks = 
      LOAD 'access_log.gz' 
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
        'IP:connection.client.host',
        'HTTP.URI:request.firstline.uri',
        'STRING:request.firstline.uri.query.foo',
        'STRING:request.status.last',
        'HTTP.URI:request.referer',
        'STRING:request.referer.query.foo',
        'HTTP.USERAGENT:request.user-agent')
    
        AS ( 
        ConnectionClientHost,
        RequestFirstlineUri,
        RequestFirstlineUriQueryFoo,
        RequestStatusLast,
        RequestReferer,
        RequestRefererQueryFoo,
        RequestUseragent);
    
From here you can do as you want with the resulting tuples. Note that almost everything is output as a chararray, yet things that seem like number (based on the sloppy typing) are output as longs.

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then simply contact me and we'll talk about this.
