package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

public class UnknownKindExecption extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1694390566100800259L;

	public UnknownKindExecption(String message) {
		super(message);
	}

	public UnknownKindExecption(String message, Throwable cause) {
		super(message, cause);
	}
}
