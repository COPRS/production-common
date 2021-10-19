ARG BRANCH
FROM ghcr.io/coprs/s1pro-core:${BRANCH} as build
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

COPY --from=build /app/compression-trigger/target/s1pro-core-compression-trigger-*.jar s1pdgs-compression-trigger.jar
COPY /config/start.sh start.sh

ENTRYPOINT "/app/start.sh"
