REGISTER ../../httpdlog/httpdlog-pigloader/target/httpdlog-pigloader-*.jar

Fields = 
  LOAD 'test.pig' -- Any file as long as it exists 
  USING nl.basjes.pig.input.apachehttpdlog.Loader(
'"%%" "%a" "%A" "%B" "%b" "%D" "%f" "%h" "%H" "%k" "%l" "%m" "%{Foobar}i" "%{Foobar}n" "%{Foobar}o" "%p" "%{canonical}p" "%{local}p" "%{remote}p" "%P" "%{pid}P" "%{tid}P" "%{hextid}P" "%q" "%r" "%R" "%s" "%>s" "%t" "%T" "%u" "%U" "%v" "%V" "%X" "%I" "%O" "%{cookie}i" "%{set-cookie}o" "%{user-agent}i" "%{referer}i"', 
    'Fields' ) AS (fields);

DESCRIBE Fields;
DUMP Fields;

