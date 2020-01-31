ARG BRANCH
FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:${BRANCH} as build
WORKDIR /app

FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-container-ipf-simulator:${BRANCH}
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

COPY --from=build /app/ipf-execution-worker/target/s1pro-core-ipf-execution-worker-*.jar s1pdgs-ipf-execution-worker.jar
COPY --from=build /app/ipf-execution-worker/config/start/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Add Tini as entry point
RUN apk add --no-cache tini
ENTRYPOINT ["/sbin/tini", "--"]

CMD ["/app/start.sh"]