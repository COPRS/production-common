/bin/sh
mkdir routing-files
wget $externalconf_host/routing-files/auxiliary-files.xml -P routing-files/
wget $externalconf_host/routing-files/edrs-sessions.xml -P routing-files/
wget $externalconf_host/routing-files/level-jobs.xml -P routing-files/
wget $externalconf_host/routing-files/level-products.xml -P routing-files/
wget $externalconf_host/routing-files/level-reports.xml -P routing-files/
java -Djava.security.egd=file:/dev/./urandom -jar /app/mqi-server.jar --spring.config.location=classpath:/application.yml
