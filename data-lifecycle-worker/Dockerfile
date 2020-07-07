ARG BRANCH
FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:${BRANCH} as build
WORKDIR /app

FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-container-alpine_base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/data-lifecycle-worker/target/s1pro-core-data-lifecycle-worker-*.jar s1pdgs-data-lifecycle-worker.jar
COPY /config/start.sh start.sh

ENTRYPOINT "/app/start.sh"
