#/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/$externalconf_dir/mqi-server/ -P /app --reject="index.html*"
exec java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=/app/application.yml
