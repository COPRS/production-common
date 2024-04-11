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

FROM artifactory.coprs.esa-copernicus.eu/rs-docker-private/rs-container-s1-ipf-simulator:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

USER root 

# Create dummy /var/tmp/conf.sh to be used by /app/start.sh
WORKDIR /var/tmp
RUN echo "#!/bin/sh" >> /var/tmp/conf.sh && chmod +x /var/tmp/conf.sh

WORKDIR /app
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Add Tini as entry point
RUN apt-get update && apt-get install tini -y
ENTRYPOINT ["/usr/bin/tini", "--", "/app/start.sh"] 
 