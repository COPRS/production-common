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
WORKDIR /app
 
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH} as base


FROM artifactory.coprs.esa-copernicus.eu/cfi/processors/s1_l12:3.8.0-light
 
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
COPY --from=base /log/log4j2.yml /log/log4j2.yml
COPY simplified_sea_cls.geojson /app/simplified_sea_cls.geojson
 
USER root:root
 
# Install java 11
RUN yum install -y java-11-openjdk 
#RUN update-alternatives --set java /usr/lib/jvm/java-11-openjdk-11.0.4.11-0.el7_6.x86_64/bin/java
RUN alternatives --set java java-11-openjdk.x86_64

# Add Tini as entry point
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN mkdir -p /data/localWD && chown piccontrol:pic_run /data/localWD
RUN mkdir -p /data/oqcWD && chown piccontrol:pic_run /data/oqcWD
 
### WA RS-815 START ###
#RUN useradd -u 1001 rsuser
 
#USER rsuser:rsuser

USER  piccontrol:pic_run
### WA RS- 815 END ###

ENTRYPOINT ["/tini", "--", "/app/start.sh"]
