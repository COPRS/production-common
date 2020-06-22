####
#The image that is used to deploy the build environment and compile all source code.
#### 
FROM maven:3.5.3-jdk-8 as buildenv

WORKDIR /app

COPY . /app/

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
COPY --from=buildenv /app/ipf-execution-worker/target /app/ipf-execution-worker/target
COPY --from=buildenv /app/ipf-execution-worker/config /app/ipf-execution-worker/config
COPY --from=buildenv /app/request-repository/target /app/request-repository/target
COPY --from=buildenv /app/queue-watcher/target /app/queue-watcher/target
COPY --from=buildenv /app/validation/target /app/validation/target
COPY --from=buildenv /app/compression-trigger/target /app/compression-trigger/target
COPY --from=buildenv /app/compression-worker/target /app/compression-worker/target
COPY --from=buildenv /app/prip-trigger/target /app/prip-trigger/target
COPY --from=buildenv /app/prip-worker/target /app/prip-worker/target
COPY --from=buildenv /app/prip-frontend/target /app/prip-frontend/target
COPY --from=buildenv /app/data-lifecycle-trigger/target /app/data-lifecycle-trigger/target
COPY --from=buildenv /app/data-lifecycle-worker/target /app/data-lifecycle-worker/target