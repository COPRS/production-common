FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:s1pro-003-c as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/job-generator/target/s1pro-core-job-generator-*.jar s1pdgs-job-generator.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
