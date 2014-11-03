REGISTER ../../httpdlog/httpdlog-pigloader/target/httpdlog-pigloader-*.jar

--Fields =
--  LOAD 'access_log.gz' -- Any file as long as it exists
--  USING nl.basjes.pig.input.apachehttpdlog.Loader(
--    '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',
--    'Fields' ) AS (fields);
--
--DESCRIBE Fields;
--DUMP Fields;

Clicks = 
  LOAD 'access_log.gz' 
  USING nl.basjes.pig.input.apachehttpdlog.Loader(
    '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"',

    'IP:connection.client.host',
    'NUMBER:connection.client.logname',
    'STRING:connection.client.user',
    'TIME.STAMP:request.receive.time',
    'TIME.DAY:request.receive.time.day',
    'TIME.MONTHNAME:request.receive.time.monthname',
    'TIME.MONTH:request.receive.time.month',
    'TIME.YEAR:request.receive.time.year',
    'TIME.HOUR:request.receive.time.hour',
    'TIME.MINUTE:request.receive.time.minute',
    'TIME.SECOND:request.receive.time.second',
    'TIME.ZONE:request.receive.time.timezone',
    'HTTP.FIRSTLINE:request.firstline',
    'HTTP.METHOD:request.firstline.method',
    'HTTP.URI:request.firstline.uri',
    'HTTP.QUERYSTRING:request.firstline.uri.query',
    'STRING:request.firstline.uri.query.foo',
    'HTTP.PROTOCOL:request.firstline.protocol',
    'HTTP.PROTOCOL.VERSION:request.firstline.protocol.version',
    'STRING:request.status.last',
    'BYTES:response.body.bytesclf',
    'HTTP.URI:request.referer',
    'HTTP.QUERYSTRING:request.referer.query',
    'STRING:request.referer.query.foo',
    'HTTP.USERAGENT:request.user-agent')

    AS (
    ConnectionClientHost,
    ConnectionClientLogname,
    ConnectionClientUser,
    RequestReceiveTime,
    RequestReceiveTimeDay,
    RequestReceiveTimeMonthname,
    RequestReceiveTimeMonth,
    RequestReceiveTimeYear,
    RequestReceiveTimeHour,
    RequestReceiveTimeMinute,
    RequestReceiveTimeSecond,
    RequestReceiveTimeTimezone,
    RequestFirstline,
    RequestFirstlineMethod,
    RequestFirstlineUri,
    RequestFirstlineUriQuery,
    RequestFirstlineUriQueryFoo,
    RequestFirstlineProtocol,
    RequestFirstlineProtocolVersion,
    RequestStatusLast,
    ResponseBodyBytesclf:long,
    RequestReferer,
    RequestRefererQuery,
    RequestRefererQueryFoo,
    RequestUseragent:chararray);

DESCRIBE Clicks;
DUMP Clicks;

GroupedClicks =
  GROUP Clicks
  BY RequestUseragent;

CountedClicks =
  FOREACH GroupedClicks
  GENERATE group AS useragent:chararray,
           COUNT(Clicks) AS count,
           SUM(Clicks.ResponseBodyBytesclf) AS TotalResponseBytes;

DESCRIBE CountedClicks;
DUMP CountedClicks;
