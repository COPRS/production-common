#!/bin/sh
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-ingestion-trigger.jar --spring.config.location=/app/config/application.yml
