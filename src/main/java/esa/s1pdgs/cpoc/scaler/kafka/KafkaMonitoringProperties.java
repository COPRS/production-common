package esa.s1pdgs.cpoc.scaler.kafka;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.scaler.kafka.model.SpdgsTopic;

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
	 * Client connection max idle
	 */
	private long cnxMaxIdlMs;

	/**
	 * Topic names
	 */
	private Map<SpdgsTopic, List<String>> topics;

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
	 * @param clientId
	 *            the clientId to set
	 */
	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the bootstrapServers
	 */
	public String getBootstrapServers() {
		return bootstrapServers;
	}

	/**
	 * @param bootstrapServers
	 *            the bootstrapServers to set
	 */
	public void setBootstrapServers(final String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}

	/**
	 * @return the sessionTimeoutMs
	 */
	public int getSessionTimeoutMs() {
		return sessionTimeoutMs;
	}

	/**
	 * @param sessionTimeoutMs
	 *            the sessionTimeoutMs to set
	 */
	public void setSessionTimeoutMs(final int sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}

	/**
	 * @return the requestTimeoutMs
	 */
	public int getRequestTimeoutMs() {
		return requestTimeoutMs;
	}

	/**
	 * @param requestTimeoutMs
	 *            the requestTimeoutMs to set
	 */
	public void setRequestTimeoutMs(final int requestTimeoutMs) {
		this.requestTimeoutMs = requestTimeoutMs;
	}

	/**
	 * @return the cnxMaxIdlMs
	 */
	public long getCnxMaxIdlMs() {
		return cnxMaxIdlMs;
	}

	/**
	 * @param cnxMaxIdlMs
	 *            the cnxMaxIdlMs to set
	 */
	public void setCnxMaxIdlMs(final long cnxMaxIdlMs) {
		this.cnxMaxIdlMs = cnxMaxIdlMs;
	}

	/**
	 * @return the topics
	 */
	public Map<SpdgsTopic, List<String>> getTopics() {
		return topics;
	}

	/**
	 * @param topics
	 *            the topics to set
	 */
	public void setTopics(final Map<SpdgsTopic, List<String>> topics) {
		this.topics = topics;
	}

	/**
	 * @return the groupIdPerTopic
	 */
	public Map<SpdgsTopic, String> getGroupIdPerTopic() {
		return groupIdPerTopic;
	}

	/**
	 * @param groupIdPerTopic
	 *            the groupIdPerTopic to set
	 */
	public void setGroupIdPerTopic(final Map<SpdgsTopic, String> groupIdPerTopic) {
		this.groupIdPerTopic = groupIdPerTopic;
	}

}
