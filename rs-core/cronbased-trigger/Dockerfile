ARG BRANCH

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY /target/s1pro-core-cronbased-trigger-*.jar s1pdgs-cronbased-trigger.jar
COPY /config/start.sh start.sh

ENTRYPOINT "/app/start.sh"
