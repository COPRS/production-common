package esa.s1pdgs.cpoc.message.kafka;

import static java.util.Collections.*;
import static java.util.Comparator.comparingLong;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

public class LagBasedPartitioner implements Partitioner {

    public static final String KAFKA_PROPERTIES = LagBasedPartitioner.class.getSimpleName() + ".kafka.properties";

    private static final Logger LOG = LoggerFactory.getLogger(LagBasedPartitioner.class);

    private final Partitioner backupPartitioner = new DefaultPartitioner();
    private volatile PartitionLagFetcher lagAnalyzer = null;
    private volatile KafkaProperties kafkaProperties = null;


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        Integer calculatedPartition = calculatePartitionViaLagFor(topic);
        if (calculatedPartition != null && calculatedPartition >= 0) {
            LOG.debug("calculated partition {} for new message on topic {}", calculatedPartition, topic);
            return calculatedPartition;
        }

        int partition = backupPartitioner.partition(topic, key, keyBytes, value, valueBytes, cluster);
        LOG.debug("use partition {} for new message on topic {} using backup routine", partition, topic);
        return partition;
    }

    private Integer calculatePartitionViaLagFor(String topic) {
        if (kafkaProperties == null || lagAnalyzer == null) {
            return null;
        }

        final Map<String, Long> lagsByConsumerForTopic = lagsForTopic(topic);
        final List<Map<String, Long>> higherLags = lagsForTopics(higherOrEqualTopicsThan(topic));
        final Map<String, Long> sumOfLags = sumLagsOf(concat(singletonList(lagsByConsumerForTopic), higherLags));

        final Map.Entry<String, Long> consumerWithLowestLag = consumerWithLowestLag(sumOfLags);

        if(consumerWithLowestLag == null) {
            return null;
        }

        LOG.debug("consumer with lowest lag is {} with summarized lag {}", consumerWithLowestLag.getKey(), consumerWithLowestLag.getValue());

        return partitionWithLowestLagForTopicAndConsumer(topic, consumerWithLowestLag.getKey());
    }

    private Integer partitionWithLowestLagForTopicAndConsumer(final String topic, final String consumerId) {
        List<PartitionLagFetcher.ConsumerLag> consumerLags = lagAnalyzer.getConsumerLags().getOrDefault(topic, emptyList());

        return consumerLags.stream()
                .filter(lag -> lag.getConsumerId().equals(consumerId))
                .min(comparingLong(PartitionLagFetcher.ConsumerLag::getLag))
                .map(PartitionLagFetcher.ConsumerLag::getPartition).orElse(null);
    }

    private Map.Entry<String, Long> consumerWithLowestLag(Map<String, Long> sumOfLags) {
        return sumOfLags.entrySet().stream().min(comparingByValue()).orElse(null);
    }

    @SafeVarargs
    private final <T> List<T> concat(List<T>... lists) {
        return Stream.of(lists).flatMap(List::stream).collect(Collectors.toList());
    }

    private Map<String, Long> sumLagsOf(List<Map<String, Long>> higherLags) {
        return higherLags.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey, summingLong(Map.Entry::getValue)));
    }

    private Map<String, Long> lagsForTopic(final String topic) {
        final List<PartitionLagFetcher.ConsumerLag> topicConsumerLags = lagAnalyzer.getConsumerLags().get(topic);

        if(topicConsumerLags == null) {
            return emptyMap();
        }

        return topicConsumerLags.stream()
                .collect(groupingBy(
                        PartitionLagFetcher.ConsumerLag::getConsumerId,
                        Collectors.<PartitionLagFetcher.ConsumerLag>summingLong(PartitionLagFetcher.ConsumerLag::getLag)));
    }

    private List<Map<String, Long>> lagsForTopics(List<String> topicsWithEqualOrHigherPriority) {
        return topicsWithEqualOrHigherPriority.stream().map(this::lagsForTopic).collect(toList());
    }

    private List<String> higherOrEqualTopicsThan(String topic) {
        Map<String, Integer> topicsWithPriority = kafkaProperties.getProducer().getLagBasedPartitioner().getTopicsWithPriority();
        Integer thisPriority = topicsWithPriority.getOrDefault(topic, Integer.MAX_VALUE);

        return topicsWithPriority.entrySet().stream()
                .filter(otherTopic -> !otherTopic.getKey().equals(topic) && otherTopic.getValue() >= thisPriority)
                .map(Map.Entry::getKey)
                .collect(toList());
    }

    @Override
    public void close() {
        backupPartitioner.close();
        if (lagAnalyzer != null) {
            lagAnalyzer.stop();
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {
        LOG.debug("configure: {}", configs);
        backupPartitioner.configure(configs);

        if (kafkaProperties == null) {
            kafkaProperties = (KafkaProperties) configs.get(LagBasedPartitioner.KAFKA_PROPERTIES);
        }

        if (lagAnalyzer == null) {
            Map<String, Object> adminConfig = new HashMap<>();
            adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
            lagAnalyzer = new PartitionLagFetcher(Admin.create(adminConfig), kafkaProperties);
            lagAnalyzer.start();
        }

    }

    @Override
    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        backupPartitioner.onNewBatch(topic, cluster, prevPartition);
    }
}
