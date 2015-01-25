-- Simply register everything in the lib directory
REGISTER lib/*.jar;

-- Get the right values (mostly from the property file)
%declare LOGFILE   '${ACCESS_LOGPATH}/omniture.log'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

Clicks =
  LOAD '${LOGFILE}'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
        'HTTP.PATH:request.firstline.uri.path',
        'HTTP.PATH.CLASS:request.firstline.uri.path.class',
    '-load:nl.basjes.parse.UrlClassDissector:',
        'IP:connection.client.host',
        'TIME.STAMP:request.receive.time',
    '-map:request.firstline.uri.query.g:HTTP.URI',
        'STRING:request.firstline.uri.query.g.query.promo',
        'STRING:request.firstline.uri.query.g.query.*',
        'STRING:request.firstline.uri.query.s',
    '-map:request.firstline.uri.query.r:HTTP.URI',
        'STRING:request.firstline.uri.query.r.query.blabla',
        'HTTP.COOKIE:request.cookies.bui',
        'HTTP.COOKIE:request.cookies.*',
        'HTTP.USERAGENT:request.user-agent'
        )
    AS  (
        URIPath,
        URIPathClass,
        ConnectionClientHost,
        RequestReceiveTime,
        Promo,
        QueryParams:map[],
        ScreenResolution,
        GoogleQuery,
        BUI,
        AllCookies:map[],
        RequestUseragent
        );

Clicks2 =
    FOREACH Clicks
    GENERATE  URIPath,
              AllCookies,
              QueryParams,
              QueryParams#'foo'         AS foo,
              QueryParams#'referrer'    AS ref;

DUMP Clicks2;
