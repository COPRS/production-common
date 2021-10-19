package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

/**
 * Abstract custom exception
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractAppDataException extends Exception {

    /**
     * UUID
     */
    private static final long serialVersionUID = -3674800585523293639L;

    /**
     * Code identified the error
     */
    private final ErrorCode code;

    /**
     * Constructor
     * 
     * @param code
     * @param message
     */
    public AbstractAppDataException(final ErrorCode code,
            final String message) {
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
    public AbstractAppDataException(final ErrorCode code, final String message,
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
     * The available error codes
     */
    public enum ErrorCode {

        INTERNAL_ERROR(1),

        JOB_NOT_FOUND(10), JOB_INVALID_STATE(11),
        JOB_GENERATION_INVALID_STATE(12), JOB_GENERATION_NOT_FOUND(13),
        JOB_GENERATION_INVALID_STATE_TRANSITION(14),
        
        JOB_GENERATION_TERMINATED(30);

        /**
         * code
         */
        private final int code;

        /**
         * constructor
         * 
         * @param code
         */
        private ErrorCode(final int code) {
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
     * Display exception details (except code) in a specific format: [fieldname1
     * fieldvalue1]... [fieldnamen fieldvaluen] [msg msg]
     * 
     * @return
     */
    public abstract String getLogMessage();

}
