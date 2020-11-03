#!/bin/sh
exec java -Xmx256m -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-validation.jar --spring.config.location=/app/config/application.yml
