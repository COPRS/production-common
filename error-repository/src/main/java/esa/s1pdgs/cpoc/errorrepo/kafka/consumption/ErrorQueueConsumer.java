package esa.s1pdgs.cpoc.errorrepo.kafka.consumption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;

@Controller
public class ErrorQueueConsumer {
	
	/**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(ErrorQueueConsumer.class);

    private final ErrorRepository errorRepository; 
	
    public ErrorQueueConsumer(@Autowired final ErrorRepository errorRepository) {
		this.errorRepository = errorRepository;
	}
    
	@KafkaListener(topics = "${kafka.topic.errors}", groupId = "${kafka.group-id}")
	public void receive(FailedProcessingDto failedProcessing, final Acknowledgment acknowledgment) {
		try {
			LOGGER.error("DEBUG INFO: received failed processing message");
			errorRepository.saveFailedProcessing(failedProcessing);		
			LOGGER.error("ACK");
	    	acknowledgment.acknowledge();
			LOGGER.error("ACKED");
	    } catch (Exception e) {
	    	LOGGER.error("[code {}] Exception occurred during acknowledgment {}", ErrorCode.INTERNAL_ERROR.getCode(), e.getMessage());
	    }
	}
}
