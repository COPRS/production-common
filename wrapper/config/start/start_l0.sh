#/bin/sh
wget --cut-dirs=2 --no-parent -nH -r $externalconf_host/l0-wrapper/app/ -P /app --reject="index.html*"
/var/tmp/conf.sh
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-wrapper.jar --spring.config.location=/app/application.yml
