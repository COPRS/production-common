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

COPY --from=build /app/native-api/target/coprs-native-api-*.jar coprs-native-api.jar
COPY /config/start.sh start.sh

COPY --from=build /app/native-api/target/classes/native-api_openapi-gen.yml /app/openapi/native-api_openapi.yml

ENTRYPOINT "/app/start.sh"
