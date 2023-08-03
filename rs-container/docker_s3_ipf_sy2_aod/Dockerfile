ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH} as base

FROM artifactory.coprs.esa-copernicus.eu/cfi/processors/sentinel3/s3-sy2-aod:01.09
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

### Reconstruction of the base image provided by the agency 
USER root

RUN yum install java-11-openjdk -y

RUN useradd -u 1001 rsuser

### End of Base-Image  
WORKDIR /app
RUN mkdir -p /app && chown rsuser:rsuser /app

USER rsuser:rsuser

# Adding Version information
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

# Adding execution worker
COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
COPY --from=base /log/log4j2.yml /log/log4j2.yml

USER root:root

# Add Tini as entry point
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD
RUN mkdir -p /data/oqcWD && chown rsuser:rsuser /data/oqcWD

USER rsuser:rsuser

ENTRYPOINT ["/tini", "--", "/app/start.sh"]