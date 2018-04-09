package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

public class WrapperException extends Exception {

	private static final long serialVersionUID = -1655370514373140620L;

	public WrapperException(String message) {
		super(message);
	}

	public WrapperException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
