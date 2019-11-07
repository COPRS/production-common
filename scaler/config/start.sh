#!/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/$externalconf_dir/ -P /app --reject="index.html*"
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-scaler.jar --spring.config.location=/app/config/application.yml
