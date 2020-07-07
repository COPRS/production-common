#!/bin/sh
exec java -Xmx1536m -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-ingestion-worker.jar --spring.config.location=/app/config/application.yml
