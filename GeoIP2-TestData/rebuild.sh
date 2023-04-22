#!/bin/bash
# Apache HTTPD & NGINX Access log parsing made easy
# Copyright (C) 2011-2023 Niels Basjes
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

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

cd "${DIR}" || exit 1

PROJECTNAME=GeoIPTestData
IMAGE_NAME=geo-ip-testdatabuilder
CONTAINER_NAME=GeoIPTestDataBuilder

docker build -t "${IMAGE_NAME}" .

if [ "$(uname -s)" == "Linux" ]; then
  USER_NAME=${SUDO_USER:=${USER}}
  USER_ID=$(id -u "${USER_NAME}")
  GROUP_ID=$(id -g "${USER_NAME}")
else # boot2docker uid and gid
  USER_NAME=${USER}
  USER_ID=1000
  GROUP_ID=50
fi

# man docker-run
# When using SELinux, mounted directories may not be accessible
# to the container. To work around this, with Docker prior to 1.7
# one needs to run the "chcon -Rt svirt_sandbox_file_t" command on
# the directories. With Docker 1.7 and later the z mount option
# does this automatically.
# Since Docker 1.7 was release 5 years ago we only support 1.7 and newer.
V_OPTS=:z

COMMAND=( "$@" )
if [ $# -eq 0 ];
then
#  COMMAND=( "bash" "-i" )
  COMMAND=( )
fi

( cd "${DIR}/test-data" && rm *.mmdb )

docker run --rm=true -i -t                    \
       -u "${USER_ID}"                        \
       -v "${PWD}:/GeoIPTestData${V_OPTS:-}"  \
       -w "/GeoIPTestData/test-data"          \
       --name "${CONTAINER_NAME}"             \
       "${IMAGE_NAME}"                        \
       "${COMMAND[@]}"
