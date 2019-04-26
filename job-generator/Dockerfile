FROM registry.geohub.space/wo7/repo-maven-all:latest as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY build/ /app/build/
COPY test/ /app/test/
COPY config/ /app/config/
COPY src/ /app/src/
RUN mkdir /app/tmp/ && \
	mkdir /app/tmp/sessions/ && \
	mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
WORKDIR /app
RUN mkdir -p /data/sessions/ 
RUN apk update && apk add wget
COPY --from=build /app/target/s1pdgs-job-generator-2.0.0.jar s1pdgs-job-generator.jar
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
