-- A simple example on counting the number occurrances of cookies

-- Simply register everything in the lib directory
REGISTER lib/*.jar;

-- Get the right values (mostly from the property file)
%declare LOGFILE   '${ACCESS_LOGPATH}/omniture.log'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

Clicks =
  LOAD '${LOGFILE}'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
        'HTTP.COOKIE:request.cookies.*'
        )
    AS  (
        AllCookies:map[]
        );

CookieNames =
    FOREACH     Clicks
    GENERATE    FLATTEN(KEYSET(AllCookies)) AS CookieName:chararray,
                1L                          AS count:long;

GroupedCookieNames =
    GROUP       CookieNames
    BY          (CookieName);

CountedCookies =
    FOREACH     GroupedCookieNames
    GENERATE    group                       AS CookieName:chararray,
                SUM(CookieNames.count)      AS count:long;

DUMP CountedCookies;
