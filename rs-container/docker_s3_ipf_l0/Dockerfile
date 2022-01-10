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

RUN --mount=type=secret,id=ARTIFACTORY_USER export ARTIFACTORY_USER=`cat /run/secrets/artifactory_user`
RUN --mount=type=secret,id=ARTIFACTORY_PASS export ARTIFACTORY_PASS=`cat /run/secrets/artifactory_pass`

RUN yum install -y libgomp java-1.8.0-openjdk

# create user
ARG USER=ops3pdgs
ARG UID=1000
ARG GID=1000

RUN useradd -m ${USER} --uid=${UID} && echo "${USER}:${PW}" | \
      chpasswd

WORKDIR /tmp

RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/S3-IPF-COTS.tgz"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/S3IPF-L0_06.12.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/blas-3.2.1-4.el6.x86_64.rpm"
RUN curl -sSf -u "${ARTIFACTORY_USER}:${ARTIFACTORY_PASS}" -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/lapack-3.2.1-4.el6.x86_64.rpm"

RUN tar -xf S3-IPF-COTS.tgz -C /usr/local
RUN yum install -y blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm; yum clean all
RUN rpm -i S3IPF-L0_06.12.rpm --nodeps
RUN rm -f S3IPF-L0_06.12.rpm blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm

      
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
 