package esa.s1pdgs.cpoc.common.errors.k8s;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class K8sEntityException extends AbstractCodedException {

    /**
     * UUID
     */
    private static final long serialVersionUID = 3960118050558022364L;

    /**
     * @param code
     * @param message
     */
    public K8sEntityException(final ErrorCode code, final String message) {
        super(code, message);
    }

    /**
     * @param code
     * @param message
     * @param e
     */
    public K8sEntityException(final ErrorCode code, final String message,
            final Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
