package esa.s1pdgs.cpoc.preparation.worker.model.exception;

@SuppressWarnings("serial")
public class AppCatJobUpdateFailedException extends RuntimeException {
	public AppCatJobUpdateFailedException(final String message) {
		super(message);
	}

	public AppCatJobUpdateFailedException(final String message, final Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
