package fr.viveris.s1pdgs.mqi.server;

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
     * ID to pass to the server when making requests. Used for server-side
     * logging.
     */
    private String clientId;

    /**
     * Consumer properties
     */
    private KafkaConsumerProperties consumer;

    /**
     * Listener properties
     */
    private KafkaListenerProperties listener;

    /**
     * Producer properties
     */
    private KafkaProducerProperties producer;

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
     * @return the consumer
     */
    public KafkaConsumerProperties getConsumer() {
        return consumer;
    }

    /**
     * @param consumer
     *            the consumer to set
     */
    public void setConsumer(final KafkaConsumerProperties consumer) {
        this.consumer = consumer;
    }

    /**
     * @return the listener
     */
    public KafkaListenerProperties getListener() {
        return listener;
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setListener(final KafkaListenerProperties listener) {
        this.listener = listener;
    }

    /**
     * @return the producer
     */
    public KafkaProducerProperties getProducer() {
        return producer;
    }

    /**
     * @param producer
     *            the producer to set
     */
    public void setProducer(final KafkaProducerProperties producer) {
        this.producer = producer;
    }

    /**
     * Properties of a KAFKA producer
     * 
     * @author Viveris Technologies
     */
    public static class KafkaConsumerProperties {

        /**
         * Unique string that identifies the consumer group to which this
         * consumer belongs.
         */
        private String groupId;

        /**
         * Maximum number of records returned in a single call to poll().
         */
        private int maxPollRecords;

        /**
         * 
         */
        private int maxPollIntervalMs;

        /**
         * Expected time between heartbeats to the consumer coordinator.
         */
        private int heartbeatIntvMs;

        /**
         * 
         */
        private int sessionTimeoutMs;

        /**
         * What to do when there is no initial offset in Kafka or if the current
         * offset does not exist any more on the server (e.g. because that data
         * has been deleted):
         * <ul>
         * <li>earliest: automatically reset the offset to the earliest offset
         * <li>latest: automatically reset the offset to the latest offset</li>
         * <li>none: throw exception to the consumer if no previous offset is
         * found for the consumer's group</li>
         * <li>anything else: throw exception to the consumer.</li>
         * </ul>
         */
        private String autoOffsetReset;

        /**
         * Constructor
         */
        public KafkaConsumerProperties() {
            super();
            maxPollRecords = 0;
            sessionTimeoutMs = 0;
            maxPollIntervalMs = 0;
            heartbeatIntvMs = 0;
        }

        /**
         * @return the groupId
         */
        public String getGroupId() {
            return groupId;
        }

        /**
         * @param groupId
         *            the groupId to set
         */
        public void setGroupId(final String groupId) {
            this.groupId = groupId;
        }

        /**
         * @return the maxPollRecords
         */
        public int getMaxPollRecords() {
            return maxPollRecords;
        }

        /**
         * @param maxPollRecords
         *            the maxPollRecords to set
         */
        public void setMaxPollRecords(final int maxPollRecords) {
            this.maxPollRecords = maxPollRecords;
        }

        /**
         * @return the maxPollIntervalMs
         */
        public int getMaxPollIntervalMs() {
            return maxPollIntervalMs;
        }

        /**
         * @param maxPollIntervalMs
         *            the maxPollIntervalMs to set
         */
        public void setMaxPollIntervalMs(final int maxPollIntervalMs) {
            this.maxPollIntervalMs = maxPollIntervalMs;
        }

        /**
         * @return the heartbeatIntervalMs
         */
        public int getHeartbeatIntvMs() {
            return heartbeatIntvMs;
        }

        /**
         * @param heartbeatIntvMs
         *            the heartbeatIntvMs to set
         */
        public void setHeartbeatIntvMs(final int heartbeatIntvMs) {
            this.heartbeatIntvMs = heartbeatIntvMs;
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
         * @return the autoOffsetReset
         */
        public String getAutoOffsetReset() {
            return autoOffsetReset;
        }

        /**
         * @param autoOffsetReset
         *            the autoOffsetReset to set
         */
        public void setAutoOffsetReset(final String autoOffsetReset) {
            this.autoOffsetReset = autoOffsetReset;
        }

    }

    /**
     * Properties of a KAFKA listener
     * 
     * @author Viveris Technologies
     */
    public static class KafkaListenerProperties {

        /**
         * Timeout to use when polling the consumer.
         */
        private long pollTimeoutMs;

        /**
         * Constructor
         */
        public KafkaListenerProperties() {
            super();
            this.pollTimeoutMs = 0;
        }

        /**
         * @return the pollTimeoutMs
         */
        public long getPollTimeoutMs() {
            return pollTimeoutMs;
        }

        /**
         * @param pollTimeoutMs
         *            the pollTimeoutMs to set
         */
        public void setPollTimeoutMs(final long pollTimeoutMs) {
            this.pollTimeoutMs = pollTimeoutMs;
        }

    }

    /**
     * Properties of a KAFKA producer
     * 
     * @author Viveris Technologies
     */
    public static class KafkaProducerProperties {

        /**
         * When greater than zero, enables retrying of failed sends.
         */
        private int maxRetries;

        /**
         * Default constructor
         */
        public KafkaProducerProperties() {
            super();
            this.maxRetries = 0;
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

    }
}
