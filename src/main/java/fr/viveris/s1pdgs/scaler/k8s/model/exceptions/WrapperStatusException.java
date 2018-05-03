package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

public class WrapperStatusException extends K8sEntityException {

	private static final long serialVersionUID = -1655370514373140620L;

	private String ip;
	private String name;

	public WrapperStatusException(String ip, String name, String message) {
		super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message);
		this.ip = ip;
		this.name = name;
	}

	public WrapperStatusException(String ip, String name, String message, Throwable cause) {
		super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message, cause);
		this.ip = ip;
		this.name = name;
	}

	@Override
	public String getLogMessage() {
		return String.format("[podIp %s] [podName %s] [msg %s]", this.ip, this.name, getMessage());
	}

}
