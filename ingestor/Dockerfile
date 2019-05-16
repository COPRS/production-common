FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core as build

WORKDIR /app
#COPY pom.xml /app/s1pro-core-ingestor
#RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
#COPY build/ /app/build/
#COPY src/ /app/src/
#COPY config /app/config/
#RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package


FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/ingestor/target/s1pro-core-ingestor-0.2.0-SNAPSHOT.jar s1pdgs-ingestor.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
