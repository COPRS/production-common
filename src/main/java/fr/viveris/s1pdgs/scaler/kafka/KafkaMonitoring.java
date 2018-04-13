package fr.viveris.s1pdgs.scaler.kafka;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.kafka.model.ConsumerGroupsDescription;
import fr.viveris.s1pdgs.scaler.kafka.model.KafkaPerGroupPerTopicMonitor;
import fr.viveris.s1pdgs.scaler.kafka.model.SpdgsTopic;
import fr.viveris.s1pdgs.scaler.kafka.services.KafkaService;

/**
 * Service to monitor KAFKA
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaMonitoring {

	/**
	 * Kafka properties
	 */
	private final KafkaMonitoringProperties kafkaProperties;

	/**
	 * KAFKA service
	 */
	private final KafkaService kafkaService;

	/**
	 * Constructor
	 * 
	 * @param kafkaService
	 */
	@Autowired
	public KafkaMonitoring(final KafkaMonitoringProperties kafkaProperties, final KafkaService kafkaService) {
		this.kafkaProperties = kafkaProperties;
		this.kafkaService = kafkaService;
	}

	public KafkaPerGroupPerTopicMonitor monitorL1Jobs() {
		String groupId = kafkaProperties.getGroupIdPerTopic().get(SpdgsTopic.L1_JOBS);
		String topicName = kafkaProperties.getTopics().get(SpdgsTopic.L1_JOBS);
		return this.monitorGroupPerTopic(groupId, topicName);
	}

	private KafkaPerGroupPerTopicMonitor monitorGroupPerTopic(String groupId, String topicName) {
		ConsumerGroupsDescription desc = this.kafkaService.describeConsumerGroup(groupId, topicName);
		KafkaPerGroupPerTopicMonitor monitor = new KafkaPerGroupPerTopicMonitor(new Date(), groupId, topicName);
		if (desc.getDescriptionPerPartition() != null) {
			monitor.setNbPartitions(desc.getDescriptionPerPartition().size());
			desc.getDescriptionPerPartition().forEach((k, v) -> {
				monitor.getLagPerPartition().put(Integer.valueOf(v.getId()), Long.valueOf(v.getLag()));
			});
		}
		if (desc.getDescriptionPerConsumer() != null) {
			monitor.setNbConsumers(desc.getDescriptionPerConsumer().size());
			desc.getDescriptionPerConsumer().forEach((k, v) -> {
				monitor.getLagPerConsumers().put(v.getConsumerId(), Long.valueOf(v.getTotalLag()));
			});
		}
		return monitor;
	}
}
