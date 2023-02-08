ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH} as base

# RS-597: Original image contains DEM files, however the resulting image is to big for the build pipe
# to handle it. A new image without the DEM files are created that will assume the DEM files to be
# on the shared disk.
FROM artifactory.coprs.esa-copernicus.eu/cfi/processors/s1_l12:3.5.2-light
 
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app
 
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/rs-execution-worker.jar rs-execution-worker.jar
COPY --from=build /app/start.sh /app/start.sh
COPY --from=base /log/log4j2.yml /log/log4j2.yml

USER root:root
 
# Install java 11
RUN yum install -y java-11-openjdk 
#RUN update-alternatives --set java /usr/lib/jvm/java-11-openjdk-11.0.4.11-0.el7_6.x86_64/bin/java
RUN alternatives --set java java-11-openjdk.x86_64

# Add Tini as entry point
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN useradd -u 1001 rsuser

USER rsuser:rsuser
 
ENTRYPOINT ["/tini", "--", "/app/start.sh"]
