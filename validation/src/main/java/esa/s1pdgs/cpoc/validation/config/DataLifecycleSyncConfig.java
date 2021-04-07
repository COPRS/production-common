package esa.s1pdgs.cpoc.validation.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;

@Configuration
@ConfigurationProperties("data-lifecycle-sync")
public class DataLifecycleSyncConfig {
	
	private List<RetentionPolicy> retentionPolicies = new ArrayList<>();

	public List<RetentionPolicy> getRetentionPolicies() {
		return retentionPolicies;
	}

	public void setRetentionPolicies(List<RetentionPolicy> retentionPolicies) {
		this.retentionPolicies = retentionPolicies;
	}
}
