package esa.s1pdgs.cpoc.dlq.manager.configuration;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;
import esa.s1pdgs.cpoc.dlq.manager.service.DlqManagerService;

@Configuration
public class DlqManagerServiceConfiguration {

	@Autowired
	private RoutingTable routingTable;

	@Autowired
	private DlqManagerConfigurationProperties dlqManagerConfigurationProperties;
	
	@Bean
	public Function<Message<byte[]>, List<Message<byte[]>>> route() {
		return new DlqManagerService(routingTable, dlqManagerConfigurationProperties);
	}
}
