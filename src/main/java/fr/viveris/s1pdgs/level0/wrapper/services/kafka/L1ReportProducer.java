package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.ReportDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.KafkaSendException;

/**
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class L1ReportProducer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(L1ReportProducer.class);

	/**
	 * KAFKA template for topic "session"
	 */
	@Autowired
	private KafkaTemplate<String, ReportDto> kafkaL1ReportTemplate;

	/**
	 * Name of the topic "session"
	 */
	@Value("${kafka.topic.l1-reports}")
	private String kafkaTopic;

	/**
	 * Send a message to a topic and wait until one is published
	 * 
	 * @param descriptor
	 */
	public void send(ReportDto descriptor) throws KafkaSendException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send L1_REPORT = {}", descriptor);
			}
			kafkaL1ReportTemplate.send(kafkaTopic, descriptor).get();
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaSendException(kafkaTopic, descriptor.getProductName(), e.getMessage(), e);
		}
	}
}
