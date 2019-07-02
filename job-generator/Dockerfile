ARG BRANCH
FROM registry.tools.s1pdgs.eu/werum/processing/s1pro-core:${BRANCH} as build
WORKDIR /app

FROM openjdk:8-jre-alpine
ARG BRANCH_TEXT
WORKDIR /app
RUN echo "${BRANCH_TEXT}" >> VERSION
COPY --from=build /app/job-generator/target/s1pro-core-job-generator-*.jar s1pdgs-job-generator.jar
RUN apk update && apk add wget
COPY /config/start.sh start.sh
ENTRYPOINT "/app/start.sh"
