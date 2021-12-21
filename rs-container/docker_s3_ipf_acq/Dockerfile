ARG BRANCH
FROM ghcr.io/coprs/rs-core:${BRANCH} as build
WORKDIR /app

#FROM artifactory.coprs.esa-copernicus.eu/cfi/sentinel3/s3-ipf0:06.12
FROM centos:7
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT
ARG ARTIFACTORY_USER
ARG ARTIFACTORY_PASS
ARG ARTIFACTORY_URL="https://artifactory.coprs.esa-copernicus.eu:443/artifactory/cfi"


### Reconstruction of the base image provided by the agency
USER root

RUN yum install -y java-1.8.0-openjdk

# create user
ARG USER=ops3pdgs
ARG UID=1000
ARG GID=1000

RUN useradd -m ${USER} --uid=${UID} && echo "${USER}:${PW}" | \
      chpasswd

WORKDIR /tmp

# DDC
RUN mkdir -p /tmp/s3-cgs1-ddc
RUN cd /tmp/s3-cgs1-ddc

RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/S3PD-DDC-1.0-1.0.0-0.x86_64.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/S3PD-DDC-CFG-1.0-1.0.0-00.00.noarch.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/s3-cgs1-ddc.sh"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/s3-cgs1-ddc.rls"

# L0Pre
RUN mkdir -p /tmp/s3-cgs1-l0preproc
RUN cd /tmp/s3-cgs1-l0preproc

RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/S3PD-L0PreProc-1.0-1.0.0-0.x86_64.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/S3PD-L0PreProc-CFG-1.0-1.0.0-00.00.noarch.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/s3-cgs1-l0preproc.sh"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Pre/s3-cgs1-l0preproc.rls"

# L0Post
RUN mkdir -p /tmp/s3-cgs1-l0postproc
RUN cd /tmp/s3-cgs1-l0postproc

RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/S3PD-L0PostProc-1.0-1.0.0-0.x86_64.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/S3PD-L0PostProc-CFG-1.0-1.0.0-00.00.noarch.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/s3-cgs1-l0postproc.sh"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0Post/s3-cgs1-l0postproc.rls"

# Common
RUN mkdir -p /tmp/common
RUN cd /tmp/common

RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/common/S3PD-COTS-libssh2-1.3.0-1.x86_64.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/common/S3PD-COTS-xerces-3.1.1-1.x86_64.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/common/common.rls"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/common/common.sh"

# Functions
cd /tmp
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/s3-cgs1-ddc/functions"

# Deploy
RUN ./s3-cgs1-ddc.sh -i
RUN ./s3-cgs1-l0preproc.sh -i
RUN ./s3-cgs1-l0postproc.sh -i

#RUN rm -f S3IPF-L0_06.12.rpm blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm

      
### End of Base-Image  
WORKDIR /app

RUN mkdir -p /app && chown ops3pdgs:ops3pdgs /app

USER ops3pdgs:ops3pdgs


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

RUN mkdir -p /data/localWD && chown ops3pdgs:ops3pdgs /data/localWD
RUN mkdir -p /data/oqcWD && chown ops3pdgs:ops3pdgs /data/oqcWD

USER ops3pdgs:ops3pdgs

CMD ["/app/start.sh"]
 