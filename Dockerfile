FROM obs-sdk:SPDGS-210 as build_lib_obs
WORKDIR /app

FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY --from=build_lib_obs /app/libs/ /app/libs/
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY dev/ /app/dev/
COPY src/ /app/src/
COPY config /app/config/
COPY test /app/test/
RUN mvn -B package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-ingestor-0.1.0.jar s1pdgs-ingestor.jar
COPY /config/log/log4j2.yml log4j2.yml
COPY /src/main/resources/application.yml application.yml
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-ingestor.jar", "--spring.config.location=classpath:/application.yml"]
