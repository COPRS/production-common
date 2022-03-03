ARG BRANCH

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY /target/s1pro-core-disseminator-*.jar s1pdgs-disseminator.jar
COPY /config/start.sh start.sh

RUN ulimit 8192

ENTRYPOINT "/app/start.sh"
