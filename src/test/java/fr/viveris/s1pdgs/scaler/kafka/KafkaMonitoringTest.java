package fr.viveris.s1pdgs.scaler.kafka;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.scaler.kafka.model.ConsumerDescription;
import fr.viveris.s1pdgs.scaler.kafka.model.ConsumerGroupsDescription;
import fr.viveris.s1pdgs.scaler.kafka.model.KafkaPerGroupPerTopicMonitor;
import fr.viveris.s1pdgs.scaler.kafka.model.PartitionDescription;
import fr.viveris.s1pdgs.scaler.kafka.model.SpdgsTopic;
import fr.viveris.s1pdgs.scaler.kafka.services.KafkaService;

public class KafkaMonitoringTest {

    /**
     * Kafka properties
     */
    @Mock
    private KafkaMonitoringProperties kafkaProperties;

    /**
     * KAFKA service
     */
    @Mock
    private KafkaService kafkaService;

    /**
     * Admin to test
     */
    private KafkaMonitoring admin;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        Map<SpdgsTopic, String> topics = new HashMap<>();
        topics.put(SpdgsTopic.L1_JOBS, "topic");
        Map<SpdgsTopic, String> groups = new HashMap<>();
        groups.put(SpdgsTopic.L1_JOBS, "group");
        doReturn(topics).when(kafkaProperties).getTopics();
        doReturn(groups).when(kafkaProperties).getGroupIdPerTopic();

        admin = new KafkaMonitoring(kafkaProperties, kafkaService);
    }

    @Test
    public void testMonitoring() {
        PartitionDescription pDesc1 = new PartitionDescription(0, "topic", "consumer1", 25, 27, 2);
        PartitionDescription pDesc2 = new PartitionDescription(1, "topic", "consumer2", 42, 47, 5);
        PartitionDescription pDesc3 = new PartitionDescription(2, "topic", "consumer1", 12, 13, 1);
        
        ConsumerDescription cDesc1 = new ConsumerDescription("client1", "consumer1");
        cDesc1.addPartition(pDesc1);
        cDesc1.addPartition(pDesc3);
        ConsumerDescription cDesc2 = new ConsumerDescription("client2", "consumer2");
        cDesc2.addPartition(pDesc2);
        
        ConsumerGroupsDescription desc = new ConsumerGroupsDescription("group");
        desc.getDescPerConsumer().put("consumer1", cDesc1);
        desc.getDescPerConsumer().put("consumer2", cDesc2);
        desc.getDescPerPartition().put("0", pDesc1);
        desc.getDescPerPartition().put("1", pDesc2);
        desc.getDescPerPartition().put("2", pDesc3);
        
        doReturn(desc).when(kafkaService).describeConsumerGroup(
                Mockito.anyString(), Mockito.anyString());
        
        KafkaPerGroupPerTopicMonitor result = admin.monitorL1Jobs();
        assertEquals("group", result.getGroupId());
        assertEquals("topic", result.getTopicName());
        assertEquals(2, result.getNbConsumers());
        assertEquals(3, result.getNbPartitions());
        assertEquals(2, result.getLagPerConsumers().size());
        assertEquals(Long.valueOf(3), result.getLagPerConsumers().get("consumer1"));
        assertEquals(Long.valueOf(5), result.getLagPerConsumers().get("consumer2"));
        assertEquals(3, result.getLagPerPartition().size());
        assertEquals(Long.valueOf(2), result.getLagPerPartition().get(Integer.valueOf(0)));
        assertEquals(Long.valueOf(5), result.getLagPerPartition().get(Integer.valueOf(1)));
        assertEquals(Long.valueOf(1), result.getLagPerPartition().get(Integer.valueOf(2)));
        
        verify(kafkaService, only()).describeConsumerGroup(
                Mockito.eq("group"), Mockito.eq("topic"));
    }

    @Test
    public void testMonitoringWhenNoDescPerConsumer() {
        PartitionDescription pDesc1 = new PartitionDescription(0, "topic", "consumer1", 25, 27, 2);
        PartitionDescription pDesc2 = new PartitionDescription(1, "topic", "consumer2", 42, 47, 5);
        PartitionDescription pDesc3 = new PartitionDescription(2, "topic", "consumer1", 12, 13, 1);
        
        ConsumerGroupsDescription desc = new ConsumerGroupsDescription("group");
        desc.getDescPerPartition().put("0", pDesc1);
        desc.getDescPerPartition().put("1", pDesc2);
        desc.getDescPerPartition().put("2", pDesc3);
        
        doReturn(desc).when(kafkaService).describeConsumerGroup(
                Mockito.anyString(), Mockito.anyString());
        
        KafkaPerGroupPerTopicMonitor result = admin.monitorL1Jobs();
        assertEquals("group", result.getGroupId());
        assertEquals("topic", result.getTopicName());
        assertEquals(0, result.getNbConsumers());
        assertEquals(3, result.getNbPartitions());
        assertEquals(0, result.getLagPerConsumers().size());
        assertEquals(3, result.getLagPerPartition().size());
        assertEquals(Long.valueOf(2), result.getLagPerPartition().get(Integer.valueOf(0)));
        assertEquals(Long.valueOf(5), result.getLagPerPartition().get(Integer.valueOf(1)));
        assertEquals(Long.valueOf(1), result.getLagPerPartition().get(Integer.valueOf(2)));
        
        verify(kafkaService, only()).describeConsumerGroup(
                Mockito.eq("group"), Mockito.eq("topic"));
    }

    @Test
    public void testMonitoringWhenNoDescPartition() {
        PartitionDescription pDesc1 = new PartitionDescription(0, "topic", "consumer1", 25, 27, 2);
        PartitionDescription pDesc2 = new PartitionDescription(1, "topic", "consumer2", 42, 47, 5);
        PartitionDescription pDesc3 = new PartitionDescription(2, "topic", "consumer1", 12, 13, 1);
        
        ConsumerDescription cDesc1 = new ConsumerDescription("client1", "consumer1");
        cDesc1.addPartition(pDesc1);
        cDesc1.addPartition(pDesc3);
        ConsumerDescription cDesc2 = new ConsumerDescription("client2", "consumer2");
        cDesc2.addPartition(pDesc2);
        
        ConsumerGroupsDescription desc = new ConsumerGroupsDescription("group");
        desc.getDescPerConsumer().put("consumer1", cDesc1);
        desc.getDescPerConsumer().put("consumer2", cDesc2);
        
        doReturn(desc).when(kafkaService).describeConsumerGroup(
                Mockito.anyString(), Mockito.anyString());
        
        KafkaPerGroupPerTopicMonitor result = admin.monitorL1Jobs();
        assertEquals("group", result.getGroupId());
        assertEquals("topic", result.getTopicName());
        assertEquals(2, result.getNbConsumers());
        assertEquals(0, result.getNbPartitions());
        assertEquals(2, result.getLagPerConsumers().size());
        assertEquals(Long.valueOf(3), result.getLagPerConsumers().get("consumer1"));
        assertEquals(Long.valueOf(5), result.getLagPerConsumers().get("consumer2"));
        assertEquals(0, result.getLagPerPartition().size());
        
        verify(kafkaService, only()).describeConsumerGroup(
                Mockito.eq("group"), Mockito.eq("topic"));
    }

}
