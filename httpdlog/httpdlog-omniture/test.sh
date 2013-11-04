#!/bin/bash

rm -f pig_*.log

pig -x local test.pig > output.txt
cat output.txt
