package esa.s1pdgs.cpoc.scaler.kafka.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberAssignment;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.scaler.kafka.KafkaMonitoringProperties;
import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.ConsumerGroupsDescription;
import esa.s1pdgs.cpoc.scaler.kafka.model.PartitionDescription;


/**
 * Class to access to KAFKA cluster
 * 
 * @author Cyrielle Gailliard
 */
@Service
public class KafkaService {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(KafkaService.class);

    /**
     * KAFKA admin client
     */
    private final AdminClient kafkaAdminClient;

    /**
     * Properties
     */
    private final KafkaMonitoringProperties properties;

    /**
     * Constructor
     * 
     * @param kafkaAdminClient
     * @param properties
     */
    @Autowired
    public KafkaService(final AdminClient kafkaAdminClient,
            final KafkaMonitoringProperties properties) {
        this.kafkaAdminClient = kafkaAdminClient;
        this.properties = properties;
    }

    /**
     * Get the description of consumers of a given group on a given topic.<br/>
     * To get the offset and the lag on each partition, we connect a consumer on
     * the partitions.<br/>
     * The algorithm used is the one used by the KAFKA script consumer-group.sh
     * with --describe option
     * 
     * @param groupId
     * @param limitTopic
     * @return
     */
    public ConsumerGroupsDescription describeConsumerGroup(final String groupId,
            final String limitTopic) {

        ConsumerGroupsDescription r = new ConsumerGroupsDescription(groupId);
        KafkaConsumer<String, String> consumer = null;

        try {
        	LOGGER.debug("describeConsumerGroup is using groupid {}",groupId);
            consumer = createKafkaConsumer(groupId);
            
            final DescribeConsumerGroupsResult res = kafkaAdminClient
            		.describeConsumerGroups(Collections.singleton(groupId));
            
            for (final KafkaFuture<ConsumerGroupDescription> groupDescFuture : res.describedGroups().values())
            {
            	final ConsumerGroupDescription groupDesc = groupDescFuture.get();
            	for (final MemberDescription desc : groupDesc.members())
            	{
            		final MemberAssignment f = desc.assignment();
            		final String clientId = desc.clientId();
            		final String consumerId = desc.consumerId();  
            		
            	    final ConsumerDescription cd = new ConsumerDescription(clientId,consumerId);            		
            		addConsumerDescription(r, cd, new ArrayList<>(f.topicPartitions()), limitTopic, consumer);
            	}
            }
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
            closeKafkaConsumer(consumer);
        }
        return r;
    }
    
    final void addConsumerDescription(
    		ConsumerGroupsDescription result,
    		ConsumerDescription cd, 
            List<TopicPartition> topicPartitions,
            String limitTopic, 
            KafkaConsumer<String, String> consumer
            ) {
 
        for (TopicPartition tp : topicPartitions) {        	
            if (limitTopic.equalsIgnoreCase(tp.topic())) {
            	// Calculate offset and lag
            	OffsetAndMetadata lastCommitedOffset = consumer.committed(tp);
            	if (lastCommitedOffset == null) {
            		// A returning null value means no prior messages commited, we can ignore this case
            		LOGGER.debug("topic partition {} does not contain prior messages, skipping",tp);
            		continue;
            	}
                long currentOffset = lastCommitedOffset.offset();
                LOGGER.debug("topic partition {} current offset is {}",tp,currentOffset);
                
                consumer.assign(Arrays.asList(tp));
                consumer.seekToEnd(Arrays.asList(tp));
                long logEndOffset = consumer.position(tp);
                long lag = logEndOffset - currentOffset;
                // Create partition description
                PartitionDescription pd = new PartitionDescription(
                        tp.partition(), tp.topic(), cd.getConsumerId(),
                        currentOffset, logEndOffset, lag);
                cd.addPartition(pd);
                result.getDescPerPartition().put("" + pd.getId(), pd);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "Kafka partition {} ignored because invalid topic {}",
                            tp.partition(), tp.topic());
                }
            }
        }
        result.getDescPerConsumer().put(cd.getConsumerId(), cd);
    }



    /**
     * Build and get usefull Kafka client properties
     * 
     * @param groupId
     * @return
     */
    protected Properties kafkaConsumerProperties(String groupId) {
        Properties consProps = new Properties();
        consProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        consProps.put(ConsumerConfig.CLIENT_ID_CONFIG,
                properties.getClientId());
        consProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                properties.getSessionTimeoutMs());
        consProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        consProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        return consProps;
    }

    /**
     * Open the KAFKA consumer for given group
     * 
     * @param groupId
     */
    protected KafkaConsumer<String, String> createKafkaConsumer(
            final String groupId) {
        return new KafkaConsumer<String, String>(
                kafkaConsumerProperties(groupId));
    }

    /**
     * Close the kafka consumer
     */
    protected void closeKafkaConsumer(
            final KafkaConsumer<String, String> consumer) {
        if (consumer != null) {
            consumer.close();
        }
    }
}
