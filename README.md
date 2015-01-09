Apache HTTPD logparser
===
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

* Modifiers: "%400,501{User-agent}i"
* %{format}t : The time, in the form given by format, which should be in strftime(3) format. (potentially localized)

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

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then
simply contact me and we'll talk about this.
