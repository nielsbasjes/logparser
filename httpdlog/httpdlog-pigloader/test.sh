#!/bin/bash

rm -f pig_*.log
pig -x local -f test.pig > test-output.txt
cat pig_*.log
echo ===========
cat test-output.txt
echo ===========
