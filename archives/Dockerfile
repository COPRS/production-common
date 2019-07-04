FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:s1pro-003-s1pro-263-c as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
RUN apk update && apk add wget
COPY --from=build /app/archives/target/s1pro-core-archives-*.jar /app/s1pdgs-archives.jar
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
