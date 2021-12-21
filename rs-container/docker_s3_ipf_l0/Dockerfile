ARG BRANCH
FROM ghcr.io/coprs/rs-core:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/cfi/sentinel3/s3-ipf0:06.12
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

# Basic setup of the image
USER root:root
RUN groupadd -r s1pdgs --gid=1000 && useradd -r -g s1pdgs --uid=1000 s1pdgs
USER s1pdgs:s1pdgs

# Adding Version information
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

# Adding execution worker
COPY --from=build /app/ipf-execution-worker/target/s1pro-core-ipf-execution-worker-*.jar s1pdgs-ipf-execution-worker.jar
COPY --from=build /app/ipf-execution-worker/config/start.sh /app/start.sh

# Add Tini as entry point
USER root:root
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

RUN mkdir -p /data/oqcWD
RUN chown s1pdgs:s1pdgs /data/oqcWD

USER s1pdgs:s1pdgs

CMD ["/app/start.sh"]
 