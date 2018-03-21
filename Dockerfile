FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY dev/ /app/dev/
COPY src/ /app/src/
COPY /logback-spring.xml /app/logback-spring.xml
RUN mvn -B package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-scaler-1.0.0.jar /app/s1pdgs-scaler.jar
COPY /logback-spring.xml logback-spring.xml
COPY /src/main/resources/application.yml /etc/catalog/application.yml application.yml
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-scaler.jar", "--spring.config.location=classpath:/application.yml"]
