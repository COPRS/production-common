package esa.s1pdgs.cpoc.common.errors.mqi;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiStopApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * @param category
     * @param message
     */
    public MqiStopApiError(final String message) {
        super(ErrorCode.MQI_STOP_API_ERROR, message);
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiStopApiError(final String message, final Throwable cause) {
        super(ErrorCode.MQI_STOP_API_ERROR, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
