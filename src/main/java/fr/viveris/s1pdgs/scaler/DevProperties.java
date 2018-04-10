package fr.viveris.s1pdgs.scaler;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dev")
public class DevProperties {

	private Map<String, Boolean> activations;

	/**
	 * @return the activations
	 */
	public Map<String, Boolean> getActivations() {
		return activations;
	}

	/**
	 * @param activations the activations to set
	 */
	public void setActivations(Map<String, Boolean> activations) {
		this.activations = activations;
	}
	
	
}
