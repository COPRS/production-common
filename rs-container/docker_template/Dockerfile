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

# This docker file is a generic template that explains how to build a RS add-on image for
# a new workflow and what activities needs to be rolled out in order to get the Execution
# worker into the image that will invoke the processing and monitor the execution.

# When using the automatic build pipe the branch information will be automatically extracted
# from the github workflows and provided via the variable BRANCH. This will contain the tag
# or branch of the current build (e.g. DEVELOP or 1.11.0-rc1). This version can be however
# also specified manually when building an image outside of the automatic build pipe.

# The following command will take a the image containing the execution worker from the artifactory
# and introduce it as "build". This will be used later to extract the software from the image.
ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

# The following commands will take the COPRS base image from the artifactory. This is used to
# extract some artefacts like log file configurations and is introduced as "base"
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH} as base

# The provided CFI image will be used as the base layer for the new image as it will contain
# an already configured IPF environment with an installed IPF, it will already contain all prerequisties
# it requires as IPF binaries, required configurations and required operating system dependencies.
 
# In this example we reference a image from a remote docker registry, but you can also use a local
# docker installation instead if you imported the images or creating a new one from the scratch.   
FROM artifactory.coprs.esa-copernicus.eu/cfi/processors/sentinel3/s3-sr1:07.03

# For COPRS it is forseen that the applications are not executed as root but as user "rsuser".
# We are switching to root to create the new user. And creating a folde /app that will be used as 
# default location for the installation of the execution worker. As well we are creating the default
# location of the working directory of the IPFs.
USER root
RUN useradd -u 1001 rsuser
WORKDIR /app
RUN mkdir -p /app && chown rsuser:rsuser /app
RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD
RUN mkdir -p /data/oqcWD && chown rsuser:rsuser /data/oqcWD

# We are switching to the rsuser that was created and starting to copy the required artifacts into the
# image 
USER rsuser:rsuser
# The actual jar that contains the Execution Worker from the build layer defined above
COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
# A default log file configuration from the base layer defined above
COPY --from=base /log/log4j2.yml /log/log4j2.yml

# The following step is optional, but highly recommended. In case of a bug in the IPF it can happen that
# the processes spawned by the IPF are not cleaned up correctly and zombie processes are left behind.
# This can over time have an impact on the stability of the system. By using tini as init process for the
# system it is ensured that all the spawned childs are cleaned up correctly.
USER root:root
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
USER rsuser:rsuser
ENTRYPOINT ["/tini", "--", "/app/start.sh"]

# If you don't want to use tiny us the following entry point for launching the execution worker directly.
#ENTRYPOINT ["/app/start.sh"]
