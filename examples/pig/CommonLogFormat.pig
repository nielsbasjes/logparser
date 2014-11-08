REGISTER ../../httpdlog/httpdlog-pigloader/target/httpdlog-pigloader-*.jar

Fields = 
  LOAD 'access_log.gz' -- Any file as long as it exists 
  USING nl.basjes.pig.input.apachehttpdlog.Loader(
    'common', 
    'Fields' ) AS (fields);

DESCRIBE Fields;
DUMP Fields;

