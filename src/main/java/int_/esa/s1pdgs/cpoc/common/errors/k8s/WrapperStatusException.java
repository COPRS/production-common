package int_.esa.s1pdgs.cpoc.common.errors.k8s;

/**
 * @author Viveris Technologies
 */
public class WrapperStatusException extends K8sEntityException {

    /**
     * UUID
     */
    private static final long serialVersionUID = -1655370514373140620L;

    /**
     * IP address
     */
    private final String ipAddress;

    /**
     * Name
     */
    private final String name;

    /**
     * @param ipAddress
     * @param name
     * @param message
     */
    public WrapperStatusException(final String ipAddress, final String name,
            final String message) {
        super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message);
        this.ipAddress = ipAddress;
        this.name = name;
    }

    /**
     * @param ipAddress
     * @param name
     * @param message
     * @param cause
     */
    public WrapperStatusException(final String ipAddress, final String name,
            final String message, final Throwable cause) {
        super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message, cause);
        this.ipAddress = ipAddress;
        this.name = name;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[podIp %s] [podName %s] [msg %s]", ipAddress,
                name, getMessage());
    }

}
