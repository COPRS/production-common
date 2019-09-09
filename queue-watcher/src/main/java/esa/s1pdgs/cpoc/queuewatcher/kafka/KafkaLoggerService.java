package esa.s1pdgs.cpoc.queuewatcher.kafka;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.queuewatcher.config.ApplicationProperties;

@Service
public class KafkaLoggerService {
	private static final Logger LOGGER = LogManager.getLogger(KafkaLoggerService.class);

	private ConsumerFactory<String, String> factory;
	private ApplicationProperties properties;

	@Autowired
	public KafkaLoggerService(ApplicationProperties properties, final ConsumerFactory<String, String> factory) {
		this.properties = properties;
		this.factory = factory;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("Starting kafka logger service...");

		for (String topic : properties.getKafkaTopics()) {
			LOGGER.info("Subscribing to kafka topic {}", topic);
			ContainerProperties containerProperties = new ContainerProperties(topic);
			containerProperties.setMessageListener((MessageListener<String, String>) record -> {
				// do something with received record
				LOGGER.debug("Received message from topic {}: {} ",topic, record.value());

				String fileName = properties.getKafkaFolder() + "/kafka-" + topic;
				FileWriter writer = null;
				try {
					writer = new FileWriter(new File(fileName));
					writer.append(record.value());
				} catch (IOException ex) {
					LOGGER.error("An IO error occured while accessing {}", fileName);
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
						LOGGER.error("An IO error occured while closing {}", fileName);
					}
				}

				ConcurrentMessageListenerContainer<String, String> container = new ConcurrentMessageListenerContainer<>(factory,
						containerProperties);
				container.start();
			});

		}
	}
}
