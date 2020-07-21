package esa.s1pdgs.cpoc.production.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.ConfigurableKeyEvaluator;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.RoutingBasedTasktableMapper;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.SingleTasktableMapper;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;
import esa.s1pdgs.cpoc.xml.XmlConverter;

@Configuration
public class TaskTableMappingConfig {		
	@Value("${tasktable.pathroutingxmlfile}")
	private String routingFile = null;
	
	@Value("${tasktable.name}")	
	private String name = null;
	
	@Value("${tasktable.routingKeyTemplate}")	
	private String routingKeyTemplate = "${product.swathtype}_${product.satelliteId}";
		
	@Bean
	@Autowired
	public TasktableMapper newTastTableMapper(
			final XmlConverter xmlConverter
	) {
		if (name != null) {
			return new SingleTasktableMapper(name);
		}
		if (routingFile != null) {			
			return new RoutingBasedTasktableMapper
					.Factory(xmlConverter, routingFile, new ConfigurableKeyEvaluator(routingKeyTemplate))
					.newMapper();
		}
		throw new IllegalStateException(
				String.format("Missing required elements in configuration: %s", this)
		);
	}

	@Override
	public String toString() {
		return "TaskTableMappingConfig [routingFile=" + routingFile + ", name=" + name + ", routingKeyTemplate="
				+ routingKeyTemplate + "]";
	}
}
