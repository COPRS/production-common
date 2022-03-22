package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author Viveris Technologies
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Job generation forcely terminated")
public class AppCatalogJobGenerationTerminatedException extends AbstractAppDataException {

    /**
     * 
     */
    private static final long serialVersionUID = -5220416714429436808L;

    /**
     * State
     */
    private final String productName;

    /**
     * Type of job: db or dto
     */
    private final List<GenericMessageDto<? extends AbstractMessage>> mqiMessages;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobGenerationTerminatedException(final String productName,
            final List<GenericMessageDto<? extends AbstractMessage>> mqiMessages) {
        super(ErrorCode.JOB_GENERATION_TERMINATED, "Job generation forcely terminated");
        this.productName = productName;
        this.mqiMessages = mqiMessages;
    }

    /**
     * @return the state
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return the type
     */
    public List<GenericMessageDto<? extends AbstractMessage>> getMqiMessages() {
        return mqiMessages;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[productName %s] [mqiMessages %s] [msg %s]", productName, mqiMessages,
                getMessage());
    }
}
