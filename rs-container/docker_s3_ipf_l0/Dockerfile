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

FROM artifactory.coprs.esa-copernicus.eu/cfi/sentinel3/s3-ipf0:06.15
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT
ARG ARTIFACTORY_URL="https://artifactory.coprs.esa-copernicus.eu:443/artifactory/cfi"


### Reconstruction of the base image provided by the agency
USER root 

RUN yum install wget -y

#RUN yum install -y libgomp java-1.8.0-openjdk

RUN useradd -u 1001 rsuser

# Deploy AdoptJDK workaround

WORKDIR /opt 

RUN wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16.1%2B1/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz
RUN tar -xf OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz && rm OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz

#WORKDIR /tmp

#RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/S3-IPF-COTS.tgz"
#RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/S3IPF-L0_06.12.rpm"
#RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/blas-3.2.1-4.el6.x86_64.rpm"
#RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/lapack-3.2.1-4.el6.x86_64.rpm"

#RUN tar -xf S3-IPF-COTS.tgz -C /usr/local
#RUN yum install -y blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm; yum clean all
#RUN rpm -i S3IPF-L0_06.12.rpm --nodeps
#RUN rm -f S3IPF-L0_06.12.rpm blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm

      
### End of Base-Image  
WORKDIR /app

RUN mkdir -p /app && chown rsuser:rsuser /app

USER rsuser:rsuser


# Adding Version information
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

# Adding execution worker
COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
COPY --from=base /log/log4j2.yml /log/log4j2.yml

USER root:root 

# Add Tini as entry point
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD
RUN mkdir -p /data/oqcWD && chown rsuser:rsuser /data/oqcWD

# Install java 11 - workaround for AdoptJDK
RUN sed -i '1 a\export PATH=$PATH:/opt/jdk-11.0.16.1+1/bin' /app/start.sh

USER rsuser:rsuser

ENTRYPOINT ["/tini", "--", "/app/start.sh"]
