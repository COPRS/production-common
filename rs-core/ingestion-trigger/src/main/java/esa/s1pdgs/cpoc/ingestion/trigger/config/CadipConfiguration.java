package esa.s1pdgs.cpoc.ingestion.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "cadip")
public class CadipConfiguration {
	
	/**
	 * Earliest start time. All session objects published before are ignored
	 */
	private String start;

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}
}
