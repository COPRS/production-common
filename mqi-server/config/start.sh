#!/bin/bash
if [[ ${HOSTNAME} == inbox-ingestion* ]]
then
	sed -i "s;t-pdgs-ingestion;t-pdgs-ingestion-${HOSTNAME};g" /app/config/application.yml routing-files/outputs.xml
fi
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/config/application.yml
