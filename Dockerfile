FROM repo-maven-external:master as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
COPY build/ /app/build/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
RUN mkdir -p /app/libs/spdgs-sdk/commons/1.1.1
COPY --from=build /app/pom.xml /app/libs/spdgs-sdk/commons/1.1.1/commons-1.1.1.pom
COPY --from=build /app/target/commons-1.1.1.jar /app/libs/spdgs-sdk/commons/1.1.1/commons-1.1.1.jar
ENTRYPOINT /bin/sh