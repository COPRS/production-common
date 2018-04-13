package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

public class UnknownVolumeNameException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6134253736778850869L;

	public UnknownVolumeNameException(String message) {
		super(message);
	}

	public UnknownVolumeNameException(String message, Throwable cause) {
		super(message, cause);
	}

}
