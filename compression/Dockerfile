FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:develop as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/compression/target/s1pro-core-compression-0.2.0-SNAPSHOT.jar s1pdgs-compression.jar
RUN apk update && apk add wget
RUN mkdir tmp
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
