# Copyright 2023 Airbus
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

ARG BRANCH

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

#RUN apk update && apk add p7zip
USER root
RUN apt-get update && apt-get -y install p7zip-full
USER 1001

COPY /target/rs-compression-worker-*.jar rs-compression-worker.jar
COPY /config/start.sh start.sh

COPY /config/zip-compression.sh zip-compression.sh
COPY /config/zip-nocompression.sh zip-nocompression.sh
COPY /config/tar-compression.sh tar-compression.sh

COPY /config/uncompression.sh uncompression.sh

ENTRYPOINT ["/app/start.sh"]
