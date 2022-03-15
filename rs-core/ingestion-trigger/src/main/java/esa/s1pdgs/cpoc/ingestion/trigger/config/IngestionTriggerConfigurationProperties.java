package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "ingestion-trigger")
public class IngestionTriggerConfigurationProperties {
	
	private Map<String, InboxConfiguration> polling = new HashMap<>();
	
	private MongoProperties mongodb = new MongoProperties();

	public Map<String, InboxConfiguration> getPolling() {
		return polling;
	}

	public void setPolling(Map<String, InboxConfiguration> polling) {
		this.polling = polling;
	}

	public MongoProperties getMongodb() {
		return mongodb;
	}

	public void setMongodb(MongoProperties mongo) {
		this.mongodb = mongo;
	}

	@Override
	public String toString() {
		return "InboxPollingConfigurationProperties [polling=" + polling + "]";
	}

}
