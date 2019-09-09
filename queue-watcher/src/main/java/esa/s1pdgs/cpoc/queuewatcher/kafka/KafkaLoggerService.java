package esa.s1pdgs.cpoc.queuewatcher.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaLoggerService {
	@KafkaListener(topics = "t-pdgs-compressed-products,t-pdgs-edrs-sessions", groupId = "foo")
	public void listen(String message) {
	    System.out.println("Received Messasge in group foo: " + message);
	}
}
