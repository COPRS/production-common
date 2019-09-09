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
COPY disseminator/ /app/disseminator
COPY inbox-polling/ /app/inbox-polling
COPY inbox-ingestion/ /app/inbox-ingestion
COPY job-generator/ /app/job-generator
COPY lib-commons/ /app/lib-commons
COPY metadata-catalog/ /app/metadata-catalog
COPY mqi-client/ /app/mqi-client
COPY mqi-server/ /app/mqi-server
COPY obs-sdk/ /app/obs-sdk
COPY scaler/ /app/scaler
COPY wrapper/ /app/wrapper
COPY request-repository/ /app/request-repository
COPY queue-watcher/ /app/queue-watcher
COPY validation/ /app/validation

RUN mvn -DskipTests=true -Dpmd.skip=true -Dfindbugs.skip=true -B -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml install 

####
# An empty image that will be just used to gather all build artifacts into a small image
####

# scratch seems not to work for some reason, we go for alpine...
FROM alpine as final
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app
RUN echo ${VERSION} >> VERSION
RUN echo ${BRANCH_TEXT} >> VERSION
RUN echo ${COMMIT_ID} >> VERSION

COPY --from=buildenv /app/applicative-catalog/target /app/applicative-catalog/target
COPY --from=buildenv /app/archives/target /app/archives/target
COPY --from=buildenv /app/compression/target /app/compression/target
COPY --from=buildenv /app/disseminator/target /app/disseminator/target
COPY --from=buildenv /app/inbox-polling/target /app/inbox-polling/target
COPY --from=buildenv /app/inbox-ingestion/target /app/inbox-ingestion/target
COPY --from=buildenv /app/job-generator/target /app/job-generator/target
COPY --from=buildenv /app/metadata-catalog /app/metadata-catalog
COPY --from=buildenv /app/mqi-server/target /app/mqi-server/target
COPY --from=buildenv /app/scaler/target /app/scaler/target
COPY --from=buildenv /app/wrapper/target /app/wrapper/target
COPY --from=buildenv /app/wrapper/config /app/wrapper/config
COPY --from=buildenv /app/request-repository/target /app/request-repository/target
COPY --from=buildenv /app/queue-watcher/target /app/queue-watcher/target
COPY --from=buildenv /app/validation/target /app/validation/target
