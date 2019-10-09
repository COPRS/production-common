ARG BRANCH
FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:${BRANCH} as build
WORKDIR /app

FROM alpine:latest

ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

RUN apk update && apk add p7zip

COPY --from=build /app/compression/target/s1pro-core-compression-*.jar s1pdgs-compression.jar

COPY /config/start.sh start.sh
COPY /config/compression.sh compression.sh
ENTRYPOINT "/app/start.sh"