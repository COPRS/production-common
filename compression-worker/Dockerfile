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

RUN apk update && apk add p7zip

COPY --from=build /app/compression-worker/target/s1pro-core-compression-worker-*.jar s1pdgs-compression-worker.jar
COPY /config/start.sh start.sh
COPY /config/compression.sh compression.sh
COPY /config/uncompression.sh uncompression.sh

ENTRYPOINT "/app/start.sh"