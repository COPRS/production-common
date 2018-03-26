package fr.viveris.s1pdgs.scaler.monitoring.kafka;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.scaler.monitoring.kafka.model.ConsumerGroupsDescription;
import fr.viveris.s1pdgs.scaler.monitoring.kafka.model.KafkaPerGroupPerTopicMonitor;

/**
 * Service to monitor KAFKA
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaMonitoring {

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
	public KafkaMonitoring(final KafkaService kafkaService) {
		this.kafkaService = kafkaService;
	}

	/**
	 * Get monitor for a given group and a given topic
	 * 
	 * @param groupId
	 * @param topicName
	 * @return
	 */
	public KafkaPerGroupPerTopicMonitor getPerGroupPerTopicMonitor(String groupId, String topicName) {
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
