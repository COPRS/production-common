#!/bin/sh
exec java $jvm_flags_global -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-compression-trigger.jar --spring.config.location=/app/config/application.yml
