package esa.s1pdgs.cpoc.wrapper.job.mqi;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.wrapper.config.ProcessConfiguration;
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
     * MQI client 
     */
    private final GenericMqiClient sender;

    private final String hostname;

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProcuderFactory(final GenericMqiClient sender, final ProcessConfiguration processConfiguration) {
        this.sender = sender;
        this.hostname = processConfiguration.getHostname();
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
        dtoReport.setCreationDate(new Date());
        dtoReport.setHostname(hostname);
        sender.publish(
        		new GenericPublicationMessageDto<LevelReportDto>(
        				inputMessage.getIdentifier(), 
        				msg.getFamily(), 
        				dtoReport
        		),
        		ProductCategory.LEVEL_REPORTS
        );
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
            sender.publish(messageToPublish, ProductCategory.LEVEL_SEGMENTS);
        } else {
    		messageToPublish.setInputKey(inputMessage.getInputKey());
    		messageToPublish.setOutputKey(msg.getFamily().name());
            sender.publish(messageToPublish, ProductCategory.LEVEL_PRODUCTS);
        }
    }
    
    private final ProductDto toProductDto(final ObsQueueMessage msg)
    {
    	return new ProductDto(
        		msg.getProductName(), 
        		msg.getKeyObs(),
        		msg.getFamily(), 
        		toUppercaseOrNull(msg.getProcessMode()),
        		msg.getOqcFlag()
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
