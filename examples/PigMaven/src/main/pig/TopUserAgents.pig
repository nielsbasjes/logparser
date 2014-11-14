REGISTER lib/*.jar;

%declare LOGFILE   '${ACCESS_LOGPATH}/access-2014-11-11.log.gz'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

UserAgents =
  LOAD '$LOGFILE'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
            'HTTP.USERAGENT:request.user-agent'
        ) AS (
            useragent:chararray
        );

UserAgentsCount =
    FOREACH  UserAgents
    GENERATE useragent AS useragent:chararray,
             1L        AS clicks:long;

CountsPerUseragents =
    GROUP UserAgentsCount
    BY    (useragent);

SumsPerBrowser =
    FOREACH  CountsPerUseragents
    GENERATE SUM(UserAgentsCount.clicks) AS clicks,
             group                       AS useragent;

STORE SumsPerBrowser
    INTO  'TopUseragents'
    USING org.apache.pig.piggybank.storage.CSVExcelStorage('	','NO_MULTILINE', 'UNIX');

