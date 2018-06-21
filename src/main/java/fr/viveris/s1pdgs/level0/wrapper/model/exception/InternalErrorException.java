package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * @author Viveris Technologies
 */
public class InternalErrorException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 858134968662485565L;

    /**
     * Constructor
     * 
     * @param message
     */
    public InternalErrorException(final String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param e
     */
    public InternalErrorException(final String message, final Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
