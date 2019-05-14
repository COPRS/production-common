FROM maven:3.5.3-jdk-8

WORKDIR /app

COPY pom.xml /app
COPY wrapper/ /app/wrapper

RUN find /app

RUN mvn -B -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml package

#ENTRYPOINT ["/usr/local/bin/mvn-entrypoint.sh"]
#CMD ["mvn"]
