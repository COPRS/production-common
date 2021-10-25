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

COPY --from=build /app/on-demand-interface-provider/target/s1pro-core-on-demand-interface-provider-*.jar s1pdgs-core-on-demand-interface-provider.jar
COPY /config/start.sh start.sh

ENTRYPOINT "/app/start.sh"
