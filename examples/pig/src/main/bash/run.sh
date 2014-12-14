#!/bin/bash

pig -param_file etc/AccessLogs.properties pig/TopUserAgents.pig

