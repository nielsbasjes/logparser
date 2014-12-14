REGISTER lib/*.jar;

%declare LOGFILE   '${ACCESS_LOGPATH}/access*.gz'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

Fields =
  LOAD 'fields.sh'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
            'fields',
            '-load:nl.basjes.parse.UrlClassDissector:'
        ) AS (
            fields:chararray
        );

DUMP Fields;

