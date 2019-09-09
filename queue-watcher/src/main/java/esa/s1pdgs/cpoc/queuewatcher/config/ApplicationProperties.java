package esa.s1pdgs.cpoc.queuewatcher.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "watcher")
public class ApplicationProperties {
	private String csvFile;
	
	/**
	 * Base directory that is used for files storing the kafka messages
	 */
	private String kafkaFolder;
	
	/**
	 * A list of kafka topics that should be observed by the service. Each
	 * topic will be stored in a file in the kafkaFolder
	 */
	private List<String> kafkaTopics;

	public String getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(String csvFile) {
		this.csvFile = csvFile;
	}

	public String getKafkaFolder() {
		return kafkaFolder;
	}

	public void setKafkaFolder(String kafkaFolder) {
		this.kafkaFolder = kafkaFolder;
	}

	public List<String> getKafkaTopics() {
		return kafkaTopics;
	}

	public void setKafkaTopics(List<String> kafkaTopics) {
		this.kafkaTopics = kafkaTopics;
	}
}