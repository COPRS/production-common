package esa.s1pdgs.cpoc.message.kafka;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

public class LagBasedPartitionerTest {

    @Mock
    KafkaProperties kafkaProperties;

    @Mock
    PartitionLagFetcher fetcher;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void initKafkaProperties() {
        when(kafkaProperties.getBootstrapServers()).thenReturn("server:9092");
        final KafkaProperties.KafkaProducerProperties producer = mock(KafkaProperties.KafkaProducerProperties.class);

        final KafkaProperties.KafkaLagBasedPartitionerProperties lagBasedPartitioner = mock(KafkaProperties.KafkaLagBasedPartitionerProperties.class);
        when(lagBasedPartitioner.getConsumerGroup()).thenReturn("group");

        final Map<String, Integer> topicsWithPriority = new HashMap<>();
        topicsWithPriority.put("topic10", 10);
        topicsWithPriority.put("topic20", 20);
        topicsWithPriority.put("topic301", 30);

        when(lagBasedPartitioner.getTopicsWithPriority()).thenReturn(topicsWithPriority);

        when(producer.getLagBasedPartitioner()).thenReturn(lagBasedPartitioner);

        when(kafkaProperties.getProducer()).thenReturn(producer);
    }

    @Test
    public void partitionOnlyOneTopic() {

        LagBasedPartitioner uut = new LagBasedPartitioner();
        Map<String, Object> config = new HashMap<>();
        config.put(LagBasedPartitioner.KAFKA_PROPERTIES, kafkaProperties);
        config.put(LagBasedPartitioner.PARTITION_LAG_FETCHER_SUPPLIER, (Supplier<PartitionLagFetcher>) () -> fetcher);

        final Map<String, List<PartitionLagFetcher.ConsumerLag>> consumerLags = new HashMap<>();

        consumerLags.put("topic10", asList(
                lag("consumer1", 1, 10),
                lag("consumer1", 2, 0),
                lag("consumer2", 3, 1),
                lag("consumer2", 4, 0)));

        when(fetcher.getConsumerLags()).thenReturn(consumerLags);

        uut.configure(config);

        int partition = uut.partition("topic10", null, null, null, null, null);
        assertThat(partition, is(equalTo(4)));
    }

    @Test
    public void partitionWithHigherTopics() {

        LagBasedPartitioner uut = new LagBasedPartitioner();
        Map<String, Object> config = new HashMap<>();
        config.put(LagBasedPartitioner.KAFKA_PROPERTIES, kafkaProperties);
        config.put(LagBasedPartitioner.PARTITION_LAG_FETCHER_SUPPLIER, (Supplier<PartitionLagFetcher>) () -> fetcher);

        final Map<String, List<PartitionLagFetcher.ConsumerLag>> consumerLags = new HashMap<>();

        consumerLags.put("topic10", asList(
                lag("consumer1", 1, 10),
                lag("consumer1", 2, 0),
                lag("consumer2", 3, 1),
                lag("consumer2", 4, 0)));

        consumerLags.put("topic20", asList(
                lag("consumer1", 1, 5),
                lag("consumer1", 2, 0),
                lag("consumer2", 3, 1),
                lag("consumer2", 4, 33)
        ));

        when(fetcher.getConsumerLags()).thenReturn(consumerLags);

        uut.configure(config);

        int partition = uut.partition("topic10", null, null, null, null, null);
        assertThat(partition, is(equalTo(2)));
    }


    private PartitionLagFetcher.ConsumerLag lag(final String consumerId, final Integer partition, final long lag) {
        return new PartitionLagFetcher.ConsumerLag(consumerId, "n/a", partition, lag, 0);
    }
}