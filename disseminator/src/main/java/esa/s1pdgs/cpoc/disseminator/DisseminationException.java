package esa.s1pdgs.cpoc.disseminator;

@SuppressWarnings("serial")
public final class DisseminationException extends RuntimeException {
	public DisseminationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DisseminationException(String message) {
		super(message);
	}
}
