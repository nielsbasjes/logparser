Usage (Overview)
===
The framework needs two things:

- The format specification in which the logfile was written (straight from the original apache httpd config file).
- The identifiers for the fields that you want

Usage (Pig)
===
You simply register the httpdlog-pigloader-1.8.jar

    REGISTER httpdlog-pigloader-*.jar

**Step 1: What CAN we get from this line?**

Call the loader with a dummy file (must exist, won't be read) and the parameter called 'fields'.

    Fields = 
      LOAD 'test.pig' -- Any file as long as it exists 
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
        'Fields' );
    
    DUMP Fields;

This will return a list of all possible fields in a format that is simply all the possible fields.
In version 1.6 an option was added to get this output in a copy-paste step away from (almost)
working code.


    Example = 
      LOAD 'test.pig' -- Any file as long as it exists 
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
        'Example' );
    
    DUMP Example;

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
            'TIME.DAY:request.receive.time.day_utc',
            'TIME.MONTHNAME:request.receive.time.monthname_utc',
            'TIME.MONTH:request.receive.time.month_utc',
            'TIME.WEEK:request.receive.time.weekofweekyear_utc',
            'TIME.YEAR:request.receive.time.weekyear_utc',
            'TIME.YEAR:request.receive.time.year_utc',
            'TIME.HOUR:request.receive.time.hour_utc',
            'TIME.MINUTE:request.receive.time.minute_utc',
            'TIME.SECOND:request.receive.time.second_utc',
            'TIME.MILLISECOND:request.receive.time.millisecond_utc',
            'HTTP.FIRSTLINE:request.firstline',
            'HTTP.METHOD:request.firstline.method',
            'HTTP.URI:request.firstline.uri',
            'HTTP.PROTOCOL:request.firstline.uri.protocol',
            'HTTP.USERINFO:request.firstline.uri.userinfo',
            'HTTP.HOST:request.firstline.uri.host',
            'HTTP.PORT:request.firstline.uri.port',
            'HTTP.PATH:request.firstline.uri.path',
            'HTTP.QUERYSTRING:request.firstline.uri.query',
            'STRING:request.firstline.uri.query.*',  -- If you only want a single field replace * with name and change type to chararray',
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
            'STRING:request.referer.query.*',  -- If you only want a single field replace * with name and change type to chararray',
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
            request_receive_time_day_utc:long,
            request_receive_time_monthname_utc:chararray,
            request_receive_time_month_utc:long,
            request_receive_time_weekofweekyear_utc:long,
            request_receive_time_weekyear_utc:long,
            request_receive_time_year_utc:long,
            request_receive_time_hour_utc:long,
            request_receive_time_minute_utc:long,
            request_receive_time_second_utc:long,
            request_receive_time_millisecond_utc:long,
            request_firstline:chararray,
            request_firstline_method:chararray,
            request_firstline_uri:chararray,
            request_firstline_uri_protocol:chararray,
            request_firstline_uri_userinfo:chararray,
            request_firstline_uri_host:chararray,
            request_firstline_uri_port:long,
            request_firstline_uri_path:chararray,
            request_firstline_uri_query:chararray,
            request_firstline_uri_query__:map[],  -- If you only want a single field replace * with name and change type to chararray,
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
            request_referer_query__:map[],  -- If you only want a single field replace * with name and change type to chararray,
            request_referer_ref:chararray,
            request_user_agent:chararray);

As you can see most values are 'chararray' but some are of type 'long' or 'double'.
Note that where a '*' appears this means there are many possible values that can
appear there (for example the keys of a query string in a URL).
Also note that some of the lines have a comment that you must make a choice before you can proceed.
The current version does not yet handle those wildcards.

**Step 2 Use the parser in your application.**

Now that we have all the possible values that CAN be produced from this logformat we simply choose
the ones we need and tell the Loader we want those.
As you can see in the next example the order of the requested fields is irrelevant in the sense that it only has to match the list of atom names in the 'AS' specification.
    
    Clicks = 
      LOAD 'access_log.gz' 
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
        'HTTP.URI:request.firstline.uri',
        'STRING:request.firstline.uri.query.foo',
        'STRING:request.firstline.uri.query.bar',
        'STRING:request.status.last',
        'BYTES:response.body.bytesclf',
        'HTTP.URI:request.referer',
        'STRING:request.referer.query.foo',
        'IP:connection.client.host',
        'HTTP.USERAGENT:request.user-agent')
    
        AS ( 
        Uri:chararray,
        Foo:chararray,
        Bar:chararray,
        Status:chararray,
        BodyBytes:long,
        Referer:chararray,
        RefererFoo:chararray,
        ClientIP:chararray,
        Useragent:chararray);

From here you can do as you want with the resulting tuples. Note that almost everything is output
as a chararray, yet things that are numerical are output as longs or doubles.

Loading custom dissectors
===
If you have written a custom dissector it is now possible to load this from pig and use it to it's full capabilities.
The extra dissector is specified by adding an extra parameter like this:

   -load:<name of the class>:<parameter string>

You must REGISTER the jar file that contains the class. The <parameter string> is used to call the initializeFromSettingsParameter(String settings) method.

Example:
Assume we have the Dissector com.example.fooSplitter then we may so something like this:

    REGISTER foosplitter-1.0.jar

    Clicks =
      LOAD 'access_log.gz'
      USING nl.basjes.pig.input.apachehttpdlog.Loader(
        'common',
        '-map:request.firstline.uri.query.foo:FOO',
        '-load:com.example.fooSplitter:Some:settings:for:the:foo:Splitter',
        'FOO.SPLIT:request.firstline.uri.query.foo.split'
        )
        AS ( fooSplitValue:chararray )

Some explanation:

We're parsing the common logformat. Which may give us the foo query parameter as

    STRING:request.firstline.uri.query.foo

so we map this field to the type name the new dissector can pick apart.

    '-map:request.firstline.uri.query.foo:FOO',
    '-load:com.example.fooSplitter:Some:settings:for:the:foo:Splitter',

and we ask for the end result (which is found by asking for the special 'Fields' value)

    'FOO.SPLIT:request.firstline.uri.query.foo.split'

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then
simply contact me and we'll talk about this.
