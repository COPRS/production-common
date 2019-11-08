#!/bin/sh
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-queue-watcher.jar --spring.config.location=/app/config/application.yml
