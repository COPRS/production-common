#!/bin/sh
/app/airbus/entrypoint.sh pac1 8 && ulimit -s unlimited
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-ipf-execution-worker.jar --spring.config.location=/app/config/application.yml
