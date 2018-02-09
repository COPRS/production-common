FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY . /app
RUN mvn -B clean package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-ingestor-0.1.0.jar s1pdgs-ingestor.jar
COPY /logback-spring.xml logback-spring.xml
COPY /src/main/resources/application.yml application.yml
COPY /xsltDir/* /app/xsltDir/
RUN mkdir tmp
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-ingestor.jar", "--spring.config.location=classpath:/application.yml,file:/home/centos/config/ingestor/application.yml"]
