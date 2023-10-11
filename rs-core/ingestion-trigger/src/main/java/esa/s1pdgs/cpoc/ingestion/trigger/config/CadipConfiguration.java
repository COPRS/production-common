package esa.s1pdgs.cpoc.ingestion.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Component
@Validated
@ConfigurationProperties(prefix = "cadip")
public class CadipConfiguration {
	
	/**
	 * Earliest start time. All session objects published before are ignored
	 */
	private String start;

	private ProductFamily retransferFamily = ProductFamily.BLANK;
	
	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public ProductFamily getRetransferFamily() {
		return retransferFamily;
	}

	public void setRetransferFamily(ProductFamily retransferFamily) {
		this.retransferFamily = retransferFamily;
	}
}
