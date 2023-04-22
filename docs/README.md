Apache HTTPD & NGINX access log parser
======================================
[![Github actions Build status](https://img.shields.io/github/actions/workflow/status/nielsbasjes/logparser/build.yml?branch=main)](https://github.com/nielsbasjes/logparser/actions)
[![Coverage Status](https://img.shields.io/codecov/c/github/nielsbasjes/logparser)](https://app.codecov.io/gh/nielsbasjes/logparser)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.parse/parser-parent.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22nl.basjes.parse.httpdlog%22)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

This is a Logparsing framework intended to make parsing [Apache HTTPD](https://httpd.apache.org/) and [NGINX](https://nginx.org/) access log files much easier.

The basic idea is that you should be able to have a parser that you can construct by simply
telling it with what configuration options the line was written.
These configuration options are the schema of the access loglines.

So we are using the LogFormat that wrote the file as the input parameter for the parser that reads the same file.
In addition to the config options specified in the Apache HTTPD manual under
[Custom Log Formats](https://httpd.apache.org/docs/current/mod/mod_log_config.html) the following are also recognized:

* common
* combined
* combinedio
* referer
* agent

For Nginx the log_format tokens are specified [here](https://nginx.org/en/docs/http/ngx_http_log_module.html#log_format) and [here](https://nginx.org/en/docs/http/ngx_http_core_module.html#variables).


*** PLACE HOLDER PAGE ***


Donations
===
If this project has business value for you then don't hesitate to support me with a small donation.

[![Donations via PayPal](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

License
===
    Apache HTTPD & NGINX Access log parsing made easy
    Copyright (C) 2011-2023 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
