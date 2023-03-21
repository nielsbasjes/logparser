# Apache HTTPD & NGINX Access log parsing made easy
# Copyright (C) 2011-2021 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM perl:5.37.10-buster

RUN cpan File::Slurper
RUN cpan Cpanel::JSON::XS
RUN cpan Math::Int128
RUN cpan MaxMind::DB::Writer::Serializer

WORKDIR "/GeoIPTestData/test-data"

CMD ["/GeoIPTestData/test-data/write-test-data.pl"]
