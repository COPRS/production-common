ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

#RUN apk update && apk add python2
USER root
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install python2 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

#create local WD
RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD

USER 1001

COPY files/* /opt/

COPY files/DirectDataCaptureServer /usr/local/components/DDC/bin/DirectDataCaptureServer
COPY files/NOOP.sh /usr/local/components/L0PreProc/bin/S3L0PreProcessor
COPY files/S3L0PostProcessor.py /usr/local/components/L0PostProc/bin/S3L0PostProcessor

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY /config/start.sh start.sh

ENTRYPOINT ["/app/start.sh"]