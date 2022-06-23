ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-execution-worker:${BRANCH} as build

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

#RUN apk update && apk add python2
USER root
RUN apt-get update && apt-get install -y python2

#create local WD
RUN mkdir -p /data/localWD

USER 1001

COPY files/* /opt/

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY /config/start.sh start.sh

ENTRYPOINT ["/app/start.sh"]