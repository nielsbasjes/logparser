Hive
====

The SerDe (it's really only a Deserializer) can be used present an Apache HTTPD logfile as a table in Hive.

This is an annotated example on how you could make the logfiles directly accessible through Hive.

First we must ensure that Hive has the right jar file available. This can be either using the ADD JAR option in the Hive Cli
 or by installing it on the cluster.

    ADD JAR target/httpdlog-serde-2.8-udf.jar;

We can now define an external table with column types are STRING, BIGINT and DOUBLE.

    CREATE EXTERNAL TABLE nbasjes.clicks (
         ip           STRING
        ,timestamp    BIGINT
        ,useragent    STRING
        ,referrer     STRING
        ,bui          STRING
        ,screenHeight BIGINT
        ,screenWidth  BIGINT
    )

Of course we must specify the class name of the Deserializer that does the heavy lifting.

    ROW FORMAT SERDE 'nl.basjes.parse.apachehttpdlog.ApacheHttpdlogDeserializer'

The big part of the config lies in the SERDEPROPERTIES.

There are currently 4 types of options you can/must put in there:

- "logformat" = "[Apache httpd logformat]"
- "field:[columnname]" = "[Field]"
- "map:[field]" = "[new type]"
- "load:[classname that implements Dissector]" = "[initialization string send to the initializeFromSettingsParameter method]"

Note that the order of various settings in the SERDEPROPERTIES is irrelevant.

    WITH SERDEPROPERTIES (

**"logformat" = "[Apache httpd logformat]"**

This is the Logformat specification straight from the apache httpd config file.

        "logformat"       = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Cookie}i\" %T %V"

**"field:[columnname]" = "[Field]"**

For each column this type of property is needed for the system to know where to get the content from.

        ,"field:timestamp" = "TIME.EPOCH:request.receive.time.epoch"
        ,"field:ip"        = "IP:connection.client.host"
        ,"field:useragent" = "HTTP.USERAGENT:request.user-agent"

**"map:[field]" = "[new type]"**

Only used when mapping a specific field to a different type.

        ,"map:request.firstline.uri.query.g"="HTTP.URI"
        ,"map:request.firstline.uri.query.r"="HTTP.URI"

        ,"field:referrer"  = "STRING:request.firstline.uri.query.g.query.referrer"
        ,"field:bui"       = "HTTP.COOKIE:request.cookies.bui"

**"load:[classname that implements Dissector]" = "[initialization string send to the initializeFromSettingsParameter method]"**

Only used when there is a custom Dissector implementation that needs to be loaded in addition to the regular Dissectors.

        ,"load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector" = "x"
        ,"map:request.firstline.uri.query.s" = "SCREENRESOLUTION"
        ,"field:screenHeight" = "SCREENHEIGHT:request.firstline.uri.query.s.height"
        ,"field:screenWidth"  = "SCREENWIDTH:request.firstline.uri.query.s.width"
    )

Finally we define that this is stored as a TEXTFILE and where the files are located.

    STORED AS TEXTFILE
    LOCATION "/user/nbasjes/clicks";


Complete example
====

    ADD JAR target/httpdlog-serde-2.8-udf.jar;

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

        ,"field:timestamp" = "TIME.EPOCH:request.receive.time.epoch"
        ,"field:ip"        = "IP:connection.client.host"
        ,"field:useragent" = "HTTP.USERAGENT:request.user-agent"

        ,"map:request.firstline.uri.query.g"="HTTP.URI"
        ,"map:request.firstline.uri.query.r"="HTTP.URI"

        ,"field:referrer"  = "STRING:request.firstline.uri.query.g.query.referrer"
        ,"field:bui"       = "HTTP.COOKIE:request.cookies.bui"

        ,"load:nl.basjes.parse.httpdlog.dissectors.ScreenResolutionDissector" = "x"
        ,"map:request.firstline.uri.query.s" = "SCREENRESOLUTION"
        ,"field:screenHeight" = "SCREENHEIGHT:request.firstline.uri.query.s.height"
        ,"field:screenWidth"  = "SCREENWIDTH:request.firstline.uri.query.s.width"
    )
    STORED AS TEXTFILE
    LOCATION "/user/nbasjes/clicks";

License
===
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
