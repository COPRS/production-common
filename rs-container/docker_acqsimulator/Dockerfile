ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-ipf-execution-worker:${BRANCH} as build

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

RUN apk update && apk add python2

COPY files/* /opt/

COPY --from=build /app/s1pdgs-ipf-execution-worker.jar s1pdgs-ipf-execution-worker.jar
COPY /config/start.sh start.sh

#create local WD
RUN mkdir -p /data/localWD

CMD ["/app/start.sh"]