package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ingestion-trigger")
public class IngestionTriggerConfigurationProperties {
	
	private List<InboxConfiguration> polling = new ArrayList<>();

	public List<InboxConfiguration> getPolling() {
		return polling;
	}

	public void setPolling(List<InboxConfiguration> polling) {
		this.polling = polling;
	}

	@Override
	public String toString() {
		return "InboxPollingConfigurationProperties [polling=" + polling + "]";
	}

}
