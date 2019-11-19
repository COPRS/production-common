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
COPY disseminator/ /app/disseminator
COPY ingestion-trigger/ /app/ingestion-trigger
COPY ingestion-worker/ /app/ingestion-worker
COPY ipf-preparation-worker/ /app/ipf-preparation-worker
COPY lib-commons/ /app/lib-commons
COPY app-status /app/app-status
COPY metadata-catalog/ /app/metadata-catalog
COPY metadata-client/ /app/metadata-client
COPY mqi-client/ /app/mqi-client
COPY mqi-server/ /app/mqi-server
COPY obs-sdk/ /app/obs-sdk
COPY scaler/ /app/scaler
COPY ipf-execution-worker/ /app/ipf-execution-worker
COPY request-repository/ /app/request-repository
COPY queue-watcher/ /app/queue-watcher
COPY validation/ /app/validation
COPY compression-worker/ /app/compression-worker
COPY prip /app/prip

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
COPY --from=buildenv /app/disseminator/target /app/disseminator/target
COPY --from=buildenv /app/ingestion-trigger/target /app/ingestion-trigger/target
COPY --from=buildenv /app/ingestion-worker/target /app/ingestion-worker/target
COPY --from=buildenv /app/ipf-preparation-worker/target /app/ipf-preparation-worker/target
COPY --from=buildenv /app/metadata-catalog /app/metadata-catalog
COPY --from=buildenv /app/metadata-client /app/metadata-client
COPY --from=buildenv /app/mqi-server/target /app/mqi-server/target
COPY --from=buildenv /app/scaler/target /app/scaler/target
COPY --from=buildenv /app/ipf-execution-worker/target /app/ipf-execution-worker/target
COPY --from=buildenv /app/ipf-execution-worker/config /app/ipf-execution-worker/config
COPY --from=buildenv /app/request-repository/target /app/request-repository/target
COPY --from=buildenv /app/queue-watcher/target /app/queue-watcher/target
COPY --from=buildenv /app/validation/target /app/validation/target
COPY --from=buildenv /app/compression-worker/target /app/compression-worker/target
COPY --from=buildenv /app/prip/target /app/prip/target