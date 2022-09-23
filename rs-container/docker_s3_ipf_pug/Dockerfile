ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH} as base

FROM artifactory.coprs.esa-copernicus.eu/cfi/processors/sentinel3/s3-pug:3.45
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

### Reconstruction of the base image provided by the agency
USER root

RUN yum install wget -y

RUN useradd -u 1001 rsuser

# Deploy AdoptJDK workaround

WORKDIR /opt 

RUN wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16.1%2B1/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz
RUN tar -xf OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz && rm OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz

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

# Install java 11 - workaround for AdoptJDK
RUN echo 'export PATH=$PATH:/opt/jdk-11.0.16.1+1/bin' >> /app/start.sh

USER rsuser:rsuser

ENTRYPOINT ["/tini", "--", "/app/start.sh"]
