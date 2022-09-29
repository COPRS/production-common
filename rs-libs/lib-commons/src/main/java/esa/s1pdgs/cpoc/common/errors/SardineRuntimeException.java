package esa.s1pdgs.cpoc.common.errors;

public class SardineRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7900790315629678800L;

	public SardineRuntimeException() {
		super();
	}

	public SardineRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SardineRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SardineRuntimeException(String message) {
		super(message);
	}

	public SardineRuntimeException(Throwable cause) {
		super(cause);
	}
	
	

}
