FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:s1pro-003-c as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/queue-watcher/target/s1pro-core-queue-watcher-*.jar s1pdgs-queue-watcher.jar
RUN apk update && apk add wget
RUN mkdir -p tmp
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
