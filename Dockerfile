FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY dev/ /app/dev/
COPY src/ /app/src/
COPY config/ /app/config/
COPY test /app/test/
RUN mkdir tmp  && \
	mvn -B package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/s1pdgs-metadata-catalog-1.0.0.jar /app/s1pdgs-metadata-catalog.jar
COPY /config/log/log4j2.yml log4j2.yml
COPY /src/main/resources/application.yml application.yml
COPY /config/xsltDir/ /app/xsltDir/
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-metadata-catalog.jar", "--spring.config.location=classpath:/application.yml"]
