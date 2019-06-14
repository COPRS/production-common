#/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/error-repository/ -P /app --reject="index.html*"
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-error-repository.jar --spring.config.location=/app/application.yml
