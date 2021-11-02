ARG BRANCH
FROM ghcr.io/coprs/rs-core:${BRANCH} as build
WORKDIR /app

FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH}
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app
RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

USER root:root

RUN mkdir -p /usr/local/conf
COPY IPFSimulator.tar /usr/local/conf
RUN tar -xvf /usr/local/conf/IPFSimulator.tar -C /usr/local/conf

RUN ln -s /usr/local/conf/IPFSimulator/bin/Processor_wrapper.sh S3IPF.sh

# link task table binaries
# rsync required for ipf-sim-for-tc25
RUN apk add rsync

ADD files/linkBin.sh /app/linkBin.sh
RUN /app/linkBin.sh

COPY --from=build /app/ipf-execution-worker/target/s1pro-core-ipf-execution-worker-*.jar /app/s1pdgs-ipf-execution-worker.jar
ADD files/start.sh /app/start.sh

ADD files/ipf-sim-for-tc25.sh /usr/local/components/S3IPF_L0_05.02/bin/OL_0_CR_.bin

# Create folder for IPFSimulator
RUN mkdir -p /data/ipf-s3

CMD ["/app/start.sh"]
