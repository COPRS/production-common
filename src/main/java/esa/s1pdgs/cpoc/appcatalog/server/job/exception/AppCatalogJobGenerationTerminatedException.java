package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    private final List<Object> mqiMessages;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobGenerationTerminatedException(final String productName,
            final List<Object> mqiMessages) {
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
    public List<Object> getMqiMessages() {
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
