ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/cfi/processors/l0_aio:integration-032
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh

# Add Tini as entry point
USER root:root
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

RUN useradd -u 1001 rsuser

USER rsuser:rsuser

ENTRYPOINT ["/app/start.sh"]
