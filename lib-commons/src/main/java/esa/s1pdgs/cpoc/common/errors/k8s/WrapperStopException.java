package esa.s1pdgs.cpoc.common.errors.k8s;

/**
 * @author Viveris Technologies
 */
public class WrapperStopException extends K8sEntityException {

    /**
     * UUID
     */
    private static final long serialVersionUID = -1655370514373140620L;

    /**
     * IP of concerned pod
     */
    private final String ipAddress;

    /**
     * @param ip
     * @param message
     */
    public WrapperStopException(final String ipAddress, final String message) {
        super(ErrorCode.K8S_WRAPPER_STOP_ERROR, message);
        this.ipAddress = ipAddress;
    }

    /**
     * @param ipAddress
     * @param message
     * @param cause
     */
    public WrapperStopException(final String ipAddress, final String message,
            final Throwable cause) {
        super(ErrorCode.K8S_WRAPPER_STOP_ERROR, message, cause);
        this.ipAddress = ipAddress;
    }

    /**
     * @return the ip
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[podIp %s] [msg %s]", ipAddress, getMessage());
    }

}
