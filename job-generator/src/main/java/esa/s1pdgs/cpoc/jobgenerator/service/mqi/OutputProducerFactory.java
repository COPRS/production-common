package esa.s1pdgs.cpoc.jobgenerator.service.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
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
    private final GenericMqiService<LevelJobDto> senderJobs;

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProducerFactory(
            @Qualifier("mqiServiceForLevelJobs") final GenericMqiService<LevelJobDto> senderJobs) {
        this.senderJobs = senderJobs;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendJob(GenericMessageDto<?> genericMessageDto, LevelJobDto dto)
            throws AbstractCodedException {
        GenericPublicationMessageDto<LevelJobDto> messageToPublish =
                new GenericPublicationMessageDto<LevelJobDto>(
                        genericMessageDto.getIdentifier(), dto.getFamily(), dto);
        messageToPublish.setInputKey(genericMessageDto.getInputKey());
        messageToPublish.setOutputKey(dto.getFamily().name());
        senderJobs.publish(messageToPublish);
    }    
}
