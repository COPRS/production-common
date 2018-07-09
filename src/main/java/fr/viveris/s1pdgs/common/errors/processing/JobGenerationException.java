/**
 * 
 */
package fr.viveris.s1pdgs.common.errors.processing;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * Abstract class for custom exception concerning jobs
 * 
 * @author Viveris Technologies
 */
public class JobGenerationException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = -1059947384304413070L;

    /**
     * Task table XML filename
     */
    private final String taskTable;

    /**
     * @param taskTable
     * @param code
     * @param message
     */
    public JobGenerationException(final String taskTable, final ErrorCode code,
            final String message) {
        super(code, message);
        this.taskTable = taskTable;
    }

    /**
     * @param taskTable
     * @param code
     * @param message
     * @param e
     */
    public JobGenerationException(final String taskTable, final ErrorCode code,
            final String message, final Throwable exp) {
        super(code, message, exp);
        this.taskTable = taskTable;
    }

    /**
     * @return
     */
    public String getTaskTable() {
        return taskTable;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[taskTable %s] [msg %s]", taskTable,
                getMessage());
    }
}
