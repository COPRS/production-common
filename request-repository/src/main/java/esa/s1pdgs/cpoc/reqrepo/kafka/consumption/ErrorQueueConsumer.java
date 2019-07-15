package esa.s1pdgs.cpoc.reqrepo.kafka.consumption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;

@Controller
public class ErrorQueueConsumer {
	
	/**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(ErrorQueueConsumer.class);

    private final RequestRepository requestRepository; 
	
    public ErrorQueueConsumer(@Autowired final RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}
    
	@KafkaListener(topics = "${kafka.topic.errors}", groupId = "${kafka.group-id}")
	public void receive(FailedProcessingDto failedProcessing, final Acknowledgment acknowledgment) {
		try {
			requestRepository.saveFailedProcessing(failedProcessing);		
	    	acknowledgment.acknowledge();
	    } catch (Exception e) {
	    	LOGGER.error("[code {}] Exception occurred during acknowledgment {}", ErrorCode.INTERNAL_ERROR.getCode(), LogUtils.toString(e));
	    }
	}
}
