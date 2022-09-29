package esa.s1pdgs.cpoc.dlq.manager.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;

@Configuration
public class RoutingTableConfiguration {

	@Autowired
	private DlqManagerConfigurationProperties dlqManagerConfigurationProperties;
	
	@Bean
	public RoutingTable getRoutingTable() {
		return RoutingTable.of(dlqManagerConfigurationProperties.getRouting());
	}
	
}
