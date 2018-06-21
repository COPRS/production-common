package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception concerning the object storage
 * 
 * @author Viveris Technologies
 */
public class ObsParallelAccessException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3680895691846942569L;

    /**
     * Constructor
     * 
     * @param key
     * @param bucket
     * @param e
     */
    public ObsParallelAccessException(final Throwable cause) {
        super(ErrorCode.OBS_PARALLEL_ACCESS, cause.getMessage(), cause);
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }
}
