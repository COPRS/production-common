package esa.s1pdgs.cpoc.preparation.worker.model.exception;

@SuppressWarnings("serial")
public class DiscardedException extends RuntimeException {
	public DiscardedException(final String message) {
		super(message);
	}
}
