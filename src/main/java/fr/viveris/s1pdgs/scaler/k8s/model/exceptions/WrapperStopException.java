package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

public class WrapperStopException extends K8sEntityException {

    private static final long serialVersionUID = -1655370514373140620L;

    private final String ip;

    public WrapperStopException(final String ip, final String message) {
        super(ErrorCode.K8S_WRAPPER_STOP_ERROR, message);
        this.ip = ip;
    }

    public WrapperStopException(final String ip, final String message,
            final Throwable cause) {
        super(ErrorCode.K8S_WRAPPER_STOP_ERROR, message, cause);
        this.ip = ip;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    @Override
    public String getLogMessage() {
        return String.format("[podIp %s] [msg %s]", this.ip, getMessage());
    }

}
