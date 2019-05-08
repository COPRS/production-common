FROM registry.tools.s1pdgs.eu/wo7/repo-maven-all:latest as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
COPY build/ /app/build/
COPY test/ /app/test/
COPY config/ /app/config/
RUN mkdir tmp && mvn -B -s /usr/share/maven/ref/settings-docker.xml package


FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-metadata-catalog-2.0.0.jar /app/s1pdgs-metadata-catalog.jar
RUN apk update && apk add wget
RUN mkdir tmp
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
