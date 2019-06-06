FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:latest as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/ingestor/target/s1pro-core-ingestor-*.jar s1pdgs-ingestor.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
