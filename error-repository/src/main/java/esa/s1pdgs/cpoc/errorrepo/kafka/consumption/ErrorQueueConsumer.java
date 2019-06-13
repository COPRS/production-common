package esa.s1pdgs.cpoc.errorrepo.kafka.consumption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

@Controller
public class ErrorQueueConsumer {
	
	public final static String TOPIC_ERROR = "t-pdgs-errors";

	/**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(ErrorQueueConsumer.class);

	
	@KafkaListener(topics = TOPIC_ERROR, groupId = "${kafka.group-id}")
	public void receive(String message, final Acknowledgment acknowledgment) {
		try {
			System.out.println("Received Messasge: " + message); // TODO submit to Applicative Catalog instead
	    	acknowledgment.acknowledge();
	    } catch (Exception e) {
	    	LOGGER.error("[code {}] Exception occurred during acknowledgment {}", ErrorCode.INTERNAL_ERROR.getCode(), e.getMessage());
	    }
	}
}
