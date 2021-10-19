package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Viveris Technologies
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid job generation state transition")
public class AppCatalogJobGenerationInvalidTransitionStateException
        extends AbstractAppDataException {

    /**
     * 
     */
    private static final long serialVersionUID = -5220416714429436808L;

    /**
     * State
     */
    private final String stateBefore;

    /**
     * Type of job: db or dto
     */
    private final String stateAfter;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobGenerationInvalidTransitionStateException(
            final String stateBefore, final String stateAfter) {
        super(ErrorCode.JOB_GENERATION_INVALID_STATE_TRANSITION,
                "Invalid job generation state transition");
        this.stateBefore = stateBefore;
        this.stateAfter = stateAfter;
    }

    /**
     * @return the state
     */
    public String getStateBefore() {
        return stateBefore;
    }

    /**
     * @return the type
     */
    public String getStateAfter() {
        return stateAfter;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[stateBefore %s] [stateAfter %s] [msg %s]",
                stateBefore, stateAfter, getMessage());
    }
}
