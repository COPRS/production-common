package esa.s1pdgs.cpoc.preparation.worker.config.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "s3-synergy")
public class S3SynergyProperties {

	/**
	 * map for dynamic process parameters which are not part of the metadata (ex.
	 * facilityName)
	 */
	private Map<String, String> dynProcParams = new HashMap<>();

	public Map<String, String> getDynProcParams() {
		return dynProcParams;
	}

	public void setDynProcParams(Map<String, String> dynProcParams) {
		this.dynProcParams = dynProcParams;
	}
}
