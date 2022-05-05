Apache HTTPD logparser
===
This is a Logparsing framework intended to make parsing Apache HTTPD logfiles much easier.

The basic idea is that you should be able to have a parser that you can construct by simply 
telling it with what configuration options the line was written.

Usage (Java)
===
For the Java API there is an annotation based parser.

First you put something like this in your pom.xml file:

    <dependency>
        <groupId>nl.basjes.parse.httpdlog</groupId>
        <artifactId>httpdlog-parser</artifactId>
        <version>5.8</version>
    </dependency>

I assume we have a logformat variable that looks something like this:

    String logformat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";

**Step 1: What CAN we get from this line?**

To figure out what values we CAN get from this line we instantiate the parser with a dummy class
that does not have ANY @Field annotations. The "Object" class will do just fine for this purpose.

    Parser<Object> dummyParser = new HttpdLoglineParser<Object>(Object.class, logformat);
    List<String> possiblePaths = dummyParser.getPossiblePaths();
    for (String path: possiblePaths) {
        System.out.println(path);
    }

You will get a list that looks something like this:

    IP:connection.client.host
    NUMBER:connection.client.logname
    STRING:connection.client.user
    TIME.STAMP:request.receive.time
    TIME.DAY:request.receive.time.day
    TIME.MONTHNAME:request.receive.time.monthname
    TIME.MONTH:request.receive.time.month
    TIME.YEAR:request.receive.time.year
    TIME.HOUR:request.receive.time.hour
    TIME.MINUTE:request.receive.time.minute
    TIME.SECOND:request.receive.time.second
    TIME.MILLISECOND:request.receive.time.millisecond
    TIME.ZONE:request.receive.time.timezone
    HTTP.FIRSTLINE:request.firstline
    HTTP.METHOD:request.firstline.method
    HTTP.URI:request.firstline.uri
    HTTP.QUERYSTRING:request.firstline.uri.query
    STRING:request.firstline.uri.query.*
    HTTP.PROTOCOL:request.firstline.protocol
    HTTP.PROTOCOL.VERSION:request.firstline.protocol.version
    STRING:request.status.last
    BYTESCLF:response.body.bytes
    HTTP.URI:request.referer
    HTTP.QUERYSTRING:request.referer.query
    STRING:request.referer.query.*
    HTTP.USERAGENT:request.user-agent

Now some of these lines contain a * . 
This is a wildcard that can be replaced with any 'name' if you need a specific value.
You can also leave the '*' and get everything that is found in the actual log line.

**Step 2 Create the receiving POJO** 

We need to create the receiving record class that is simply a POJO that does not need any interface or inheritance. 
In this class we create setters that will be called when the specified field has been found in the line.

So we can now add to this class a setter that simply receives a single value: 

    @Field("IP:connection.client.host")
    public void setIP(final String value) {
        ip = value;
    }

If we really want the name of the field we can also do this

    @Field("STRING:request.firstline.uri.query.img")
    public void setQueryImg(final String name, final String value) {
        results.put(name, value);
    }

This latter form is very handy because this way we can obtain all values for a wildcard field

    @Field("STRING:request.firstline.uri.query.*")
    public void setQueryStringValues(final String name, final String value) {
        results.put(name, value);
    }

Or a combination of the above examples where you specify multiple field patterns

    @Field({"IP:connection.client.host", 
            "STRING:request.firstline.uri.query.*"})
    public void setValue(final String name, final String value) {
        results.put(name, value);
    }

In some cases you may not want to have empty/null values so starting with version 5.0 you can specify a setterPolicy:

    @Field(value = "STRING:request.firstline.uri.query.*", setterPolicy = NOT_NULL)

The 3 possible values for the setterPolicy flag are:

    ALWAYS    : Call the setter for all values: Normal, Empty and NULL.
    NOT_NULL  : Call the setter for values: Normal and Empty, but not for NULL values.
    NOT_EMPTY : Call the setter for values: Normal, but not for Empty and NULL values.

*Notes about the setters*

- Only if a value exists in the actual logline the setter will be called (mainly relevant if you want to get a specific query param or cookie).
- If you specifiy the same field on several setters then each of these setters will be called.
- There is NO guarantee about the order the setters will be called.

Have a look at the 'examples/pojo' directory for a working example.

**Step 3 Use the parser in your application.**

You create an instance of the parser

    Parser<MyRecord> parser = new HttpdLoglineParser<MyRecord>(MyRecord.class, logformat);

And then call the parse method repeatedly for each line.
There are two ways to do this:
1) Let the parser create and a new instance of "MyRecord" for each parsed line (think about the GC consequences!!):

       MyRecord record = parser.parse(logline);
 
2) Reuse the same instance.
So you do this only once:

       MyRecord record = new MyRecord(); 

And then for each logline:

    record.clear(); // Which is up to you to implement to 'reset' the record instance to it's initial/empty state.
    parser.parse(record, logline);

Project Lombok
===
In case you like to use project Lombok to generate your getters and setters then using the annotations looks something like this:

    @Getter @Setter(onMethod=@__(@Field("HTTP.COOKIE:request.cookies.foo"))) private String foo = null;

To avoid weird effects please install the "Lombok Plugin" in IntelliJ IDEA to use this.

License
===
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    https://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
