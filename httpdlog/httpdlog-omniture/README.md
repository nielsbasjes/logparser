Apache HTTPD logparser (Omniture specific)
===
At the company where I work we get a copy of the tags that are sent to Omniture.
With this very simple adaptation it is now possible to easily retrieve the fields that are logged this way.
One of the main reasons for this is that when using this kind of logging of requests the querystring parameters of the original Url are encoded into a querystring parameter in the logging url.
In this case this is the 'g' parameter. So now you can do this

        'STRING:request.firstline.uri.query.g.query.referrer'

And get the parameter called 'Referrer' from the original url.

Usage (PIG)
===
You simply register the httpdlog-omniture-*-job.jar

    REGISTER target/httpdlog-omniture-*-job.jar
    
And then call the loader with a dummy file (must exist, won't be read) and the parameter called 'fields'. This will return a list of all possible fields. Note that weher a '*' appears this means there are many possible values that can appear there (for example the keys of a query string in a URL).
As you can see there is a kinda sloppy type mechanism to stear the parsing, don't change that as the persing really relies on this.
    
    %declare LOGFORMAT '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i" "%{Cookie}i" %T'

    Fields = 
      LOAD 'test.pig' -- Any file as long as it exists 
      USING nl.basjes.pig.input.apachehttpdlog.omniture.OmnitureLoader(
        '$LOGFORMAT',
        'Fields' ) AS (fields);

    DESCRIBE Fields;
    DUMP Fields;

Now that we have all the possible values that CAN be produced from this logformat we simply choose the ones we need and tell the Loader we want those.
    
    Clicks = 
      LOAD 'sample.log' 
      USING nl.basjes.pig.input.apachehttpdlog.omniture.OmnitureLoader(
        '$LOGFORMAT',
        'IP:connection.client.host',
        'TIME.STAMP:request.receive.time',
        'STRING:request.firstline.uri.query.g.query.referrer',
        'STRING:request.firstline.uri.query.s',
        'STRING:request.firstline.uri.query.r.query.q',
        'HTTP.COOKIE:request.cookies.bui',
        'HTTP.USERAGENT:request.user-agent')
        AS ( 
        ConnectionClientHost,
        RequestReceiveTime,
        Referrer,
        ScreenResolution,
        GoogleQuery,
        BUI,
        RequestUseragent);
    
    DESCRIBE Clicks;
    DUMP Clicks;
    
From here you can do as you want with the resulting tuples. Note that almost everything is output as a chararray, yet things that seem like number (based on the sloppy typing) are output as longs.

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then simply contact me and we'll talk about this.
