package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.KafkaSendException;

/**
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@Service
public class L1SlicesProducer {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(L1SlicesProducer.class);

	/**
	 * KAFKA template for topic "session"
	 */
	@Autowired
	private KafkaTemplate<String, L1SliceDto> kafkaProductTemplate;

	/**
	 * Name of the topic "session"
	 */
	@Value("${kafka.topic.l1-slices}")
	private String kafkaTopic;

	/**
	 * Send a message to a topic and wait until one is published
	 * 
	 * @param descriptor
	 */
	public void send(L1SliceDto descriptor) throws KafkaSendException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send slice = {}", descriptor);
			}
			kafkaProductTemplate.send(kafkaTopic, descriptor).get();
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaSendException(kafkaTopic, descriptor.getProductName(), e.getMessage(), e);
		}
	}
}
