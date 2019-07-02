ARG BRANCH
FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:${BRANCH} as build
WORKDIR /app

FROM centos:latest
ARG BRANCH_TEXT
WORKDIR /app
RUN echo "${BRANCH_TEXT}" >> VERSION
COPY --from=build /app/compression/target/s1pro-core-compression-0.4.0-SNAPSHOT.jar s1pdgs-compression.jar
RUN mkdir -p tmp
RUN yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN yum -y install p7zip wget java-1.8.0-openjdk

COPY /config/start.sh start.sh
COPY /config/compression.sh compression.sh
ENTRYPOINT "/app/start.sh"