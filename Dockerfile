FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY src/ /app/src/
COPY dev/ /app/dev/
RUN mkdir /app/test && \ 
    mvn -B package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/spgds-obs-client-sdk-1.0.0.jar spgds-obs-client-sdk-1.0.0.jar