
FROM ubuntu:focal-20211006

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install default-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN useradd rsuser
USER 1001