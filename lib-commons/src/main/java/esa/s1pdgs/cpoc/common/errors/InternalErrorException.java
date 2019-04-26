/**
 * 
 */
package esa.s1pdgs.cpoc.common.errors;

/**
 * Exception occurred during job generation
 * 
 * @author Cyrielle Gailliard
 */
public class InternalErrorException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * @param message
     */
    public InternalErrorException(final String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    /**
     * @param message
     * @param e
     */
    public InternalErrorException(final String message, final Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
