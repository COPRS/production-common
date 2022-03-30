package esa.s1pdgs.cpoc.mdc.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("searchcontroller")
public class SearchControllerConfig {
	// Regular Expression used to determinate if the file is an aux file. Required for correct query of aux files
	private String auxPatternConfig;

	public String getAuxPatternConfig() {
		return auxPatternConfig;
	}

	public void setAuxPatternConfig(String auxPatternConfig) {
		this.auxPatternConfig = auxPatternConfig;
	}
	
	
}
