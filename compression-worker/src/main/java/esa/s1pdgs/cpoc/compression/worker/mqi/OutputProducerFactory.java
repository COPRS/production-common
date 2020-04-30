package esa.s1pdgs.cpoc.compression.worker.mqi;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.compression.worker.model.mqi.CompressedProductQueueMessage;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

@Service
public class OutputProducerFactory {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(OutputProducerFactory.class);

    /**
     * MQI client for LEVEL_SEGMENTS
     */
    private final GenericMqiClient senderCompression;
    
    private final AppStatus appStatus;

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProducerFactory(final GenericMqiClient senderCompressed, final AppStatus appStatus) {
    	this.senderCompression = senderCompressed;
    	this.appStatus = appStatus;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(
    		final CompressedProductQueueMessage msg, 
    		final GenericMessageDto<CompressionJob> inputMessage,
    		final UUID reportingId
    ) throws AbstractCodedException {
    	final CompressionEvent event = toCompressionEvent(msg);
    	event.setUid(reportingId);
    	    	
    	final GenericPublicationMessageDto<CompressionEvent> messageDto = new GenericPublicationMessageDto<CompressionEvent>(
    			inputMessage.getId(), 
    			msg.getFamily(), 
    			event
    	);
    	messageDto.setInputKey(inputMessage.getInputKey());
    	messageDto.setOutputKey(msg.getFamily().name());
    	
    	/*
    	 *  This block is doing a special handling when an MqiPublishApiError occurs. This shall be handled in a centralized way.
    	 *  A new story will be created to make this the same accross all services. If you are handling this story, please remove
    	 *  it!
    	 */
    	try {    	
    		senderCompression.publish(messageDto, ProductCategory.COMPRESSED_PRODUCTS);
    	}
    	catch (final MqiPublishApiError ace) {
			/*
			 * S1PRO-1370: It was requested that if an communication with the MQI server fails for the max amount of retries,
			 * the service is going into fail and being restarted by Kubernetes. All these exceptions are likely raised in
			 * this scenario and shall be handled before the AbstractCodedException in general is handled!
			 */    		
			LOGGER.error("Unable to reach the MQI Server for the maximum of retries. Terminating this service now. Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());
			appStatus.setShallBeStopped(true);
			appStatus.forceStopping();
		}
    }
    
    private final CompressionEvent toCompressionEvent(final CompressedProductQueueMessage msg)
    {
    	return new CompressionEvent(        		
        		msg.getFamily(),
        		msg.getObjectStorageKey()
        );
    }

}
