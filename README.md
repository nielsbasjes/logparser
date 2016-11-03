Apache HTTPD logparser
===
[![Travis Build status](https://api.travis-ci.org/nielsbasjes/logparser.png)](https://travis-ci.org/nielsbasjes/logparser) [![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

This is a Logparsing framework intended to make parsing Apache HTTPD logfiles much easier.

The basic idea is that you should be able to have a parser that you can construct by simply
telling it with what configuration options the line was written.

So we are using the LogFormat that wrote the file as the input parameter for the parser that reads the same file.
In addition to the config options specified in the Apache HTTPD manual under
[Custom Log Formats](http://httpd.apache.org/docs/2.2/mod/mod_log_config.html) the following are also recognized:

* common
* combined
* combinedio
* referer
* agent

Currently *not yet supported features*:

* %{format}t : The time, in the form given by format, which should be in strftime(3) format. (potentially localized)

A simple workaround for this limitation for all logparser versions before 2.5: replace the **%{...}t** with **%{timestamp}i** .
You will then get this timestamp field as if it was a request header: HTTP.HEADER:request.header.timestamp

Analyze almost anything
===
I wrote this parser for practical reallife situations. In reality a lot happens that is not allowed when looking at the
official specifications, yet in production they do happen.
So several of the key parts in this parser try to recover from bad data where possible and thus allow to extract as
much usefull information as possible even if the data is not valid.
Important examples of this are invalid encoding characters and chopped multibyte encoded characters that are both
extracted as best as possible.

If you have a real logline that causes a parse error then I kindly request you to sumbit this line, the logformat and
the field that triggered the error as a bug report.

Pre built versions
===
Prebuilt versions have been deployed to maven central so using it in a project is as simple as adding a dependency.

So using it in a Java based project is as simple as adding this to your dependencies

    <dependency>
        <groupId>nl.basjes.parse.httpdlog</groupId>
        <artifactId>httpdlog-parser</artifactId>
        <version>2.7</version>
    </dependency>

In addition you need joda-time 1.6 or newer.

    <dependency>
      <groupId>joda-time</groupId>
      <artifactId>joda-time</artifactId>
      <version>1.6</version>
    </dependency>

Building
===
Simply type : mvn package
and the whole thing should build.

Java, Hadoop, PIG & Hive
===
I'm a big user of bigdata tools like pig and Hadoop.
So in here are also a Hadoop inputformat, a Pig Loader and a Hive/HCatalog Serde that are wrappers around this library.

Usage (Overview)
===
The framework needs two things:

* The format specification in which the logfile was written (straight from the original apache httpd config file).
* The identifiers for the fields that you want.

To obtain all the identifiers the system CAN extract from the specified logformat a separate
developer call exists in various languages that allows you to get the list of all possible values.

Languages
===
The languages that are supported in this version:

* [Java](README-Java.md)
* [Apache Pig](README-Pig.md)
* [Apache Hive](README-Hive.md)

Internal structure and type remapping
===
The basic model of this system is a tree.
Each node in the tree has a 'type' and a 'name'.
The 'type' is really a 'what is the format of this string' indicator. Because there are many more of those kinds of types than your average String or Long you will see a lot of different names.
The 'name' is the "breadbrumb" towards the point in the tree where this is located.

A 'Dissector' is a class that can cut a specific type (format) into a bunch of new parts that each extend the base name and have their own type.
Because internal parser is constructed at the start of running a parser this tree has some dynamic properties.
To start only the tree is constructed for the elements actually requested. This is done to avoid 'dissecting' something that is not wanted.
So the parser will have a different structure depending on the requested output.

These dynamic properties also allow 'mapping a field to a different type'. Lets illustrate what this is by looking at the most common usecase.
Assume you are trying to parse the logline for a pixel that was written by a webanalytics product. In that scenario it is common that the URL is that of a pixel and one of the query string parameters contains the actual URL. Now by default a querystring parameter gets the type STRING (which really means that is is arbitrary and cannot be dissected any further). Using this remapping (see API details per language) we can now say that a specific query string parameter really has the type HTTP.URL. As a consequence the system can now continue dissecting this specific query string parameter into things like the host, port and query string parameters.

All that is needed to map the 'g' and 'r' parameters so they are dissected further is this:

Java: Call these against the parser instance right after construction

    parser.addTypeRemapping("request.firstline.uri.query.g", "HTTP.URI", Casts.STRING_ONLY);
    parser.addTypeRemapping("request.firstline.uri.query.r", "HTTP.URI", Casts.STRING_ONLY);

Pig: Add these to the list of requested fields

    '-map:request.firstline.uri.query.g:HTTP.URI',
    '-map:request.firstline.uri.query.r:HTTP.URI',

Hive: Add these to the SERDEPROPERTIES

    "map:request.firstline.uri.query.g"="HTTP.URI",
    "map:request.firstline.uri.query.r"="HTTP.URI",

Special Dissectors
===
**mod_unique_id**

If you have a log field / request header that gets filled using mod_unique_id you can now peek inside
the values that were used to construct this.

**NOTE: http://httpd.apache.org/docs/2.2/mod/mod_unique_id.html clearly states**

     it should be emphasized that applications should not dissect the encoding.
     Applications should treat the entire encoded UNIQUE_ID as an opaque token,
     which can be compared against other UNIQUE_IDs for equality only.

When you choose to ignore the clear 'should not' statement then simply add
a type remapping to map the field to the type *MOD_UNIQUE_ID*

Parsing problems with Jetty generated logfiles
==============================================
In Jetty there is the option to create a logfile in what they call the NCSARequestLog format.
It was found that (historically) this had two formatting problems which cause parse errors:

1. If the useragent is missing the empty value is logged with an extra ' ' after it.
   The fix for this in Jetty was committed on 2016-07-27 in the Jetty 9.3.x and 9.4.x branches
2. Before jetty-9.2.4.v20141103 if there is no user available the %u field is logged as " - "
(i.e. with two extra spaces around the '-').

To workaround these problems you can easily start the parser with a two line logformat:

    ENABLE JETTY FIX
    %h %l %u %t \"%r\" %>s %b "%{Referer}i" "%{User-Agent}i" %D

This *ENABLE JETTY FIX* is a 'magic' value that causes the underlying parser to enable the workaround for both of these problems.
In order for this to work correctly the useragent field must look exactly like this: *"%{User-Agent}i"*

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
