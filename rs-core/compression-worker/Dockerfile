ARG BRANCH
FROM ghcr.io/coprs/rs-core:${BRANCH} as build
WORKDIR /app

FROM openjdk:8-jre-alpine3.8
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
