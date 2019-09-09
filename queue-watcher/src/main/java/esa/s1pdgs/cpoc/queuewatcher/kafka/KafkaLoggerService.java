package esa.s1pdgs.cpoc.queuewatcher.kafka;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaLoggerService {
	private static final Logger LOGGER = LogManager.getLogger(KafkaLoggerService.class);
	
	private ConsumerFactory<String, String> factory;
	private ConcurrentMessageListenerContainer container;
 
    @Autowired
    public KafkaLoggerService(final ConsumerFactory<String, String> factory) {
    	this.factory = factory;
    }
    
	@PostConstruct
	public void init() {
		LOGGER.info("Starting kafka logger service...");
		
		ContainerProperties containerProperties = new ContainerProperties("t-pdgs-edrs-sessions");
		containerProperties.setMessageListener((MessageListener<String, String>) record -> {
		     //do something with received record
			 System.out.println("Received Messasge in group foo: " + record.key()+" => "+record.value());
		});
		
		container =
		        new ConcurrentMessageListenerContainer<>(
		                factory,
		                containerProperties);

		container.start();
	}	
}
