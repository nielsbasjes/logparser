REGISTER target/httpdlog-omniture-1.0-SNAPSHOT-job.jar

%declare LOGFORMAT '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i" "%{Cookie}i" %T'

Fields = 
  LOAD 'test.pig' -- Any file as long as it exists 
  USING nl.basjes.pig.input.apachehttpdlog.omniture.OmnitureLoader(
    '$LOGFORMAT',
    'Fields' ) AS (fields);

DESCRIBE Fields;
DUMP Fields;


Clicks = 
  LOAD 'sample.log' 
  USING nl.basjes.pig.input.apachehttpdlog.omniture.OmnitureLoader(
    '$LOGFORMAT',
    'IP:connection.client.host',
    'TIME.STAMP:request.receive.time',
    'STRING:request.firstline.uri.query.g.query.referrer',
    'STRING:request.firstline.uri.query.r.query.q',
    'HTTP.COOKIE:request.cookies.bui',
    'HTTP.USERAGENT:request.user-agent')

    AS ( 
    ConnectionClientHost,
    RequestReceiveTime,
    Referrer,
    GoogleQuery,
    BUI,
    RequestUseragent);

DESCRIBE Clicks;
DUMP Clicks;

