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
> TODO: Document.

> For now just have a look at the unit tests to see how it can be done.


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
