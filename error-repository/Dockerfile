FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:develop as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/error-repository/target/s1pro-core-error-repository-* s1pdgs-error-repository.jar
RUN apk update && apk add wget
RUN mkdir tmp
COPY /config/start.sh start.sh
COPY /config/error-repository.sh error-repository.sh
ENTRYPOINT "/app/start.sh"
