#!/bin/sh
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-metadata-catalog-timer.jar --spring.config.location=/app/config/application.yml
