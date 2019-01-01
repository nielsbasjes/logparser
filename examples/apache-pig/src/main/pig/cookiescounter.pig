-- -------------------------------------------------------------
-- Apache HTTPD & NGINX Access log parsing made easy
-- Copyright (C) 2011-2019 Niels Basjes
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- -------------------------------------------------------------

-- A simple example on counting the number occurrances of cookies

-- Simply register everything in the lib directory
REGISTER lib/*.jar;

-- Get the right values (mostly from the property file)
%declare LOGFILE   '${ACCESS_LOGPATH}/omniture.log'
%declare LOGFORMAT '${ACCESS_LOGFORMAT}'

Clicks =
  LOAD '${LOGFILE}'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT',
        'HTTP.COOKIE:request.cookies.*'
        )
    AS  (
        AllCookies:map[]
        );

CookieNames =
    FOREACH     Clicks
    GENERATE    FLATTEN(KEYSET(AllCookies)) AS CookieName:chararray,
                1L                          AS count:long;

GroupedCookieNames =
    GROUP       CookieNames
    BY          (CookieName);

CountedCookies =
    FOREACH     GroupedCookieNames
    GENERATE    group                       AS CookieName:chararray,
                SUM(CookieNames.count)      AS count:long;

DUMP CountedCookies;
