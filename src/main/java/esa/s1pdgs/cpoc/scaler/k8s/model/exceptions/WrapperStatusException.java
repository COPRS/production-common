package esa.s1pdgs.cpoc.scaler.k8s.model.exceptions;

public class WrapperStatusException extends K8sEntityException {

    private static final long serialVersionUID = -1655370514373140620L;

    private final String ip;
    private final String name;

    public WrapperStatusException(final String ip, final String name,
            final String message) {
        super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message);
        this.ip = ip;
        this.name = name;
    }

    public WrapperStatusException(final String ip, final String name,
            final String message, final Throwable cause) {
        super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message, cause);
        this.ip = ip;
        this.name = name;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String getLogMessage() {
        return String.format("[podIp %s] [podName %s] [msg %s]", this.ip,
                this.name, getMessage());
    }

}
