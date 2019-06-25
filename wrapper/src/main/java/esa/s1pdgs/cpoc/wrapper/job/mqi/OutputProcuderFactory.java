package esa.s1pdgs.cpoc.wrapper.job.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.ObsQueueMessage;

/**
 * Service for publishing in KAFKA topics
 * 
 * @author Viveris Technologies
 */
@Service
public class OutputProcuderFactory {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(OutputProcuderFactory.class);

    /**
     * MQI client for LEVEL_SEGMENTS
     */
    private final GenericMqiService<ProductDto> senderSegments;

    /**
     * MQI client for LEVEL_PRODUCTS
     */
    private final GenericMqiService<ProductDto> senderProducts;

    /**
     * MQI client for LEVEL_REPORTS
     */
    private final GenericMqiService<LevelReportDto> senderReports;


    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProcuderFactory(
            @Qualifier("mqiServiceForLevelSegments") final GenericMqiService<ProductDto> senderSegments,
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<ProductDto> senderProducts,
            @Qualifier("mqiServiceForLevelReports") final GenericMqiService<LevelReportDto> senderReports) {
        this.senderSegments = senderSegments;
        this.senderProducts = senderProducts;
        this.senderReports = senderReports;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(final FileQueueMessage msg, GenericMessageDto<LevelJobDto> inputMessage)
            throws AbstractCodedException {
        LevelReportDto dtoReport = new LevelReportDto(
        		msg.getProductName(),
                FileUtils.readFile(msg.getFile()), 
                msg.getFamily()
        );
        senderReports.publish(new GenericPublicationMessageDto<LevelReportDto>(inputMessage.getIdentifier(), msg.getFamily(), dtoReport));
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(final ObsQueueMessage msg, GenericMessageDto<LevelJobDto> inputMessage)
            throws AbstractCodedException {
    	
        final GenericPublicationMessageDto<ProductDto> messageToPublish = new GenericPublicationMessageDto<ProductDto>(
                inputMessage.getIdentifier(), 
                msg.getFamily(),
                toProductDto(msg)
		);
    	    	
        if (msg.getFamily() == ProductFamily.L0_SEGMENT) {
            senderSegments.publish(messageToPublish);
        } else {
    		messageToPublish.setInputKey(inputMessage.getInputKey());
    		messageToPublish.setOutputKey(msg.getFamily().name());
            senderProducts.publish(messageToPublish);
        }
    }
    
    private final ProductDto toProductDto(final ObsQueueMessage msg)
    {
    	return new ProductDto(
        		msg.getProductName(), 
        		msg.getKeyObs(),
        		msg.getFamily(), 
        		toUppercaseOrNull(msg.getProcessMode())
        );
    }
    
    private final String toUppercaseOrNull(final String string)
    {
    	if (string == null)
    	{
    		return null;
    	}
    	return string.toUpperCase();
    }
}
