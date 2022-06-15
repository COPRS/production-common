ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-container-s1-ipf-simulator:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

USER root

# Create dummy /var/tmp/conf.sh to be used by /app/start.sh
WORKDIR /var/tmp
RUN echo "#!/bin/sh" >> /var/tmp/conf.sh && chmod +x /var/tmp/conf.sh

WORKDIR /app
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Add Tini as entry point
RUN apt-get update && apt-get install tini -y
ENTRYPOINT ["/usr/bin/tini", "--"] 

CMD ["/app/start.sh"]
 