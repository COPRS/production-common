package esa.s1pdgs.cpoc.preparation.worker.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "tasktable")
public class TaskTableMappingProperties {	
	
	private String routingKeyTemplate = "${product.swathtype}_${product.satelliteId}";
	private Map<String, String> routing = new HashMap<>();
	
	public String getRoutingKeyTemplate() {
		return routingKeyTemplate;
	}

	public void setRoutingKeyTemplate(String routingKeyTemplate) {
		this.routingKeyTemplate = routingKeyTemplate;
	}

	public Map<String, String> getRouting() {
		return routing;
	}

	public void setRouting(Map<String, String> routing) {
		this.routing = routing;
	}

	@Override
	public String toString() {
		return "TaskTableMappingConfig [routingKeyTemplate="
				+ routingKeyTemplate + ", routing" + routing.toString() + "]";
	}
}
