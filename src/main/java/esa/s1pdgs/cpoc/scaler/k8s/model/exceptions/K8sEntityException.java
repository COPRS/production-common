package esa.s1pdgs.cpoc.scaler.k8s.model.exceptions;

import esa.s1pdgs.cpoc.scaler.AbstractCodedException;

public class K8sEntityException extends AbstractCodedException {

	private static final long serialVersionUID = 3960118050558022364L;

	public K8sEntityException(ErrorCode code, String message) {
		super(code, message);
	}

	public K8sEntityException(ErrorCode code, String message, Throwable e) {
		super(code, message, e);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
