Hive
====

This is a quick example on how you could make the logfiles directly accessible through Hive.

    ADD JAR target/httpdlog-serde-1.7-SNAPSHOT-job.jar;

    CREATE EXTERNAL TABLE nbasjes.clicks (
         ip           STRING
        ,timestamp    BIGINT
        ,useragent    STRING
        ,referrer     STRING
        ,bui          STRING
        ,screenHeight BIGINT
        ,screenWidth  BIGINT
    )
    ROW FORMAT SERDE 'nl.basjes.parse.apachehttpdlog.ApacheHttpdlogDeserializer'
    WITH SERDEPROPERTIES (
        "logformat"       = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" %T %V"
        ,"map:request.firstline.uri.query.g"="HTTP.URI"
        ,"map:request.firstline.uri.query.r"="HTTP.URI"

        ,"field:timestamp" = "TIME.EPOCH:request.receive.time.epoch"
        ,"field:ip"        = "IP:connection.client.host"
        ,"field:useragent" = "HTTP.USERAGENT:request.user-agent"

        ,"field:referrer"  = "STRING:request.firstline.uri.query.g.query.referrer"
        ,"field:bui"       = "HTTP.COOKIE:request.cookies.bui"

        ,"load:nl.basjes.pig.input.apachehttpdlog.ScreenResolutionDissector" = "x"
        ,"map:request.firstline.uri.query.s" = "SCREENRESOLUTION"
        ,"field:screenHeight" = "SCREENHEIGHT:request.firstline.uri.query.s.height"
        ,"field:screenWidth"  = "SCREENWIDTH:request.firstline.uri.query.s.width"
    )
    STORED AS TEXTFILE
    LOCATION "/user/nbasjes/clicks";

