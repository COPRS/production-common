package esa.s1pdgs.cpoc.preparation.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "tasktable")
public class TaskTableMappingProperties {	
	
	private String routingFile = null;
	private String name = null;
	private String routingKeyTemplate = "${product.swathtype}_${product.satelliteId}";
	
	public String getRoutingFile() {
		return routingFile;
	}

	public void setRoutingFile(String routingFile) {
		this.routingFile = routingFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoutingKeyTemplate() {
		return routingKeyTemplate;
	}

	public void setRoutingKeyTemplate(String routingKeyTemplate) {
		this.routingKeyTemplate = routingKeyTemplate;
	}

	@Override
	public String toString() {
		return "TaskTableMappingConfig [routingFile=" + routingFile + ", name=" + name + ", routingKeyTemplate="
				+ routingKeyTemplate + "]";
	}
}
