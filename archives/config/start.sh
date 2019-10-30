#!/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/$archives_type/ -P /app --reject="index.html*"
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-archives.jar --spring.config.location=/app/application.yml
