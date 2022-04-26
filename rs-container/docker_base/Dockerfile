
FROM ubuntu:focal-20211006

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install default-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Add logging files to base image
COPY /log /log
RUN chmod 666 /log/*

# Add user for reference system
RUN useradd -u 1001 rsuser
USER rsuser