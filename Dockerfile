FROM registry.geohub.space/wo7/repo-maven-all:latest as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
COPY build/ /app/build/
COPY config/ /app/config/
COPY test/ /app/test/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-applicative-catalog-1.1.0.jar /app/s1pdgs-applicative-catalog.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/bin/sh" "-c" "/app/start.sh"
