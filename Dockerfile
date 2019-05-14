FROM maven:3.5.3-jdk-8

WORKDIR /app

COPY pom.xml /app
COPY PmdJavaRuleset.xml /app

COPY app-catalog-client/ /app/app-catalog-client
COPY applicative-catalog/ /app/applicative-catalog
COPY archives/ /app/archives
COPY ingestor/ /app/ingestor
COPY job-generator/ /app/job-generator
COPY lib-commons/ /app/lib-commons
COPY metadata-catalog/ /app/metadata-catalog
COPY mqi-client/ /app/mqi-client
COPY mqi-server/ /app/mqi-server
COPY obs-sdk/ /app/obs-sdk
COPY scaler/ /app/scaler
COPY wrapper/ /app/wrapper

RUN find /app
RUN echo "content of /usr/share/maven/ref/settings-docker.xml:"
RUN cat /usr/share/maven/ref/settings-docker.xml
# RUN find /usr/share/maven/ref/repository
RUN mvn --debug -B -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
#RUN mvn --debug -B -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml package
RUN find /usr/share/maven/ref/repository

#ENTRYPOINT ["/usr/local/bin/mvn-entrypoint.sh"]
#CMD ["mvn"]
