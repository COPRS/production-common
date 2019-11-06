#!/bin/bash
wget --cut-dirs=2 --no-parent -nH -r $externalconf_host/$externalconf_dir/mqi-server/ -P /app --reject="index.html*"
if [[ ${HOSTNAME} == inbox-ingestion* ]]
then
	sed -i "s;t-pdgs-ingestion;t-pdgs-ingestion-${HOSTNAME};g" /app/application.yml routing-files/outputs.xml
fi
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/config/application.yml
