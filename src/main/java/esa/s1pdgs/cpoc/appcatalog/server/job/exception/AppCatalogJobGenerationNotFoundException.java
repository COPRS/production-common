package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Viveris Technologies
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such job generation")
public class AppCatalogJobGenerationNotFoundException
        extends AbstractAppDataException {

    /**
     * 
     */
    private static final long serialVersionUID = -5220416714429436808L;

    /**
     * Job identifier
     */
    private final long jobId;

    /**
     * Job identifier
     */
    private final String taskTable;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobGenerationNotFoundException(final long jobId,
            final String taskTable) {
        super(ErrorCode.JOB_NOT_FOUND, "Job generation not found");
        this.jobId = jobId;
        this.taskTable = taskTable;
    }

    /**
     * @return the jobId
     */
    public long getJobId() {
        return jobId;
    }

    /**
     * @return the taskTables
     */
    public String getTaskTable() {
        return taskTable;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[jobId %s] [taskTable %s] [msg %s]", jobId,
                taskTable, getMessage());
    }
}
