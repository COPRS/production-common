package fr.viveris.s1pdgs.scaler.kafka;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import fr.viveris.s1pdgs.scaler.kafka.model.SpdgsTopic;

/**
 * Properties used for monitoring KAFKA
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "kafka")
public class KafkaMonitoringProperties {

	/**
	 * Client identifier
	 */
	private String clientId;
	
	/**
	 * Bootstrap servers
	 */
	private String bootstrapServers;
	
	/**
	 * Consumer session timeout in milliseconds
	 */
	private int sessionTimeoutMs;
	
	/**
	 * Administration requests timeout in milliseconds
	 */
	private int requestTimeoutMs;
	
	/**
	 * Topic names
	 */
	private Map<SpdgsTopic, String> topics;
	
	/**
	 * Groups identifier per topics
	 */
	private Map<SpdgsTopic, String> groupIdPerTopic;

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the bootstrapServers
	 */
	public String getBootstrapServers() {
		return bootstrapServers;
	}

	/**
	 * @param bootstrapServers the bootstrapServers to set
	 */
	public void setBootstrapServers(String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}

	/**
	 * @return the sessionTimeoutMs
	 */
	public int getSessionTimeoutMs() {
		return sessionTimeoutMs;
	}

	/**
	 * @param sessionTimeoutMs the sessionTimeoutMs to set
	 */
	public void setSessionTimeoutMs(int sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}

	/**
	 * @return the requestTimeoutMs
	 */
	public int getRequestTimeoutMs() {
		return requestTimeoutMs;
	}

	/**
	 * @param requestTimeoutMs the requestTimeoutMs to set
	 */
	public void setRequestTimeoutMs(int requestTimeoutMs) {
		this.requestTimeoutMs = requestTimeoutMs;
	}

	/**
	 * @return the topics
	 */
	public Map<SpdgsTopic, String> getTopics() {
		return topics;
	}

	/**
	 * @param topics the topics to set
	 */
	public void setTopics(Map<SpdgsTopic, String> topics) {
		this.topics = topics;
	}

	/**
	 * @return the groupIdPerTopic
	 */
	public Map<SpdgsTopic, String> getGroupIdPerTopic() {
		return groupIdPerTopic;
	}

	/**
	 * @param groupIdPerTopic the groupIdPerTopic to set
	 */
	public void setGroupIdPerTopic(Map<SpdgsTopic, String> groupIdPerTopic) {
		this.groupIdPerTopic = groupIdPerTopic;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KafkaMonitoringProperties [clientId=" + clientId + ", bootstrapServers=" + bootstrapServers
				+ ", sessionTimeoutMs=" + sessionTimeoutMs + ", requestTimeoutMs=" + requestTimeoutMs + ", topics="
				+ topics + ", groupIdPerTopic=" + groupIdPerTopic + "]";
	}
	
}
