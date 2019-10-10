ARG BRANCH
FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:${BRANCH} as build
WORKDIR /app

FROM openjdk:8-jre-alpine
ARG VERSION
ARG COMMIT_ID
ARG BRANCH_TEXT

WORKDIR /app

RUN echo "${VERSION}" >> VERSION
RUN echo "${BRANCH_TEXT}" >> VERSION
RUN echo "${COMMIT_ID}" >> VERSION

COPY --from=build /app/mqi-server/target/s1pro-core-mqi-server-*.jar mqi-server.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
