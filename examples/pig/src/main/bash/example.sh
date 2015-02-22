#!/bin/bash

pig -x local -param_file etc/AccessLogs.properties pig/example.pig

