REGISTER lib/*.jar;

%declare LOGFILE   '${ACCESS_LOGPATH}/access-2014-11-11.log.gz'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

Fields =
  LOAD 'examine.sh'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
            'fields'
        ) AS (
            fields:chararray
        );

DUMP Fields;

