package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

@SuppressWarnings("serial")
public class DiscardedException extends RuntimeException {
	public DiscardedException(final String message) {
		super(message);
	}
}
