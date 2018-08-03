package esa.s1pdgs.cpoc.scaler.k8s.model.exceptions;

public class K8sUnknownResourceException extends K8sEntityException {

	private static final long serialVersionUID = 1694390566100800259L;

	public K8sUnknownResourceException(String message) {
		super(ErrorCode.K8S_UNKNOWN_RESOURCE, message);
	}
}
