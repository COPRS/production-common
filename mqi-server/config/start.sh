#!/bin/bash
if [[ ${HOSTNAME} == inbox-ingestion* ]]
then
    # This is a work around for the ingestion pods as the /app/config directory is mounted as a read-only filesystem
    cat /app/config/application.yml  | sed "s;t-pdgs-ingestion;t-pdgs-ingestion-${HOSTNAME};g" "s;config/outputs.xml;outputs.xml;g" > /app/application.yml
    cat /app/config/outputs.xml  | sed "s;t-pdgs-ingestion;t-pdgs-ingestion-${HOSTNAME};g" > /app/outputs.xml
    exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/application.yml
fi
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/config/application.yml
