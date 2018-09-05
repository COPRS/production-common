/bin/sh
wget --cut-dirs=1 --no-parent -nH -r $externalconf_host/job-generator/ -P /app --reject="index.html*"
java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-job-generator.jar --spring.config.location=classpath:/application.yml
