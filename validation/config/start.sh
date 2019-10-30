#!/bin/sh
wget --cut-dirs=2 --no-parent -nH -r $externalconf_host/validation/app -P /app --reject="index.html*"
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-validation.jar --spring.config.location=/app/application.yml
