package fr.viveris.s1pdgs.scaler.kafka.services;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.kafka.KafkaMonitoringProperties;
import fr.viveris.s1pdgs.scaler.kafka.model.ConsumerDescription;
import fr.viveris.s1pdgs.scaler.kafka.model.ConsumerGroupsDescription;
import fr.viveris.s1pdgs.scaler.kafka.model.PartitionDescription;
import kafka.admin.AdminClient;
import kafka.admin.AdminClient.ConsumerSummary;

/**
 * Class to access to KAFKA cluster
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaService {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

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
	 * @param kafkaAdminClient
	 * @param properties
	 */
	@Autowired
	public KafkaService(final AdminClient kafkaAdminClient, final KafkaMonitoringProperties properties) {
		this.kafkaAdminClient = kafkaAdminClient;
		this.properties = properties;
	}

	/**
	 * Get the description of consumers of a given group on a given topic.<br/>
	 * To get the offset and the lag on each partition, we connect a consumer on the partitions.<br/>
	 * The algorithm used is the one used by the KAFKA script consumer-group.sh with --describe option  
	 * @param groupId
	 * @param limitTopic
	 * @return
	 */
	public ConsumerGroupsDescription describeConsumerGroup(String groupId, String limitTopic) {

		KafkaConsumer<String, String> consumer = null;
		ConsumerGroupsDescription r = new ConsumerGroupsDescription(groupId);

		try {

			Properties consProps = new Properties();
			consProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
			consProps.put(ConsumerConfig.CLIENT_ID_CONFIG, properties.getClientId());
			consProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			consProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
			consProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, properties.getSessionTimeoutMs());
			consProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
			consProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
			consumer = new KafkaConsumer<String, String>(consProps);

			List<ConsumerSummary> groupSummaries = scala.collection.JavaConversions.seqAsJavaList(kafkaAdminClient
					.describeConsumerGroup(groupId, properties.getRequestTimeoutMs()).consumers().get());
			for (ConsumerSummary summary : groupSummaries) {
				ConsumerDescription cd = new ConsumerDescription(summary.clientId(), summary.consumerId());
				List<TopicPartition> topicPartitions = scala.collection.JavaConversions
						.seqAsJavaList(summary.assignment());
				for (TopicPartition tp : topicPartitions) {
					if (limitTopic.equalsIgnoreCase(tp.topic())) {
						// Calculate offset and lag
						long currentOffset = consumer.committed(tp).offset();
						consumer.assign(Arrays.asList(tp));
						consumer.seekToEnd(Arrays.asList(tp));
						long logEndOffset = consumer.position(tp);
						long lag = logEndOffset - currentOffset;
						// Create partition description
						PartitionDescription pd = new PartitionDescription(tp.partition(), tp.topic(),
								summary.consumerId(), currentOffset, logEndOffset, lag);
						cd.addPartition(pd);
						r.getDescriptionPerPartition().put("" + pd.getId(), pd);
					} else {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Kafka partition {} ignored because invalid topic {}", tp.partition(),
									tp.topic());
						}
					}
				}
				r.getDescriptionPerConsumer().put(cd.getConsumerId(), cd);
			}
		} finally {
			if (consumer != null) {
				consumer.close();
			}
		}

		return r;
	}
}
