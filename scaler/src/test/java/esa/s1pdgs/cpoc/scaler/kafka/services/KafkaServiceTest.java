package esa.s1pdgs.cpoc.scaler.kafka.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.scaler.config.KafkaMonitoringProperties;
import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerGroupsDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.PartitionDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.SpdgsTopic;
import esa.s1pdgs.cpoc.scaler.kafka.services.KafkaService;
import kafka.admin.AdminClient.ConsumerSummary;

public class KafkaServiceTest {

    /**
     * KAFKA admin client
     */
    @Mock
    private AdminClient kafkaAdminClient;

    /**
     * Properties
     */
    @Mock
    private KafkaMonitoringProperties properties;

    /**
     * Consumer
     */
    @Mock
    private KafkaConsumer<String, String> consumer;

    @Mock
    private ConsumerSummary kConsumer1;
    @Mock
    private ConsumerSummary kConsumer2;

    /**
     * Service to test
     */
    private KafkaService service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        Map<SpdgsTopic, String> topics = new HashMap<>();
        topics.put(SpdgsTopic.L1_JOBS, "topic");
        Map<SpdgsTopic, String> groups = new HashMap<>();
        groups.put(SpdgsTopic.L1_JOBS, "group");
        doReturn(topics).when(properties).getTopics();
        doReturn(groups).when(properties).getGroupIdPerTopic();
        doReturn("172.20.35.2:9093").when(properties).getBootstrapServers();
        doReturn("client").when(properties).getClientId();
        doReturn(15000).when(properties).getSessionTimeoutMs();

        service = new KafkaService(kafkaAdminClient, properties);
    }

    @Test
    public void testKafkaConsumerProperties() {
        Properties prop = service.kafkaConsumerProperties("group");
        assertEquals(7, prop.size());
        assertEquals("172.20.35.2:9093",
                prop.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("client",
                prop.getProperty(ConsumerConfig.CLIENT_ID_CONFIG));
        assertEquals("group", prop.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(false, prop.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals(15000, prop.get(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
    }

    @Test
    public void testCloseKafkaConsumer() {
        doNothing().when(consumer).close();

        service.closeKafkaConsumer(null);

        service.closeKafkaConsumer(consumer);
        verify(consumer, only()).close();
    }

    @Test
    public void testKafkaConsumer() {
        KafkaConsumer<String, String> consumer2 =
                service.createKafkaConsumer("group");
        assertNotNull(consumer2);
    }

    @Test
    public void test() {

        ConsumerGroupsDescription result =
                new ConsumerGroupsDescription("group");

        TopicPartition tp1 = new TopicPartition("topic", 0);
        TopicPartition tp2 = new TopicPartition("topic", 1);
        TopicPartition tp3 = new TopicPartition("topic", 2);
        TopicPartition tp4 = new TopicPartition("ignore", 2);

        doNothing().when(consumer).assign(Mockito.any());
        doNothing().when(consumer).seekToEnd(Mockito.any());
        doReturn(new OffsetAndMetadata(15)).when(consumer)
                .committed(Mockito.eq(tp1));
        doReturn(new OffsetAndMetadata(5)).when(consumer)
                .committed(Mockito.eq(tp2));
        doReturn(new OffsetAndMetadata(1)).when(consumer)
                .committed(Mockito.eq(tp3));
        doReturn(17L).when(consumer).position(Mockito.eq(tp1));
        doReturn(10L).when(consumer).position(Mockito.eq(tp2));
        doReturn(2L).when(consumer).position(Mockito.eq(tp3));

        doReturn("client1").when(kConsumer1).clientId();
        doReturn("consumer1").when(kConsumer1).consumerId();
        doReturn("client2").when(kConsumer2).clientId();
        doReturn("consumer2").when(kConsumer2).consumerId();

        List<TopicPartition> list1 = new ArrayList<>();
        list1.add(tp1);
        list1.add(tp3);
        list1.add(tp4);
        List<TopicPartition> list2 = new ArrayList<>();
        list2.add(tp2);
        
        final ConsumerDescription cd1 = new ConsumerDescription(
        		kConsumer1.clientId(),
        		kConsumer1.consumerId()
        );
        
        final ConsumerDescription cd2 = new ConsumerDescription(
        		kConsumer2.clientId(),
        		kConsumer2.consumerId()
        );
        
        service.addConsumerDescription(result, cd1, list1, "topic",
                consumer);
        service.addConsumerDescription(result, cd2, list2, "topic",
                consumer);

        PartitionDescription pDesc1 =
                new PartitionDescription(0, "topic", "consumer1", 15, 17, 2);
        PartitionDescription pDesc2 =
                new PartitionDescription(1, "topic", "consumer2", 5, 10, 5);
        PartitionDescription pDesc3 =
                new PartitionDescription(2, "topic", "consumer1", 1, 2, 1);
        ConsumerDescription cDesc1 =
                new ConsumerDescription("client1", "consumer1");
        cDesc1.addPartition(pDesc1);
        cDesc1.addPartition(pDesc3);
        ConsumerDescription cDesc2 =
                new ConsumerDescription("client2", "consumer2");
        cDesc2.addPartition(pDesc2);

        ConsumerGroupsDescription expected =
                new ConsumerGroupsDescription("group");
        expected.getDescPerConsumer().put("consumer1", cDesc1);
        expected.getDescPerConsumer().put("consumer2", cDesc2);
        expected.getDescPerPartition().put("0", pDesc1);
        expected.getDescPerPartition().put("1", pDesc2);
        expected.getDescPerPartition().put("2", pDesc3);

        assertEquals(expected, result);
    }
}
