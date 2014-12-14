#!/bin/bash
mvn clean package
rm -rf PigDemo-*
tar xzf target/*.tar.gz
cd PigDemo-*
./run-demo.sh
