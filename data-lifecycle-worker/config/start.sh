#!/bin/sh
exec java $jvm_flags_global -Xmx512m -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-data-lifecycle-worker.jar --spring.config.location=/app/config/application.yml
