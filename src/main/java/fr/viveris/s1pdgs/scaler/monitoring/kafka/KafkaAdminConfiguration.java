package fr.viveris.s1pdgs.scaler.monitoring.kafka;

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kafka.admin.AdminClient;

/**
 * KAFKA administration configuration
 * @author Cyrielle Gailliard
 *
 */
@Configuration
public class KafkaAdminConfiguration {

	/**
	 * Properties
	 */
	private final KafkaMonitoringProperties properties;
	
	/**
	 * Constructor
	 * @param properties
	 */
	@Autowired
	public KafkaAdminConfiguration(final KafkaMonitoringProperties properties) {
		this.properties = properties;
	}
	
	/**
	 * KAFKA administration bean
	 * @return
	 */
	@Bean
	public AdminClient kafkaAdminClient() {
		Properties props = new Properties();
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
		return AdminClient.create(props);
	}

}
