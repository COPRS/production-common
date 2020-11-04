#!/bin/sh
exec java $jvm_flags_global -Xmx3072m -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-compression-worker.jar --spring.config.location=/app/config/application.yml
