FROM registry.geohub.space/wo7/repo-maven-all:latest as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
COPY dev/ /app/dev/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/mqi-server-1.0.0.jar /app/mqi-server.jar
COPY /config/log/log4j2.yml log4j2.yml
COPY /src/main/resources/application.yml application.yml
COPY /config/start.sh start.sh
ENTRYPOINT "/bin/sh" "-c" "/app/start.sh"
