#!/bin/bash

pig -x local -param_file etc/AccessLogs.properties pig/ExamineLogFormat.pig

