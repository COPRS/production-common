#!/bin/sh
exec java $jvm_flags_global $([ ! -z "$JAVA_XMS" ] && echo "-Xms${JAVA_XMS}") $([ ! -z "$JAVA_XMX" ] && echo "-Xmx${JAVA_XMX}") -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-compression-worker.jar --spring.config.location=/app/config/application.yml $@
