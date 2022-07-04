ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-execution-worker:${BRANCH} as build
WORKDIR /app

#FROM artifactory.coprs.esa-copernicus.eu/cfi/sentinel3/s3-ipf0:06.12
FROM centos:7
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT
ARG ARTIFACTORY_URL="https://artifactory.coprs.esa-copernicus.eu:443/artifactory/cfi"


### Reconstruction of the base image provided by the agency
USER root

RUN yum install -y libgomp java-1.8.0-openjdk

# create user
ARG USER=rsuser
ARG UID=1001
ARG GID=1001

RUN useradd -m ${USER} --uid=${UID} && echo "${USER}:${PW}" | \
      chpasswd

WORKDIR /tmp

RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/S3-IPF-COTS.tgz"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/S3IPF-L0_06.12.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/blas-3.2.1-4.el6.x86_64.rpm"
RUN --mount=type=secret,id=ARTIFACTORY_USER --mount=type=secret,id=ARTIFACTORY_PASS curl -sSf -u $(cat /run/secrets/ARTIFACTORY_USER):$(cat /run/secrets/ARTIFACTORY_PASS) -O "${ARTIFACTORY_URL}/S3-RPM/dockerfileS3L0/lapack-3.2.1-4.el6.x86_64.rpm"

RUN tar -xf S3-IPF-COTS.tgz -C /usr/local
RUN yum install -y blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm; yum clean all
RUN rpm -i S3IPF-L0_06.12.rpm --nodeps
RUN rm -f S3IPF-L0_06.12.rpm blas-3.2.1-4.el6.x86_64.rpm lapack-3.2.1-4.el6.x86_64.rpm

      
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

# Add Tini as entry point
USER root:root
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

RUN mkdir -p /data/localWD && chown rsuser:rsuser /data/localWD
RUN mkdir -p /data/oqcWD && chown rsuser:rsuser /data/oqcWD

USER rsuser:rsuser

ENTRYPOINT ["/app/start.sh"]
