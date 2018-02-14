FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY . /app
RUN mvn -B clean package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-metadata-catalog-0.1.0.jar /app/s1pdgs-metadata-catalog.jar
COPY /logback-spring.xml logback-spring.xml
COPY /src/main/resources/application.yml /etc/catalog/application.yml? application.yml
COPY /xsltDir/* /app/xsltDir/
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-metadata-catalog.jar", "--spring.config.location=classpath:/application.yml,file:/etc/catalog/application.yml"]
