/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.message.kafka;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

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

import esa.s1pdgs.cpoc.message.kafka.config.KafkaConsumerClientId;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties.KafkaLagBasedPartitionerProperties;

public class PartitionLagFetcher implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PartitionLagFetcher.class);

    private final Admin adminClient;
    private final KafkaLagBasedPartitionerProperties properties;

    private volatile ScheduledExecutorService executor;

    private final Map<String, List<ConsumerLag>> consumerLags = new ConcurrentHashMap<>();
    private final Map<TopicPartition, AtomicLong> interimPublications = new ConcurrentHashMap<>();

    public PartitionLagFetcher(final Admin adminClient, final KafkaLagBasedPartitionerProperties properties) {
        this.adminClient = adminClient;
        this.properties = properties;
    }

    private static final long TIMEOUT_SEC = 10L; //TODO configure me

    @Override
    public void run() {
        LOG.debug("running lag kafka lag analyzing for consumer-group {}", properties.getConsumerGroup());
        try {
            consumerLags.clear();
            interimPublications.clear();
            consumerLags.putAll(fetch());
        } catch (Exception e) {
            LOG.error("error during fetch", e);
        }
    }

    public Map<String, List<ConsumerLag>> getConsumerLags() {
        return consumerLags.values().stream()
                .flatMap(Collection::stream)
                .map(lag -> lag.includingInterimPublications(
                        interimPublications.get(lag.getTopicPartition())))
                .collect(groupingBy(ConsumerLag::getTopic));
    }

    public void incInterimPublicationsFor(TopicPartition partition) {
        if (!interimPublications.containsKey(partition)) {
            interimPublications.put(partition, new AtomicLong(0L));
        }

        interimPublications.get(partition).incrementAndGet();
    }

    private Map<String, List<ConsumerLag>> fetch() throws InterruptedException, ExecutionException, TimeoutException {

        final Set<String> topics = properties.getTopicsWithPriority().keySet();
        final Map<String, TopicDescription> topicDescriptions = adminClient.describeTopics(topics).all().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final List<TopicPartition> partitions = new ArrayList<>();

        topicDescriptions.forEach(
                (topic, descriptions) -> descriptions.partitions().forEach(
                        partition -> partitions.add(new TopicPartition(topic, partition.partition()))));

        final Map<TopicPartition, OffsetSpec> offsetSpecsLatest = partitions.stream().collect(toMap(tp -> tp, tp -> OffsetSpec.latest()));

        final Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets
                = adminClient.listOffsets(offsetSpecsLatest).all().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final Map<String, ConsumerGroupDescription> groupDescriptions
                = adminClient.describeConsumerGroups(
                singletonList(properties.getConsumerGroup()))
                .all().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final Map<TopicPartition, OffsetAndMetadata> consumerOffsets
                = adminClient.listConsumerGroupOffsets(
                properties.getConsumerGroup(),
                new ListConsumerGroupOffsetsOptions().topicPartitions(partitions))
                .partitionsToOffsetAndMetadata().get(TIMEOUT_SEC, TimeUnit.SECONDS);

        final Map<String, List<ConsumerLag>> consumerLags = new HashMap<>();
        topics.forEach(topic -> consumerLags.put(topic, new ArrayList<>()));

        final ConsumerGroupDescription consumerGroupDescription = groupDescriptions.get(properties.getConsumerGroup());
        consumerGroupDescription.members().forEach(groupMember -> {
            final String clientId = groupMember.clientId();
            final String hostName = groupMember.host();
            final Set<TopicPartition> assignedPartitions = groupMember.assignment().topicPartitions();

            assignedPartitions.forEach(assignedPartition -> {
                if (!latestOffsets.containsKey(assignedPartition) || !consumerOffsets.containsKey(assignedPartition)) {
                    LOG.debug("no offset information for topic partition {}, skipping it ...", assignedPartition);
                    return;
                }

                final long latestOffset = latestOffsets.get(assignedPartition).offset();
                final long committedOffset = consumerOffsets.get(assignedPartition).offset();
                final ConsumerLag consumerLag
                        = new ConsumerLag(clientId, hostName, assignedPartition.topic(), assignedPartition.partition(), latestOffset, committedOffset);
                consumerLags.get(assignedPartition.topic()).add(consumerLag);
            });
        });

        LOG.debug("fetched consumer lags: {}", consumerLags);
        return consumerLags;
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        final long delay = properties.getDelaySeconds();
        executor.scheduleWithFixedDelay(this, delay, delay, TimeUnit.SECONDS);
        LOG.info("started {} with delay {} seconds", this, delay);
    }

    public void stop() {
        LOG.info("stopping {}", this);
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public String toString() {
        KafkaProperties.KafkaLagBasedPartitionerProperties config = properties;
        return String.format(
                "PartitionLagFetcher { consumer-group: %s partitions: %s delay: %s}",
                config.getConsumerGroup(),
                config.getTopicsWithPriority().keySet(),
                config.getDelaySeconds());
    }

    public static class ConsumerLag {
        private final String clientId;
        private final String hostName;
        private final String topic;
        private final Integer partition;
        private final long committedOffset;
        private final long latestOffset;

        public ConsumerLag(final String clientId, final String hostName, final String topic, final Integer partition, long latestOffset, final long committedOffset) {
            this.clientId = clientId;
            this.hostName = hostName;
            this.topic = topic;
            this.partition = partition;
            this.committedOffset = committedOffset;
            this.latestOffset = latestOffset;
        }

        public String getClientId() {
            return clientId;
        }

        public String getRawClientId() {
            return KafkaConsumerClientId.rawIdForTopic(clientId, topic);
        }

        public String getHostName() {
            return hostName;
        }

        public String getTopic() {
            return topic;
        }

        public Integer getPartition() {
            return partition;
        }

        public TopicPartition getTopicPartition() {
            return new TopicPartition(topic, partition);
        }

        public long getLag() {
            return latestOffset - committedOffset;
        }

        public long getCommittedOffset() {
            return committedOffset;
        }

        public long getLatestOffset() {
            return latestOffset;
        }

        public ConsumerLag includingInterimPublications(AtomicLong interimPublications) {
            if (interimPublications == null || interimPublications.get() == 0L) {
                return this;
            }

            return new ConsumerLag(clientId, hostName, topic, partition, latestOffset + interimPublications.get(), committedOffset);
        }

        @Override
        public String toString() {
            return "ConsumerLag{" +
                    "clientId='" + clientId + '\'' +
                    ", host='" + hostName + '\'' +
                    ", topic='" + topic + '\'' +
                    ", partition=" + partition +
                    ", committedOffset=" + committedOffset +
                    ", latestOffset=" + latestOffset +
                    ", lag=" + getLag() +
                    '}';
        }
    }
}
