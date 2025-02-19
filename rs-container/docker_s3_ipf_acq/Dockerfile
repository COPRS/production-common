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

#FROM artifactory.coprs.esa-copernicus.eu/cfi/sentinel3/s3-ipf0:06.12
FROM centos:7
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT
ARG ARTIFACTORY_URL="https://artifactory.coprs.esa-copernicus.eu:443/artifactory/cfi"


### Reconstruction of the base image provided by the agency
USER root

RUN yum install -y java-1.8.0-openjdk java-11-openjdk boost-date-time boost-thread boost-devel tar

# RS-747: This is a work around to use the xerces-c from the operating system
RUN yum install xerces-c -y 

# Creating symbolic link to match expectation from IPF
RUN ln -s /usr/lib64/libboost_date_time.so.1.53.0 /usr/lib64/libboost_date_time.so.5

# create user
RUN useradd -u 1001 rsuser
 
WORKDIR /tmp
 
# DDC
RUN mkdir -p /tmp/s3-cgs1-ddc
WORKDIR /tmp/s3-cgs1-ddc
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/s3pd-ddc-3.4.38-3.4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/s3pd-ddc-cfg-3.4.38-3.4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/s3-cgs1-ddc.sh"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/s3-cgs1-ddc.rls"

# L0Pre
RUN mkdir -p /tmp/s3-cgs1-l0preproc
WORKDIR /tmp/s3-cgs1-l0preproc

RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/s3pd-l0preproc-3.4.38-3.4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/s3pd-l0preproc-cfg-3.4.38-3.4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/s3-cgs1-l0preproc.sh"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/s3-cgs1-l0preproc.rls"

# L0Post
RUN mkdir -p /tmp/s3-cgs1-l0postproc
WORKDIR /tmp/s3-cgs1-l0postproc

RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/s3pd-l0postproc-3.4.38-3.4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/s3pd-l0postproc-cfg-3.4.38-3.4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/s3-cgs1-l0postproc.sh"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/s3-cgs1-l0postproc.rls"

# Common
RUN mkdir -p /tmp/common
WORKDIR /tmp/common

RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/common/S3PD-COTS-libssh2-1.3.0-1.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/common/S3PD-COTS-xerces-3.1.1-1.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/common/common.rls"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/common/common.sh"

# Functions
WORKDIR /tmp
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/functions"
# Patching the function to use force on install as the RPM are not working with Centos 7 anymore
RUN sed -i 's;rpm -i $AT $rpms_to_install >>$RILOG 2>\&1;rpm --force -i $AT $rpms_to_install >>$RILOG 2>\&1;g' functions
RUN sed -i 's;rpm -i $rpms_to_install $AT >>$RILOG 2>\&1;rpm --force -i $rpms_to_install $AT >>$RILOG 2>\&1;g' functions

# FIXING naming pattern
RUN cd /tmp/s3-cgs1-l0postproc/ ; mv s3-cgs1-l0postproc.rls s3-cgs1-l0postproc.rls.bck ; cat s3-cgs1-l0postproc.rls.bck | tr '[:upper:]' '[:lower:]' > s3-cgs1-l0postproc.rls
RUN cd /tmp/s3-cgs1-l0preproc/ ; mv s3-cgs1-l0preproc.rls s3-cgs1-l0preproc.rls.bck  ; cat s3-cgs1-l0preproc.rls.bck | tr '[:upper:]' '[:lower:]' > s3-cgs1-l0preproc.rls
RUN cd /tmp/s3-cgs1-ddc ; mv s3-cgs1-ddc.rls s3-cgs1-ddc.rls.bck ; cat s3-cgs1-ddc.rls.bck | tr '[:upper:]' '[:lower:]' > s3-cgs1-ddc.rls

# Deploy
RUN cd /tmp/common; chmod a+x ./common.sh && ./common.sh -i
RUN cd /tmp/s3-cgs1-ddc; chmod a+x ./s3-cgs1-ddc.sh && ./s3-cgs1-ddc.sh -i
RUN cd /tmp/s3-cgs1-l0preproc; chmod a+x ./s3-cgs1-l0preproc.sh && ./s3-cgs1-l0preproc.sh -i
RUN cd /tmp/s3-cgs1-l0postproc; chmod a+x ./s3-cgs1-l0postproc.sh && ./s3-cgs1-l0postproc.sh -i

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

# Adding ACQ wrapper scripts
COPY files /opt

# Add Tini as entry point
USER root:root 

# Install java 11
RUN alternatives --set java java-11-openjdk.x86_64

ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD
RUN mkdir -p /data/oqcWD && chown rsuser:rsuser /data/oqcWD

#USER rsuser:rsuser

# WA RS-968: For some reason the processors seems not always generate the NRTAP structure correctly. To ensure that these directories are existing,
# they are generated in the docker image
RUN mkdir -p /data/NRTAP/CADU/S3{A,B}
RUN mkdir -p /data/NRTAP/CADU_Cache/S3{A,B}
RUN mkdir -p /data/NRTAP/DICache
RUN mkdir -p /data/NRTAP/HighPriorityOutBasket
RUN mkdir -p /data/NRTAP/L0Orders
RUN mkdir -p /data/NRTAP/LowPriorityOutBasket
RUN mkdir -p /data/NRTAP/WorkingDir/S3{A,B}

ENTRYPOINT ["/tini", "--", "/app/start.sh"]
