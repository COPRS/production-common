/bin/sh
wget $externalconf_host/template_l1_wrapper_pod.yml -P /app/config/
java -Djava.security.egd=file:/dev/./urandom -jar /app/s1pdgs-scaler.jar --spring.config.location=classpath:/application.yml
