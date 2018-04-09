package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

public class PodResourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4025488986609198762L;

	public PodResourceException(String message) {
		super(message);
	}

	public PodResourceException(String message, Throwable cause) {
		super(message, cause);
	}

}
