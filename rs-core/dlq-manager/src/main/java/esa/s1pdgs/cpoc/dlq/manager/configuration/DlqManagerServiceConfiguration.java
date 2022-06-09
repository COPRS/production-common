package esa.s1pdgs.cpoc.dlq.manager.configuration;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;
import esa.s1pdgs.cpoc.dlq.manager.service.DlqManagerService;
import esa.s1pdgs.cpoc.dlq.manager.stream.StreamBridgeMessageProducer;

@Configuration
public class DlqManagerServiceConfiguration {

	@Autowired
	private RoutingTable routingTable;

	@Autowired
	private DlqManagerConfigurationProperties dlqManagerConfigurationProperties;

	@Autowired
	private StreamBridgeMessageProducer<String> producer;
	
	@Bean
	public Consumer<Message<byte[]>> route() {
		return new DlqManagerService(routingTable, producer, dlqManagerConfigurationProperties);
	}
}
