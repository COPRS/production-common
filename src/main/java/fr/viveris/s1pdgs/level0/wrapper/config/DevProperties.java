package fr.viveris.s1pdgs.level0.wrapper.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dev")
public class DevProperties {

	private Map<String, Boolean> stepsActivation;
	
	public DevProperties() {
		stepsActivation = new HashMap<>();
	}

	/**
	 * @return the devStepsActivation
	 */
	public Map<String, Boolean> getStepsActivation() {
		return stepsActivation;
	}

	/**
	 * @param devStepsActivation the devStepsActivation to set
	 */
	public void setStepsActivation(Map<String, Boolean> stepsActivation) {
		this.stepsActivation = stepsActivation;
	}

}
