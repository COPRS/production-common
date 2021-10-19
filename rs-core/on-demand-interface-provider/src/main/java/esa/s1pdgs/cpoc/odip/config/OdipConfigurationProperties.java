package esa.s1pdgs.cpoc.odip.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("odip")
public class OdipConfigurationProperties {

	private String timeliness;

	private Map<String, String> productionTypeToProductFamily = new LinkedHashMap<>();

	public Map<String, String> getProductionTypeToProductFamily() {
		return productionTypeToProductFamily;
	}

	public void setProductionTypeToProductFamily(Map<String, String> productionTypeToProductFamily) {
		this.productionTypeToProductFamily = productionTypeToProductFamily;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(String timeliness) {
		this.timeliness = timeliness;
	}

	@Override
	public String toString() {
		return "OdipConfigurationProperties [timeliness=" + timeliness + ", productionTypeToProductFamily="
				+ productionTypeToProductFamily + "]";
	}

}
