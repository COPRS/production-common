####
#The image that is used to deploy the build environment and compile all source code.
#### 
FROM maven:3.5.3-jdk-8 as buildenv

WORKDIR /app

# Just used in Werum network
#ENV https_proxy=http://proxy.net.werum:8080/
#COPY test.xml /usr/share/maven/ref/settings-docker.xml

COPY pom.xml /app
COPY PmdJavaRuleset.xml /app

COPY app-catalog-client/ /app/app-catalog-client
COPY applicative-catalog/ /app/applicative-catalog
COPY archives/ /app/archives
COPY compression/ /app/compression
COPY ingestor/ /app/ingestor
COPY job-generator/ /app/job-generator
COPY lib-commons/ /app/lib-commons
COPY metadata-catalog/ /app/metadata-catalog
COPY mqi-client/ /app/mqi-client
COPY mqi-server/ /app/mqi-server
COPY obs-sdk/ /app/obs-sdk
COPY scaler/ /app/scaler
COPY wrapper/ /app/wrapper
COPY error-repository/ /app/error-repository
COPY queue-watcher/ /app/queue-watcher

RUN mvn -Dmaven.test.skip=true -B -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml install 

####
# An empty image that will be just used to gather all build artifacts into a small image
####

# scratch seems not to work for some reason, we go for alpine...
#FROM scatch
FROM alpine

WORKDIR /app
COPY -v --from=buildenv /app/applicative-catalog/target /
COPY -v --from=buildenv /app/archives/target /
COPY -v --from=buildenv /app/compression/target /
COPY -v --from=buildenv /app/ingestor/target /
COPY -v --from=buildenv /app/job-generator/target /
COPY -v --from=buildenv /app/metadata-catalog /
COPY -v --from=buildenv /app/mqi-server/target /
COPY -v --from=buildenv /app/scaler/target /
COPY -v --from=buildenv /app/wrapper/target /
COPY -v --from=buildenv /app/error-repository/target /
COPY -v --from=buildenv /app/queue-watcher/target /
