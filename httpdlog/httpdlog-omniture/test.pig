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

-- The output given the provided sample.log files will be:
-- ConnectionClientHost = 10.20.30.40
-- RequestReceiveTime   = 03/Apr/2013:00:00:11 +0200]
-- Referrer             = ADVNLGOT002010100200499999999
-- GoogleQuery          = the dark secret of harvest home dvd
-- BUI                  = DummyValueBUI
-- RequestUseragent     = Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0))


