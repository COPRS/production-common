package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Viveris Technologies
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid job generation state")
public class AppCatalogJobGenerationInvalidStateException extends AbstractAppDataException {

    /**
     * 
     */
    private static final long serialVersionUID = -5220416714429436808L;

    /**
     * State
     */
    private final String state;

    /**
     * Type of job: db or dto
     */
    private final String type;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobGenerationInvalidStateException(final String state,
            final String type) {
        super(ErrorCode.JOB_GENERATION_INVALID_STATE, "Invalid job generation state");
        this.state = state;
        this.type = type;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[state %s] [type %s] [msg %s]", state, type,
                getMessage());
    }
}
