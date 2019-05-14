FROM maven:3.5.3-jdk-8

WORKDIR /app

COPY pom.xml /app

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

RUN mvn -B -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml package

#ENTRYPOINT ["/usr/local/bin/mvn-entrypoint.sh"]
#CMD ["mvn"]
