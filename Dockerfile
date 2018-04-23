ARG PROCESS_IMAGE=docker_l0
ARG PROCESS_VERSION=dev

FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY src/ /app/src/
COPY dev/ /app/dev/
COPY data_test/ /app/data_test/
COPY /logback-spring.xml /app/logback-spring.xml
RUN	mvn -B package

FROM registry.geohub.space/wo7/${PROCESS_IMAGE}:${PROCESS_VERSION}
RUN yum install -y java-1.8.0-openjdk && yum clean all
WORKDIR /app
COPY --from=build /app/target/s1pdgs-wrapper-1.0.0.jar s1pdgs-wrapper.jar
COPY /logback-spring.xml logback-spring.xml
COPY /src/main/resources/application.yml application.yml
#CMD /var/tmp/configure_pdgs_custom.sh pac1 8 ; ulimit -s unlimited ;
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-wrapper.jar","--spring.config.location=classpath:/application.yml"]
ENTRYPOINT ulimit -s unlimited; /var/tmp/configure_pdgs_custom.sh pac1 8; java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-wrapper.jar --spring.config.location=classpath:/application.yml
