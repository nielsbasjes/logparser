REGISTER lib/*.jar;

%declare LOGFILE   '${ACCESS_LOGPATH}/access*.gz'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

Fields =
  LOAD 'fields.sh'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
            'example',
            '-map:request.firstline.uri.query.g:HTTP.URI',
            '-map:request.firstline.uri.query.r:HTTP.URI',
            '-load:nl.basjes.parse.UrlClassDissector:'
        ) AS (
            fields:chararray
        );

DUMP Fields;

