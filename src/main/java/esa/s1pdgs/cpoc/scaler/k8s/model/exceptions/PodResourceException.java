package esa.s1pdgs.cpoc.scaler.k8s.model.exceptions;

public class PodResourceException extends K8sEntityException {

	private static final long serialVersionUID = -4025488986609198762L;

	public PodResourceException(String message) {
		super(ErrorCode.K8S_NO_TEMPLATE_POD, message);
	}

	public PodResourceException(String message, Throwable cause) {
		super(ErrorCode.K8S_NO_TEMPLATE_POD, message, cause);
	}

}
