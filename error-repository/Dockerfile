FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:s1pro-003-s1pro-66-d as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/error-repository/target/s1pro-core-error-repository-*.jar s1pdgs-error-repository.jar
RUN apk update && apk add wget
RUN mkdir -p tmp
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
