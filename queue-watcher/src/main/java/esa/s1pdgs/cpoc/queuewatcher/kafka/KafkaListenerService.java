package esa.s1pdgs.cpoc.queuewatcher.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaListenerService {
	@KafkaListener(topics = "topicName", groupId = "queue-listener")
	public void listen(String message) {
	    System.out.println("Received Messasge in group foo: " + message);
	}
}
