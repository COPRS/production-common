#!/bin/sh
exec java $jvm_flags_global -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-core-on-demand-interface-provider.jar --spring.config.location=/app/config/application.yml

