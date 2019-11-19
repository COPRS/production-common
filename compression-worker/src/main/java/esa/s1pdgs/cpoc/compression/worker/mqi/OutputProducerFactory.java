package esa.s1pdgs.cpoc.compression.worker.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.worker.model.mqi.CompressedProductQueueMessage;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
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

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProducerFactory(final GenericMqiClient senderCompressed) {
    	this.senderCompression = senderCompressed;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(final CompressedProductQueueMessage msg, final GenericMessageDto<ProductionEvent> inputMessage) throws AbstractCodedException {
    	final GenericPublicationMessageDto<ProductionEvent> messageDto = new GenericPublicationMessageDto<ProductionEvent>(
    			inputMessage.getId(), 
    			msg.getFamily(), 
    			toProductionEvent(msg)
    	);
    	messageDto.setInputKey(inputMessage.getInputKey());
    	messageDto.setOutputKey(msg.getFamily().name());
    	
    	senderCompression.publish(messageDto, ProductCategory.COMPRESSED_PRODUCTS);
    }
    
    private final ProductionEvent toProductionEvent(final CompressedProductQueueMessage msg)
    {
    	return new ProductionEvent(
        		msg.getProductName(), 
        		msg.getObjectStorageKey(),
        		msg.getFamily(), 
        		null
        );
    }

}
