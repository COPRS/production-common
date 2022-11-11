package esa.s1pdgs.cpoc.preparation.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "metrics")
public class PrometheusMetricsProperties {

	private String mission = "";
	
	private String level = "";
	
	private String addonName = "";

	public String getMission() {
		return mission;
	}

	public void setMission(String mission) {
		this.mission = mission;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getAddonName() {
		return addonName;
	}

	public void setAddonName(String addonName) {
		this.addonName = addonName;
	}

	@Override
	public String toString() {
		return "PrometheusMetricsProperties [mission=" + mission + ", level=" + level + ", addonName=" + addonName
				+ "]";
	}
}
