FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY dev/ /app/dev/
COPY test/ /app/test/
COPY src/ /app/src/
COPY /logback-spring.xml /app/logback-spring.xml
RUN mkdir /app/tmp/ && \
	mkdir /app/tmp/sessions/ && \
	mvn -B package

FROM openjdk:8-jre-alpine
WORKDIR /app
RUN mkdir /data/ && \
	mkdir /data/sessions/ 
COPY --from=build /app/target/s1pdgs-job-generator-1.0.0.jar s1pdgs-job-generator.jar
COPY /logback-spring.xml logback-spring.xml
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-job-generator.jar"]
