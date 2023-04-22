Dissect IP using GeoIP2 information
===
This project also contains a dissector that uses the [MaxMind](https://www.maxmind.com) GeoIP2 data to
dissect IP addresses into things like Country, City, ASN, etc.

Where are the datafiles?
---
Simple: I didn't include them.

The data is owned by MaxMind and in order to use it you must either purchase a license for 'accurate' GeoIP2
data or download a 'slightly less accurate' free GeoLite2 version.
Also adding these files would make the repo very big.

See https://dev.maxmind.com/ for the both the paid GeoIP2 and the free GeoLite2 downloadable databases.

I personally install and run the geoipupdate tool.

https://dev.maxmind.com/geoip/geoipupdate/

The datafiles I usually work with:

    /var/lib/GeoIP/GeoLite2-City.mmdb
    /var/lib/GeoIP/GeoLite2-Country.mmdb
    /var/lib/GeoIP/GeoIP2-ISP.mmdb
    /var/lib/GeoIP/GeoLite2-ASN.mmdb

You can get some of those by installing geoipupdate tool with the config file /etc/GeoIP.conf

    # The following UserId and LicenseKey are required placeholders:
    UserId 999999
    LicenseKey 000000000000
    ProductIds GeoLite2-City GeoLite2-Country GeoLite2-ASN

How do I use it?
===

Currently there are 4 dissectors available

ASN
---
* Class: nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPASNDissector
* Input: Needs the path to the GeoLite2-ASN.mmdb to function.
* Output: ASN number and organization.

ISP
---
* Class: nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPISPDissector
* Input: Needs the path to the GeoIP2-ISP.mmdb or GeoLite2-ISP.mmdb to function.
* Output: ASN number and organization, ISP name and organization.

Country
---
* Class: nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCountryDissector
* Input: Needs the path to the GeoIP2-Country.mmdb or GeoLite2-Country.mmdb to function.
* Output: Information about continent and country.

City
---
* Class: nl.basjes.parse.httpdlog.dissectors.geoip.GeoIPCityDissector
* Input: Needs the path to the GeoIP2-City.mmdb or GeoLite2-City.mmdb to function.
* Output: Information about continent, country, subdivision, city, postalcode and latitude/longitude.


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
