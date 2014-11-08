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

Java, Hadoop & PIG
===
I'm a big user of bigdata tools like pig and Hadoop.
So in here are also a Hadoop inputformat and a Pig Loader that are wrappers around this library.

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

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then
simply contact me and we'll talk about this.
