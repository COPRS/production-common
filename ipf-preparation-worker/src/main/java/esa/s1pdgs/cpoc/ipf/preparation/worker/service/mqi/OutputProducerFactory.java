package esa.s1pdgs.cpoc.ipf.preparation.worker.service.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

/**
 * Service for publishing in topics
 * 
 * @author Viveris Technologies
 */
@Service
public class OutputProducerFactory {

    /**
     * Logger
     */
    protected static final Logger LOGGER = LogManager.getLogger(OutputProducerFactory.class);

    /**
     * MQI client for LEVEL_JOBS
     */
    private final GenericMqiClient senderJobs;

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProducerFactory(final GenericMqiClient senderJobs) {
        this.senderJobs = senderJobs;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendJob(GenericMessageDto<?> genericMessageDto, IpfExecutionJob dto)
            throws AbstractCodedException {
        GenericPublicationMessageDto<IpfExecutionJob> messageToPublish =
                new GenericPublicationMessageDto<IpfExecutionJob>(
                        genericMessageDto.getId(), dto.getFamily(), dto);
        messageToPublish.setInputKey(genericMessageDto.getInputKey());
        messageToPublish.setOutputKey(dto.getFamily().name());
        senderJobs.publish(messageToPublish, ProductCategory.LEVEL_JOBS);
    }    
}
