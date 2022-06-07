package esa.s1pdgs.cpoc.dlq.manager.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.dlq.manager.stream.StreamBridgeMessageProducer;

@Configuration
public class MessageProducerConfig {

	@Autowired
	private StreamBridge streamBridge;
	
	@Bean
	public StreamBridgeMessageProducer<String> getMessageProducer() {
		return new StreamBridgeMessageProducer<String>(streamBridge);
	}
}
