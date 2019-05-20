FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:develop as build

WORKDIR /app

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/mqi-server/target/s1pro-core-mqi-server-0.2.0-SNAPSHOT.jar mqi-server.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
