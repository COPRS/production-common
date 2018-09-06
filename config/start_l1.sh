/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/wrapper/ -P /app --reject="index.html*"
/var/tmp/configure_pdgs_custom.sh pac1 8 && ulimit -s unlimited && java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-wrapper.jar --spring.config.location=classpath:/application.yml
