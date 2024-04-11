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
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

#RUN apk update && apk add python2
USER root
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install python2 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

#create local WD
RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD
RUN mkdir -p /opt/sample/S3B9060 && chown rsuser:rsuser /data/localWD

USER 1001

COPY files/* /opt/

COPY files/DirectDataCaptureServer.py /usr/local/components/DDC/bin/DirectDataCaptureServer
COPY files/NOOP.sh /usr/local/components/L0PreProc/bin/S3L0PreProcessor
COPY files/S3L0PostProcessor.py /usr/local/components/L0PostProc/bin/S3L0PostProcessor

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY /config/start.sh start.sh

ENTRYPOINT ["/app/start.sh"]