package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Viveris Technologies
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such job")
public class AppCatalogJobNotFoundException extends AbstractAppDataException {

    /**
     * 
     */
    private static final long serialVersionUID = -5220416714429436808L;

    /**
     * Job identifier
     */
    private final long jobId;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobNotFoundException(final long jobId) {
        super(ErrorCode.JOB_NOT_FOUND, "Job not found");
        this.jobId = jobId;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[jobId %s] [msg %s]", jobId, getMessage());
    }
}
