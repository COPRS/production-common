ARG BRANCH

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY /target/s1pro-core-ingestion-filter-*.jar s1pdgs-ingestion-filter.jar
COPY /config/start.sh start.sh

RUN chown -R 1001 /app

ENTRYPOINT ["/app/start.sh"]
