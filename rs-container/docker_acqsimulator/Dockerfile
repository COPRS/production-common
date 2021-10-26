ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-base:${BRANCH} as build

FROM centos:6.6

USER root

RUN yum -y install boost boost-thread boost-devel libxslt tar which python3 java-1.8.0-openjdk; yum clean all

COPY files/* /opt/

COPY --from=build /app/s1pdgs-ipf-execution-worker.jar /app/s1pdgs-ipf-execution-worker.jar
COPY config/start.sh /app/start.sh

#create local WD
RUN mkdir -p /data/localWD

CMD ["/app/start.sh"]
