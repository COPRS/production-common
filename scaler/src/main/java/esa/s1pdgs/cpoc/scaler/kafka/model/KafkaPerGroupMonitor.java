package esa.s1pdgs.cpoc.scaler.kafka.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Consumer monitor for a given group and given topic
 * 
 * @author Cyrielle Gailliard
 */
public class KafkaPerGroupMonitor {

    /**
     * Monitoring date
     */
    private Date monitoringDate;

    /**
     * Group identifier
     */
    private String groupId;

    /**
     * Topic name
     */
    private List<String> topics;

    /**
     * Number of consumers of the group on the topic
     */
    private int nbConsumers;

    /**
     * Number of partitions of the topic
     */
    private int nbPartitions;

    /**
     * Lag per consumers = sum of partition lag group by consumers
     */
    private final Map<String, Long> lagPerConsumers;

    /**
     * Lag per partition
     */
    private final Map<Integer, Long> lagPerPartition;

    /**
     * Default constructor
     */
    public KafkaPerGroupMonitor(Date monitoringDate, String groupId) {
        this.monitoringDate = monitoringDate;
        this.groupId = groupId;
        this.lagPerConsumers = new HashMap<String, Long>();
        this.lagPerPartition = new HashMap<Integer, Long>();
        this.nbPartitions = 0;
        this.nbConsumers = 0;
        this.topics = new ArrayList<>();
    }

    /**
     * Add a monitor
     * 
     * @param monitor
     */
    public void addMonitor(KafkaPerGroupPerTopicMonitor monitor) {
        if (monitor != null) {
            topics.add(monitor.getTopicName());
            nbPartitions = Math.max(nbPartitions, monitor.getNbPartitions());
            for (Integer partition : monitor.getLagPerPartition().keySet()) {
                if (lagPerPartition.containsKey(partition)) {
                    Long value = lagPerPartition.get(partition);
                    lagPerPartition.put(partition, value
                            + monitor.getLagPerPartition().get(partition));
                } else {
                    lagPerPartition.put(partition,
                            monitor.getLagPerPartition().get(partition));
                }
            }
            nbConsumers = Math.max(nbConsumers, monitor.getNbConsumers());
            for (String consumer : monitor.getLagPerConsumers().keySet()) {
                if (lagPerConsumers.containsKey(consumer)) {
                    Long value = lagPerConsumers.get(consumer);
                    lagPerConsumers.put(consumer,
                            value + monitor.getLagPerConsumers().get(consumer));
                } else {
                    lagPerConsumers.put(consumer,
                            monitor.getLagPerConsumers().get(consumer));
                }
            }
        }
    }

    /**
     * @return the monitoringDate
     */
    public Date getMonitoringDate() {
        return monitoringDate;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the topics
     */
    public List<String> getTopics() {
        return topics;
    }

    /**
     * @return the nbConsumers
     */
    public int getNbConsumers() {
        return nbConsumers;
    }

    /**
     * @return the nbPartitions
     */
    public int getNbPartitions() {
        return nbPartitions;
    }

    /**
     * @return the lagPerConsumers
     */
    public Map<String, Long> getLagPerConsumers() {
        return lagPerConsumers;
    }

    /**
     * @return the lagPerPartition
     */
    public Map<Integer, Long> getLagPerPartition() {
        return lagPerPartition;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{monitoringDate: %s, groupId: %s, topics: %s, nbConsumers: %s, nbPartitions: %s, lagPerConsumers: %s, lagPerPartition: %s}",
                monitoringDate, groupId, topics, nbConsumers, nbPartitions,
                lagPerConsumers, lagPerPartition);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(monitoringDate, groupId, topics, nbConsumers,
                nbPartitions, lagPerConsumers, lagPerPartition);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            final KafkaPerGroupMonitor other = (KafkaPerGroupMonitor) obj;
            ret = Objects.equals(monitoringDate, other.monitoringDate)
                    && Objects.equals(groupId, other.groupId)
                    && Objects.equals(topics, other.topics)
                    && nbConsumers == other.nbConsumers
                    && nbPartitions == other.nbPartitions
                    && Objects.equals(lagPerConsumers, other.lagPerConsumers)
                    && Objects.equals(lagPerPartition, other.lagPerPartition);
        }
        return ret;
    }

}
