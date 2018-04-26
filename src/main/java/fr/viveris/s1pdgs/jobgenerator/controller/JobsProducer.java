package fr.viveris.s1pdgs.jobgenerator.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;

/**
 * Kafka producer for the topic "t-pdgs-l0-jobs" or "t-pdgs-l1-jobs"
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobsProducer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(JobsProducer.class);

	/**
	 * KAFKA template producer
	 */
	@Autowired
	private KafkaTemplate<String, JobDto> kafkaJobsTemplate;

	/**
	 * Topic name used for L0 jobs
	 */
	@Value("${kafka.topics.lx-jobs}")
	String kafkaTopic;

	/**
	 * Send a message asynchronously to a topic
	 * 
	 * @param customer
	 */
	public void send(JobDto dto) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Send data={} in topic={}", dto, kafkaTopic);
		}
		ListenableFuture<SendResult<String, JobDto>> future = kafkaJobsTemplate.send(kafkaTopic, dto);

		// We register a callback to verify whether the messages are sent to the topic
		// successfully or not
		future.addCallback(new ListenableFutureCallback<SendResult<String, JobDto>>() {
			@Override
			public void onSuccess(SendResult<String, JobDto> result) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("[MONITOR] [job {}] Job successfully published in topic={}", dto.getProductIdentifier(),
							kafkaTopic);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				LOGGER.error("[MONITOR] [job {}] Job publication in topic {} failed: {}", dto.getProductIdentifier(), kafkaTopic,
						e.getMessage());
			}
		});
	}

}
