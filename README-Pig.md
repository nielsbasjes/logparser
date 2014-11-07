Usage (Overview)
===
The framework needs two things:

- The format specification in which the logfile was written (straight from the original apache httpd config file).
- The identifiers for the fields that you want

Usage (Pig)
===

Usage (PIG)
===
You simply register the httpdlog-pigloader-1.0-SNAPSHOT-job.jar

    REGISTER target/httpdlog-pigloader-1.0-SNAPSHOT-job.jar

**Step 1: What CAN we get from this line?**

Call the loader with a dummy file (must exist, won't be read) and the parameter called 'fields'.

    Fields = 
      LOAD 'test.pig' -- Any file as long as it exists 
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
        'Fields' ) AS (fields);
    
    DUMP Fields;

This will return a list of all possible fields in a format that is almost a copy-paste away from
working code.
The output of this command looks like this:

    Clicks =
        LOAD 'access.log'
        USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',

            'IP:connection.client.host',
            'NUMBER:connection.client.logname',
            'STRING:connection.client.user',
            'TIME.STAMP:request.receive.time',
            'TIME.DAY:request.receive.time.day',
            'TIME.MONTHNAME:request.receive.time.monthname',
            'TIME.MONTH:request.receive.time.month',
            'TIME.WEEK:request.receive.time.weekofweekyear',
            'TIME.YEAR:request.receive.time.weekyear',
            'TIME.YEAR:request.receive.time.year',          
            'TIME.HOUR:request.receive.time.hour',
            'TIME.MINUTE:request.receive.time.minute',
            'TIME.SECOND:request.receive.time.second',
            'TIME.MILLISECOND:request.receive.time.millisecond',
            'TIME.ZONE:request.receive.time.timezone',
            'TIME.EPOCH:request.receive.time.epoch',
            'HTTP.FIRSTLINE:request.firstline',
            'HTTP.METHOD:request.firstline.method',
            'HTTP.URI:request.firstline.uri',
            'HTTP.PROTOCOL:request.firstline.uri.protocol',
            'HTTP.USERINFO:request.firstline.uri.userinfo',
            'HTTP.HOST:request.firstline.uri.host',
            'HTTP.PORT:request.firstline.uri.port',
            'HTTP.PATH:request.firstline.uri.path',
            'HTTP.QUERYSTRING:request.firstline.uri.query',
            'STRING:request.firstline.uri.query.*',         -- You cannot put a * here yet. You MUST specify a specific field.
            'HTTP.REF:request.firstline.uri.ref',
            'HTTP.PROTOCOL:request.firstline.protocol',
            'HTTP.PROTOCOL.VERSION:request.firstline.protocol.version',
            'STRING:request.status.last',
            'BYTES:response.body.bytesclf',
            'HTTP.URI:request.referer',
            'HTTP.PROTOCOL:request.referer.protocol',
            'HTTP.USERINFO:request.referer.userinfo',
            'HTTP.HOST:request.referer.host',
            'HTTP.PORT:request.referer.port',
            'HTTP.PATH:request.referer.path',
            'HTTP.QUERYSTRING:request.referer.query',
            'STRING:request.referer.query.*',       -- You cannot put a * here yet. You MUST specify a specific field.
            'HTTP.REF:request.referer.ref',
            'HTTP.USERAGENT:request.user-agent')
        AS (
            connection_client_host:chararray,
            connection_client_logname:long,
            connection_client_user:chararray,
            request_receive_time:chararray,
            request_receive_time_day:long,
            request_receive_time_monthname:chararray,
            request_receive_time_month:long,
            request_receive_time_weekofweekyear:long,
            request_receive_time_weekyear:long,
            request_receive_time_year:long,
            request_receive_time_hour:long,
            request_receive_time_minute:long,
            request_receive_time_second:long,
            request_receive_time_millisecond:long,
            request_receive_time_timezone:chararray,
            request_receive_time_epoch:long,
            request_firstline:chararray,
            request_firstline_method:chararray,
            request_firstline_uri:chararray,
            request_firstline_uri_protocol:chararray,
            request_firstline_uri_userinfo:chararray,
            request_firstline_uri_host:chararray,
            request_firstline_uri_port:long,
            request_firstline_uri_path:chararray,
            request_firstline_uri_query:chararray,
            request_firstline_uri_query_*:chararray,        -- You cannot put a * here yet. You MUST specify name.
            request_firstline_uri_ref:chararray,
            request_firstline_protocol:chararray,
            request_firstline_protocol_version:chararray,
            request_status_last:chararray,
            response_body_bytesclf:long,
            request_referer:chararray,
            request_referer_protocol:chararray,
            request_referer_userinfo:chararray,
            request_referer_host:chararray,
            request_referer_port:long,
            request_referer_path:chararray,
            request_referer_query:chararray,
            request_referer_query_*:chararray,      -- You cannot put a * here yet. You MUST specify name.
            request_referer_ref:chararray,
            request_user-agent:chararray);

As you can see most values are 'chararray' but some are of type 'long' or 'double'.
Note that where a '*' appears this means there are many possible values that can
appear there (for example the keys of a query string in a URL).
Also note that some of the lines have a comment that you must make a choice before you can proceed.
The current version does not yet handle those wildcards.

**Step 2 Use the parser in your application.**

Now that we have all the possible values that CAN be produced from this logformat we simply choose
the ones we need and tell the Loader we want those.
    
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
        ConnectionClientHost:chararray,
        RequestFirstlineUri:chararray,
        RequestFirstlineUriQueryFoo:chararray,
        RequestStatusLast:chararray,
        ResponseBodyBytesclf:long,
        RequestReferer:chararray,
        RequestRefererQueryFoo:chararray,
        RequestUseragent:chararray);
    
From here you can do as you want with the resulting tuples. Note that almost everything is output
as a chararray, yet things that are numerical are output as longs or doubles.

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then simply contact me and we'll talk about this.
