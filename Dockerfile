ARG PROCESS_IMAGE=docker_l0
ARG PROCESS_VERSION=dev
ARG PROCESS_COMMAND=/var/tmp/conf.sh

FROM maven:3.5.2-jdk-8-alpine as build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY src/ /app/src/
COPY dev/ /app/dev/
COPY test/ /app/test/
COPY config/ /app/config/
RUN	mvn -B package

FROM registry.geohub.space/wo7/${PROCESS_IMAGE}:${PROCESS_VERSION}
RUN yum install -y java-1.8.0-openjdk && yum clean all
WORKDIR /app
COPY --from=build /app/target/s1pdgs-wrapper-1.0.0.jar s1pdgs-wrapper.jar
COPY /config/log/log4j2.yml log4j2.yml
COPY /src/main/resources/application.yml application.yml
#CMD /var/tmp/configure_pdgs_custom.sh pac1 8 ; ulimit -s unlimited ;
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/s1pdgs-wrapper.jar","--spring.config.location=classpath:/application.yml"]
ENTRYPOINT ulimit -s unlimited; ${PROCESS_COMMAND}; java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-wrapper.jar --spring.config.location=classpath:/application.yml
