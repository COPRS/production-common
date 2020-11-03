#!/bin/sh
exec java $jvm_flags_global -Xmx1024m -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-ipf-preparation-worker.jar --spring.config.location=/app/config/application.yml
