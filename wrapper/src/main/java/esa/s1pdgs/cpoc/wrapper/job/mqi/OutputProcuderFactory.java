package esa.s1pdgs.cpoc.wrapper.job.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mqi.client.ErrorService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
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
    private final GenericMqiService<LevelSegmentDto> senderSegments;

    /**
     * MQI client for LEVEL_PRODUCTS
     */
    private final GenericMqiService<LevelProductDto> senderProducts;

    /**
     * MQI client for LEVEL_REPORTS
     */
    private final GenericMqiService<LevelReportDto> senderReports;

    /**
     * MQI client for errors
     */
    private final ErrorService senderErrors;

    /**
     * Constructor
     * 
     * @param senderProducts
     * @param senderReports
     */
    @Autowired
    public OutputProcuderFactory(
            @Qualifier("mqiServiceForLevelSegments") final GenericMqiService<LevelSegmentDto> senderSegments,
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<LevelProductDto> senderProducts,
            @Qualifier("mqiServiceForLevelReports") final GenericMqiService<LevelReportDto> senderReports,
            @Qualifier("mqiServiceForErrors") final ErrorService senderErrors) {
        this.senderSegments = senderSegments;
        this.senderProducts = senderProducts;
        this.senderReports = senderReports;
        this.senderErrors = senderErrors;
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(final FileQueueMessage msg,
            GenericMessageDto<LevelJobDto> inputMessage)
            throws AbstractCodedException {
        LevelReportDto dtoReport = new LevelReportDto(msg.getProductName(),
                FileUtils.readFile(msg.getFile()), msg.getFamily());
        senderReports.publish(new GenericPublicationMessageDto<LevelReportDto>(
                inputMessage.getIdentifier(), msg.getFamily(), dtoReport));
    }

    /**
     * Send an output in right topic according its family
     * 
     * @param msg
     * @throws AbstractCodedException
     */
    public void sendOutput(final ObsQueueMessage msg,
            GenericMessageDto<LevelJobDto> inputMessage)
            throws AbstractCodedException {
        if (msg.getFamily() == ProductFamily.L0_SEGMENT) {
            LevelSegmentDto dtoProduct =
                    new LevelSegmentDto(msg.getProductName(), msg.getKeyObs(),
                            msg.getFamily(), msg.getProcessMode());
            senderSegments
                    .publish(new GenericPublicationMessageDto<LevelSegmentDto>(
                            inputMessage.getIdentifier(), msg.getFamily(),
                            dtoProduct));
        } else {
            LevelProductDto dtoProduct =
                    new LevelProductDto(msg.getProductName(), msg.getKeyObs(),
                            msg.getFamily(), msg.getProcessMode());
            GenericPublicationMessageDto<LevelProductDto> messageToPublish =
                    new GenericPublicationMessageDto<LevelProductDto>(
                            inputMessage.getIdentifier(), msg.getFamily(),
                            dtoProduct);
            messageToPublish.setInputKey(inputMessage.getInputKey());
            messageToPublish.setOutputKey(msg.getFamily().name());
            senderProducts.publish(messageToPublish);
        }
    }

    /**
     * Publish a error
     * 
     * @param message
     */
    public void sendError(final String message) {
        try {
        	ErrorDto errorDto = new ErrorDto();
        	errorDto.setMessage(message);
            senderErrors.publish(new GenericPublicationMessageDto<ErrorDto>(ProductFamily.BLANK, errorDto));
        } catch (AbstractCodedException e) {
            LOGGER.error(e.getLogMessage());
        }
    }
}
