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

COPY --from=build /app/ingestion-trigger/target/s1pro-core-ingestion-trigger-*.jar s1pdgs-ingestion-trigger.jar
COPY /config/start.sh start.sh

ENTRYPOINT "/app/start.sh"
