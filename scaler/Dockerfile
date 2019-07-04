FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:s1pro-003-s1pro-265 as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/scaler/target/s1pro-core-scaler-*.jar s1pdgs-scaler.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
