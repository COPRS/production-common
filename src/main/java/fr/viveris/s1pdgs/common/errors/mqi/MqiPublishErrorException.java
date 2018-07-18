package fr.viveris.s1pdgs.common.errors.mqi;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiPublishErrorException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 7444307610324617302L;

    /**
     * Output
     */
    private final String errorMessage;

    /**
     * @param category
     * @param message
     */
    public MqiPublishErrorException(final String errorMessage,
            final String message) {
        super(ErrorCode.MQI_PUBLISH_ERROR, message);
        this.errorMessage = errorMessage;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiPublishErrorException(final String errorMessage,
            final String message, final Throwable cause) {
        super(ErrorCode.MQI_PUBLISH_ERROR, message, cause);
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[errorMessage %s] [msg %s]", errorMessage,
                getMessage());
    }

}
