ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app
 
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH} as base

ROM artifactory.coprs.esa-copernicus.eu/cfi/processors/s1_l12:3.8.0
 
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

RUN mkdir -p /data/localWD && chown piccontrol:pic_run /data/localWD
RUN mkdir -p /data/oqcWD && chown piccontrol:pic_run /data/oqcWD
 
### WA RS-815 START ###
#RUN useradd -u 1001 rsuser
 
#USER rsuser:rsuser

USER  piccontrol:pic_run
### WA RS- 815 END ###

ENTRYPOINT ["/tini", "--", "/app/start.sh"]
