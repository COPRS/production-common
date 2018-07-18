package fr.viveris.s1pdgs.level0.wrapper.job.mqi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import fr.viveris.s1pdgs.level0.wrapper.job.model.mqi.FileQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.job.model.mqi.ObsQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.utils.FileUtils;

/**
 * Service for publishing in KAFKA topics
 * 
 * @author Viveris Technologies
 */
@Service
public class OutputProcuderFactory {

    /**
     * MQI client for LEVEL_PRODUCTS
     */
    private final GenericMqiService<LevelProductDto> senderProducts;

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
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<LevelProductDto> senderProducts,
            @Qualifier("mqiServiceForLevelReports") final GenericMqiService<LevelReportDto> senderReports) {
        this.senderProducts = senderProducts;
        this.senderReports = senderReports;
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
        // TODO set mesage id
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
        LevelProductDto dtoProduct = new LevelProductDto(msg.getProductName(),
                msg.getKeyObs(), msg.getFamily());
        // TODO set mesage id
        senderProducts
                .publish(new GenericPublicationMessageDto<LevelProductDto>(
                        inputMessage.getIdentifier(), msg.getFamily(),
                        dtoProduct));
    }
}
