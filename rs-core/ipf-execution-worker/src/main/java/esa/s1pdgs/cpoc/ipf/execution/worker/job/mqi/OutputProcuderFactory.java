package esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi;

import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.ObsQueueMessage;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

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

    
    // FIXME!!!
    // remove duplication below, 
    
    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public GenericPublicationMessageDto<LevelReportDto> sendOutput(
    		final FileQueueMessage msg, 
    		final GenericMessageDto<IpfExecutionJob> inputMessage,
    		final UUID reportUid
    )
            throws AbstractCodedException {
        final LevelReportDto dtoReport = new LevelReportDto(
        		msg.getProductName(),
                FileUtils.readFile(msg.getFile()), 
                msg.getFamily()
        );
        dtoReport.setCreationDate(new Date());
        dtoReport.setHostname(hostname);
        
        final GenericPublicationMessageDto<LevelReportDto> mess = new GenericPublicationMessageDto<LevelReportDto>(
				inputMessage.getId(), 
				msg.getFamily(), 
				dtoReport
		);        
        sender.publish(mess,ProductCategory.LEVEL_REPORTS);
        return mess;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public GenericPublicationMessageDto<ProductionEvent> sendOutput(
    		final ObsQueueMessage msg, 
    		final GenericMessageDto<IpfExecutionJob> inputMessage,
    		final UUID reportUid
    )
            throws AbstractCodedException {
    	
        final GenericPublicationMessageDto<ProductionEvent> messageToPublish = new GenericPublicationMessageDto<ProductionEvent>(
                inputMessage.getId(), 
                msg.getFamily(),
                toProductionEvent(msg, inputMessage.getBody().getTimeliness(), reportUid)
		);
 		messageToPublish.setInputKey(inputMessage.getInputKey());
		messageToPublish.setOutputKey(msg.getFamily().name());
		return messageToPublish;
    }
    
    private final ProductionEvent toProductionEvent(final ObsQueueMessage msg, final String timeliness, final UUID uid)
    {
    	return new ProductionEvent(
    			msg.getProductName(),
        		msg.getKeyObs(),        	
        		msg.getFamily(), 
        		toUppercaseOrNull(msg.getProcessMode()),
        		msg.getOqcFlag(),
        		timeliness,
        		uid
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
