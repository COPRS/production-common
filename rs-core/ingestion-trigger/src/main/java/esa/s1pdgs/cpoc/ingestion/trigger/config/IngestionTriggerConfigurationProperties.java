package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class IngestionTriggerConfigurationProperties {

	private Map<String, InboxConfiguration> polling = new HashMap<>();

	private MongoConfiguration mongodb;

	private AuxipConfiguration auxip;

	private ProcessConfiguration process;

	public Map<String, InboxConfiguration> getPolling() {
		return polling;
	}

	public void setPolling(Map<String, InboxConfiguration> polling) {
		this.polling = polling;
	}

	public MongoConfiguration getMongodb() {
		return mongodb;
	}

	public void setMongodb(MongoConfiguration mongodb) {
		this.mongodb = mongodb;
	}

	public AuxipConfiguration getAuxip() {
		return auxip;
	}

	public void setAuxip(AuxipConfiguration auxip) {
		this.auxip = auxip;
	}

	public ProcessConfiguration getProcess() {
		return process;
	}

	public void setProcess(ProcessConfiguration process) {
		this.process = process;
	}

	@Override
	public String toString() {
		return "InboxPollingConfigurationProperties [polling=" + polling + ", mongodb=" + mongodb.toString()
				+ ", auxip=" + auxip.toString() + ", process=" + process.toString() + "]";
	}

}
