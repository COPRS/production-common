package esa.s1pdgs.cpoc.dissemination.worker.service;

@SuppressWarnings("serial")
public final class DisseminationException extends RuntimeException {

	public DisseminationException(String message) {
		super(message);
	}

	public DisseminationException(String message, Throwable ex) {
		super(message, ex);
	}

}
