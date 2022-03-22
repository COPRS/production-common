ARG BRANCH
FROM ghcr.io/coprs/rs-core:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/ingestion-trigger/target/s1pro-core-ingestion-trigger-*.jar s1pdgs-ingestion-trigger.jar
COPY /config/start.sh start.sh

ENTRYPOINT "/app/start.sh"
