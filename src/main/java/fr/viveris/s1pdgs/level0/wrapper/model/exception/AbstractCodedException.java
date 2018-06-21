package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Custom exception
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractCodedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3674800585523293639L;

    /**
     * Error code
     */
    private final ErrorCode code;

    /**
     * Constructor
     * 
     * @param code
     * @param message
     */
    public AbstractCodedException(final ErrorCode code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor
     * 
     * @param code
     * @param message
     * @param e
     */
    public AbstractCodedException(final ErrorCode code, final String message,
            final Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @return the code
     */
    public ErrorCode getCode() {
        return code;
    }

    /**
     * Available values of error code
     * 
     * @author Viveris Technologies
     */
    public enum ErrorCode {

        UNKNOWN_FAMILY(2), 
        INTERNAL_ERROR(1), 
        OBS_UNKOWN_OBJ(50), 
        OBS_ERROR(51),
        OBS_PARALLEL_ACCESS(52),
        PROCESS_EXIT_ERROR(290),
        PROCESS_TIMEOUT(291), 
        KAFKA_SEND_ERROR(70),
        KAFKA_RESUMING_ERROR(71), 
        KAFKA_PAUSING_ERROR(72),
        KAFKA_COMMIT_ERROR(73);

        /**
         * Code
         */
        private final int code;

        /**
         * Constructor
         * 
         * @param code
         */
        ErrorCode(final int code) {
            this.code = code;
        }

        /**
         * @return
         */
        public int getCode() {
            return this.code;
        }
    }

    /**
     * Get the custom message for logs
     * 
     * @return
     */
    public abstract String getLogMessage();

}
