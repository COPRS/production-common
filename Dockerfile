FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY dev/ /app/dev/
COPY test/ /app/test/
COPY config/ /app/config/
COPY src/ /app/src/
RUN mkdir /app/tmp/ && \
	mkdir /app/tmp/sessions/ && \
	mvn -B package

FROM openjdk:8-jre-alpine
WORKDIR /app
RUN mkdir /data/ && \
	mkdir /data/sessions/ 
COPY --from=build /app/target/s1pdgs-job-generator-1.0.0.jar s1pdgs-job-generator.jar
COPY /config/log/log4j2.yml log4j2.yml
COPY /src/main/resources/application.yml application.yml
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-job-generator.jar"]
