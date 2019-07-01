FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:s1pro-003-s1pro-66-d as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/metadata-catalog/target/s1pro-core-metadata-catalog-*.jar s1pdgs-metadata-catalog.jar
RUN apk update && apk add wget
RUN mkdir tmp
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
