#!/bin/sh
exec java -Xmx3072 -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-compression-worker.jar --spring.config.location=/app/config/application.yml
