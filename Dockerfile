FROM local-repo-maven/custom:master as build
WORKDIR /app
COPY pom.xml /app
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY src/ /app/src/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:8-jre-alpine
RUN mkdir -p /app/libs/spdgs-sdk/lib-spdgs-common/0.0.1-SNAPSHOT
COPY --from=build /app/pom.xml /app/libs/spdgs-sdk/lib-spdgs-common/0.0.1-SNAPSHOT/lib-spdgs-common-0.0.1-SNAPSHOT.pom
COPY --from=build /app/target/lib-spdgs-common-0.0.1-SNAPSHOT.jar /app/libs/spdgs-sdk/lib-spdgs-common/0.0.1-SNAPSHOT/lib-spdgs-common-0.0.1-SNAPSHOT.jar
ENTRYPOINT /bin/sh