package esa.s1pdgs.cpoc.message.kafka;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsOptions;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

public class PartitionLagAnalyzer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PartitionLagAnalyzer.class);

    private final Admin adminClient;
    private final KafkaProperties properties;

    private volatile ScheduledExecutorService executor;

    public PartitionLagAnalyzer(final Admin adminClient, final KafkaProperties properties) {
        this.adminClient = adminClient;
        this.properties = properties;
    }

    private static final long TIMEOUT_SEC = 10L; //TODO configure me

    @Override
    public void run() {
        LOG.debug("running lag kafka lag analyzing for consumer-group {}", properties.getProducer().getLagBasedPartitioner().getConsumerGroup());
        try {
            fetch();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("error during fetch", e);
        }
    }

    private void fetch() throws InterruptedException, ExecutionException, TimeoutException {

        final Set<String> topics = properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().keySet();
        final Map<String, TopicDescription> topicDescriptions = adminClient.describeTopics(topics).all().get(10L, TimeUnit.SECONDS);

        final List<TopicPartition> partitions = new ArrayList<>();

        topicDescriptions.forEach(
                (topic, descriptions) -> descriptions.partitions().forEach(
                        partition -> partitions.add(new TopicPartition(topic, partition.partition()))));

        final Map<TopicPartition, OffsetSpec> offsetSpecsLatest = partitions.stream().collect(toMap(tp -> tp, tp -> OffsetSpec.latest()));

        final Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets
                = adminClient.listOffsets(offsetSpecsLatest).all().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final Map<String, ConsumerGroupDescription> groupDescriptions
                = adminClient.describeConsumerGroups(
                        singletonList(properties.getProducer().getLagBasedPartitioner().getConsumerGroup()))
                .all().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final Map<TopicPartition, OffsetAndMetadata> consumerOffsets
                = adminClient.listConsumerGroupOffsets(
                properties.getProducer().getLagBasedPartitioner().getConsumerGroup(),
                new ListConsumerGroupOffsetsOptions().topicPartitions(partitions))
                .partitionsToOffsetAndMetadata().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final Map<String, List<ConsumerLag>> consumerLags = new HashMap<>();
        topics.forEach(topic -> consumerLags.put(topic, new ArrayList<>()));

        final ConsumerGroupDescription consumerGroupDescription = groupDescriptions.get(properties.getProducer().getLagBasedPartitioner().getConsumerGroup());
        consumerGroupDescription.members().forEach(groupMember -> {
            String consumerId = groupMember.consumerId();
            Set<TopicPartition> assignedPartitions = groupMember.assignment().topicPartitions();
            assignedPartitions.forEach(assignedPartition -> {
                final long latestOffset = latestOffsets.get(assignedPartition).offset();
                final long committedOffset = consumerOffsets.get(assignedPartition).offset();
                final long lag = latestOffset - committedOffset;
                final ConsumerLag consumerLag = new ConsumerLag(consumerId, assignedPartition.topic(), assignedPartition.partition(), lag);
                consumerLags.get(assignedPartition.topic()).add(consumerLag);
            });
        });

        LOG.debug("fetched consumer lags: {}", consumerLags);

    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        final long delay = properties.getProducer().getLagBasedPartitioner().getDelaySeconds();
        executor.scheduleWithFixedDelay(this, delay, delay, TimeUnit.SECONDS);
        LOG.info("started {} with delay {} seconds", this, delay);
    }

    public void stop() {
        LOG.info("stopping {}", this);
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public static class ConsumerLag {
        private final String consumerId;
        private final String topic;
        private final Integer partition;
        private final Long lag;

        public ConsumerLag(String consumerId, String topic, Integer partition, Long lag) {
            this.consumerId = consumerId;
            this.topic = topic;
            this.partition = partition;
            this.lag = lag;
        }

        public String getConsumerId() {
            return consumerId;
        }

        public String getTopic() {
            return topic;
        }

        public Integer getPartition() {
            return partition;
        }

        public Long getLag() {
            return lag;
        }

        @Override
        public String toString() {
            return "ConsumerLag{" +
                    "consumerId='" + consumerId + '\'' +
                    ", topic='" + topic + '\'' +
                    ", partition=" + partition +
                    ", lag=" + lag +
                    '}';
        }
    }
}
