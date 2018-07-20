FROM registry.geohub.space/wo7/repo-maven-common:latest as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
COPY dev/ /app/dev/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
RUN mkdir -p /app/libs/spdgs-sdk/app-catalog-client/1.0.0
COPY --from=build /app/pom.xml /app/libs/spdgs-sdk/app-catalog-client/1.0.0/app-catalog-client-1.0.0.pom
COPY --from=build /app/target/app-catalog-client-1.0.0.jar /app/libs/spdgs-sdk/app-catalog-client/1.0.0/app-catalog-client-1.0.0.jar
ENTRYPOINT /bin/sh