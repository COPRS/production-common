/**
 * 
 */
package fr.viveris.s1pdgs.common.errors.processing;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * Exception occurred during job generation
 * 
 * @author Viveris Technologies
 */
public class WrapperProcessExecutionException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * Exit code of the process
     */
    private final int exitCode;

    /**
     * Constructor
     * 
     * @param message
     */
    public WrapperProcessExecutionException(final int exitCode, final String message) {
        super(ErrorCode.PROCESS_EXIT_ERROR, message);
        this.exitCode = exitCode;
    }

    /**
     * @return
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[exitCode %d] [msg %s]", exitCode, getMessage());
    }
}
