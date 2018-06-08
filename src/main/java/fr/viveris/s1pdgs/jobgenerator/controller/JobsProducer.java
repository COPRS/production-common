package fr.viveris.s1pdgs.jobgenerator.controller;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.exception.KafkaSendException;

/**
 * Kafka producer for the topic "t-pdgs-l0-jobs" or "t-pdgs-l1-jobs"
 * 
 * @author Cyrielle Gailliard
 *
 */
@Component
public class JobsProducer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(JobsProducer.class);

	/**
	 * KAFKA template producer
	 */
	private final KafkaTemplate<String, JobDto> kafkaJobsTemplate;

	/**
	 * Topic name used for L0 jobs
	 */
	private final String kafkaTopic;

	/**
	 * Constructor
	 * @param kafkaJobsTemplate
	 * @param kafkaTopic
	 */
	@Autowired
	public JobsProducer(final KafkaTemplate<String, JobDto> kafkaJobsTemplate, @Value("${kafka.topics.lx-jobs}") final String kafkaTopic) {
		this.kafkaJobsTemplate = kafkaJobsTemplate;
		this.kafkaTopic = kafkaTopic;
		
	}

	/**
	 * Send a message asynchronously to a topic
	 * 
	 * @param customer
	 * @throws KafkaSendException 
	 */
	public void send(final JobDto dto) throws KafkaSendException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send job = {}", dto);
			}
			kafkaJobsTemplate.send(kafkaTopic, dto).get();
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaSendException(kafkaTopic, dto, dto.getProductIdentifier(), e.getMessage(), e);
		}
	}

}
