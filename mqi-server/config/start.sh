#!/bin/bash
if [[ ${HOSTNAME} == s1pro-ingestion-[0-9]* ]]
then
    # This is a work around for the ingestion pods as the /app/config directory is mounted as a read-only filesystem
    cat /app/config/application.yml  | sed -e "s;t-pdgs-ingestion-jobs;t-pdgs-ingestion-jobs-${HOSTNAME};g" -e "s;config/outputs.xml;outputs.xml;g" > /app/application.yml
    cat /app/config/outputs.xml  | sed -e "s;t-pdgs-ingestion-jobs;t-pdgs-ingestion-jobs-${HOSTNAME};g" > /app/outputs.xml
    exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/application.yml
fi
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/config/application.yml
