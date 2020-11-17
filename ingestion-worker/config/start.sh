#!/bin/sh
exec java $jvm_flags_global -Xmx2048m -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-ingestion-worker.jar --spring.config.location=/app/config/application.yml
