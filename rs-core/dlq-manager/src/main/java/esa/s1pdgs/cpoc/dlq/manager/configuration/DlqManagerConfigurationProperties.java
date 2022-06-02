package esa.s1pdgs.cpoc.dlq.manager.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("dlq-manager")
public class DlqManagerConfigurationProperties {

	private String hostname;
	private String parkingLotTopic;

	private Map<String, Map<String, String>> routing = new LinkedHashMap<>();
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}
	
	public String getParkingLotTopic() {
		return parkingLotTopic;
	}

	public void setParkingLotTopic(String parkingLotTopic) {
		this.parkingLotTopic = parkingLotTopic;
	}

	public Map<String, Map<String, String>> getRouting() {
		return routing;
	}

	public void setRouting(Map<String, Map<String, String>> routing) {
		this.routing = routing;
	}
}
