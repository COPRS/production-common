ARG BRANCH

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY /target/rs-metadata-catalog-extraction-*.jar rs-metadata-catalog-extraction.jar
COPY /config/start.sh start.sh
#COPY /config/xsltDir xslt

ENTRYPOINT ["/app/start.sh"]
