#!/bin/sh
echo "Starting execution worker..."
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-ipf-execution-worker.jar --spring.config.location=/app/config/application.yml