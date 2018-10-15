#/bin/sh
wget --cut-dirs=2 --no-parent -nH -r $externalconf_host/$externalconf_dir/app/ -P /app --reject="index.html*"
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-job-generator.jar --spring.config.location=/app/application.yml
