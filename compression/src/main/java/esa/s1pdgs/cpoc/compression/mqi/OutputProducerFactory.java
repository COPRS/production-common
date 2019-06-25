package esa.s1pdgs.cpoc.compression.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.model.mqi.CompressedProductQueueMessage;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
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
    private final GenericMqiService<ProductDto> senderCompression;

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProducerFactory(
            @Qualifier("mqiServiceForCompression") final GenericMqiService<ProductDto> senderCompressed
            ) {
    	this.senderCompression = senderCompressed;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(final CompressedProductQueueMessage msg,
            GenericMessageDto<ProductDto> inputMessage)
            throws AbstractCodedException {
    	ProductDto dto = new ProductDto(msg.getProductName(), msg.getObjectStorageKey(), msg.getFamily());
    	GenericPublicationMessageDto messageDto = new GenericPublicationMessageDto<ProductDto>(inputMessage.getIdentifier(), msg.getFamily(), dto);
    	messageDto.setInputKey(inputMessage.getInputKey());
    	messageDto.setOutputKey(dto.getFamily().name());
    	
    	senderCompression.publish(messageDto);
    }

}
