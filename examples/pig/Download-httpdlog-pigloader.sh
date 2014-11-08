#!/bin/bash

VERSION=1.4

if [ ! -f httpdlog-pigloader-${VERSION}.jar ];
then
    mvn -DrepoUrl="http://repo1.maven.org/maven2" -DgroupId=nl.basjes.parse.httpdlog -DartifactId=httpdlog-pigloader -Dversion=${VERSION} dependency:get
    cp ~/.m2/repository/nl/basjes/parse/httpdlog/httpdlog-pigloader/${VERSION}/httpdlog-pigloader-${VERSION}.jar .
fi

pig -x local CommonLogFormat.pig
