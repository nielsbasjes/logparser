Apache HTTPD logparser
===
This is a Logparsing framework intended to make parsing Apache HTTPD logfiles much easier.

The basic idea is that you should be able to have a parser that you can construct by simply 
telling it with what configuration options the line was written.

Building
===
Simply type : mvn package
and the whole thing should build.

Java, Hadoop & PIG
===
I'm a big user of bigdata tools like pig and Hadoop.
So in here are also an Hadoop inputformat and a Pig Loader that are wrappers around this library.

Usage (Overview)
===
The framework needs two things:

- The format specification in which the logfile was written (straight from the original apache httpd config file).
- The identifiers for the fields that you want   

To obtain all the identifiers the system CAN extract from the specified logformat a separate
developer call exists in various languages that allows you to get the list of all possible values.

Languages
===
The languages that are supported in this version:
- [Java](README-Java.md)
- [Apache Pig](README-Pig.md)

License
===
This software is licenced under GPLv3. If you want to include this in a commercial (non-opensource) product then simply contact me and we'll talk about this.
