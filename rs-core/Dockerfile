####
#The image that is used to deploy the build environment and compile all source code.
#### 
FROM maven:3.5.3-jdk-8 as buildenv

WORKDIR /app
COPY . /app/

ARG MAVEN_ARGS
RUN mvn ${MAVEN_ARGS} -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml install 

ARG PROJECT_KEY
ARG SONARQUBE_HOST
ARG SONARQUBE_TOKEN
ARG BRANCH
RUN mvn sonar:sonar -f /app/pom.xml -s /usr/share/maven/ref/settings-docker.xml -Dsonar.projectKey=${PROJECT_KEY} -Dsonar.host.url=${SONARQUBE_HOST} -Dsonar.login=${SONARQUBE_TOKEN} -Dsonar.branch.name=${BRANCH}

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
COPY --from=buildenv /app/on-demand-interface-provider/target /app/on-demand-interface-provider/target
COPY --from=buildenv /app/metadata-catalog-timer/target /app/metadata-catalog-timer/target
COPY --from=buildenv /app/metadata-catalog-trigger /app/metadata-catalog-trigger
COPY --from=buildenv /app/metadata-catalog-worker /app/metadata-catalog-worker
COPY --from=buildenv /app/mqi-server/target /app/mqi-server/target
#COPY --from=buildenv /app/scaler/target /app/scaler/target
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
COPY --from=buildenv /app/ddip-frontend/target /app/ddip-frontend/target
COPY --from=buildenv /app/native-api/target /app/native-api/target
COPY --from=buildenv /app/data-lifecycle-trigger/target /app/data-lifecycle-trigger/target
COPY --from=buildenv /app/data-request-worker/target /app/data-request-worker/target
COPY --from=buildenv /app/eviction-management-worker/target /app/eviction-management-worker/target
COPY --from=buildenv /app/dissemination-trigger/target /app/dissemination-trigger/target
COPY --from=buildenv /app/dissemination-worker/target /app/dissemination-worker/target
COPY --from=buildenv /app/directory-cleaner/target /app/directory-cleaner/target
