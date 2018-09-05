/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/metadata-catalog/ -P /app --reject="index.html*"
java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-metadata-catalog.jar --spring.config.location=/app/application.yml
