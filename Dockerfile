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
COPY production-trigger/ /app/production-trigger
COPY ipf-preparation-worker/ /app/ipf-preparation-worker
COPY lib-commons/ /app/lib-commons
COPY app-status /app/app-status
COPY metadata-catalog-trigger/ /app/metadata-catalog-trigger
COPY metadata-catalog-worker/ /app/metadata-catalog-worker
COPY metadata-client/ /app/metadata-client
COPY mqi-client/ /app/mqi-client
COPY mqi-server/ /app/mqi-server
COPY obs-sdk/ /app/obs-sdk
COPY scaler/ /app/scaler
COPY execution-worker/ /app/execution-worker
COPY request-repository/ /app/request-repository
COPY queue-watcher/ /app/queue-watcher
COPY validation/ /app/validation
COPY compression-trigger/ /app/compression-trigger
COPY compression-worker/ /app/compression-worker
COPY prip-client /app/prip-client
COPY prip-trigger /app/prip-trigger
COPY prip-worker /app/prip-worker
COPY prip-frontend /app/prip-frontend


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
COPY --from=buildenv /app/production-trigger/target /app/production-trigger/target
COPY --from=buildenv /app/ipf-preparation-worker/target /app/ipf-preparation-worker/target
COPY --from=buildenv /app/metadata-catalog-trigger /app/metadata-catalog-trigger
COPY --from=buildenv /app/metadata-catalog-worker /app/metadata-catalog-worker
COPY --from=buildenv /app/metadata-client /app/metadata-client
COPY --from=buildenv /app/mqi-server/target /app/mqi-server/target
COPY --from=buildenv /app/scaler/target /app/scaler/target
COPY --from=buildenv /app/execution-worker/target /app/execution-worker/target
COPY --from=buildenv /app/execution-worker/config /app/execution-worker/config
COPY --from=buildenv /app/request-repository/target /app/request-repository/target
COPY --from=buildenv /app/queue-watcher/target /app/queue-watcher/target
COPY --from=buildenv /app/validation/target /app/validation/target
COPY --from=buildenv /app/compression-trigger/target /app/compression-trigger/target
COPY --from=buildenv /app/compression-worker/target /app/compression-worker/target
COPY --from=buildenv /app/prip-trigger/target /app/prip-trigger/target
COPY --from=buildenv /app/prip-worker/target /app/prip-worker/target
COPY --from=buildenv /app/prip-frontend/target /app/prip-frontend/target