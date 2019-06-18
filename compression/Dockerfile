FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:develop as build

WORKDIR /app

#FROM openjdk:8-jre-alpine
#WORKDIR /app
#COPY --from=build /app/compression/target/s1pro-core-compression-0.3.0-SNAPSHOT.jar s1pdgs-compression.jar
#RUN apk update && apk add wget
#RUN mkdir -p tmp
#COPY /config/start.sh start.sh
#COPY /config/compression.sh compression.sh
#ENTRYPOINT "/app/start.sh"

FROM centos:latest
WORKDIR /app
COPY --from=build /app/compression/target/s1pro-core-compression-0.3.0-SNAPSHOT.jar s1pdgs-compression.jar
RUN mkdir -p tmp

RUN yum -y install p7zip wget java-1.8.0-openjdk

COPY /config/start.sh start.sh
COPY /config/compression.sh compression.sh
ENTRYPOINT "/app/start.sh"

#FROM ubuntu:latest
#WORKDIR /app
#COPY --from=build /app/compression/target/s1pro-core-compression-0.3.0-SNAPSHOT.jar s1pdgs-compression.jar
#RUN mkdir -p tmp
#
#RUN apt-get update && \
#		apt-get install -y openjdk-8-jre p7zip wget
#
#COPY /config/start.sh start.sh
#COPY /config/compression.sh compression.sh
#ENTRYPOINT "/app/start.sh"