package fr.viveris.s1pdgs.scaler;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * List the configuration avail	ble during the developments
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dev")
public class DevProperties {

	/**
	 * Enable to activate or deactivate each step of the scaler
	 */
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
	public void setActivations(final Map<String, Boolean> activations) {
		this.activations = activations;
	}
	
}
