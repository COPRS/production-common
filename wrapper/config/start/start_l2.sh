#/bin/sh
wget --cut-dirs=2 --no-parent -nH -r $externalconf_host/l2-wrapper/app/ -P /app --reject="index.html*"
/var/tmp/configure_pdgs_custom.sh pac1 8 && ulimit -s unlimited
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-wrapper.jar --spring.config.location=/app/application.yml
