FROM centos:6.6

USER root

RUN yum -y install boost boost-thread boost-devel libxslt tar which python3; yum clean all

# create user
ARG USER=ops3pdgs
ARG UID=1000
ARG GID=1000
# default password for user
ARG PW=Ops3Pdgs-1
# Using unencrypted password/ specifying password
RUN useradd -m ${USER} --uid=${UID} && echo "${USER}:${PW}" | \
      chpasswd

COPY files/* /opt/

#create local WD
RUN mkdir -p /data/localWD && chown ops3pdgs:ops3pdgs /data/localWD

WORKDIR /data/localWD

USER ops3pdgs
 
