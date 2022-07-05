package esa.s1pdgs.cpoc.message.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of Kafka consumer / producer / topics
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
	
    /**
     * host:port to use for establishing the initial connection to the Kafka
     * cluster.
     */
    private String bootstrapServers;

    /**
     * Topic name for the errors
     */
    private String errorTopic;

    /**
     * Hostname.
     */
    private String hostname;

    /**
     * ID to pass to the server when making requests. Used for server-side
     * logging.
     */
    private String clientId;

    /**
     * When greater than zero, enables retrying of failed sends.
     */
    private int maxRetries = 0;

    /**
     * Default constructor
     */
    public KafkaProperties() {
        super();
        this.bootstrapServers = "";
    }

    /**
     * @return the bootstrapServers
     */
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /**
     * @return the errorTopic
     */
    public String getErrorTopic() {
        return errorTopic;
    }

    /**
     * @param errorTopic
     *            the errorTopic to set
     */
    public void setErrorTopic(final String errorTopic) {
        this.errorTopic = errorTopic;
    }

    /**
     * @param bootstrapServers
     *            the bootstrapServers to set
     */
    public void setBootstrapServers(final String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the clientId
     */
    //TODO move client id to consumer section as it is only used in consumption context
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
     * @return the maxRetries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @param maxRetries
     *            the maxRetries to set
     */
    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    

    @Override
	public String toString() {
		return "KafkaProperties [bootstrapServers=" + bootstrapServers + ", errorTopic=" + errorTopic + ", hostname="
				+ hostname + ", clientId=" + clientId + ", maxRetries=" + maxRetries + "]";
	}

}