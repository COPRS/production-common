FROM registry.tools.s1pdgs.eu/wo7/repo-maven-common:latest as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
COPY build/ /app/build/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
RUN mkdir -p /app/libs/spdgs-sdk/app-catalog-client/1.2.1
COPY --from=build /app/pom.xml /app/libs/spdgs-sdk/app-catalog-client/1.2.1/app-catalog-client-1.2.1.pom
COPY --from=build /app/target/app-catalog-client-1.2.1.jar /app/libs/spdgs-sdk/app-catalog-client/1.2.1/app-catalog-client-1.2.1.jar
ENTRYPOINT /bin/sh
