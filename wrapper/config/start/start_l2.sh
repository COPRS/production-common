#!/bin/sh
/var/tmp/configure_pdgs_custom.sh pac1 8 && ulimit -s unlimited
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-wrapper.jar --spring.config.location=/app/config/application.yml
