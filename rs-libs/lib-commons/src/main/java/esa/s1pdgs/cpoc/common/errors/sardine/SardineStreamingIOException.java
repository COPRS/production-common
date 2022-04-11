package esa.s1pdgs.cpoc.common.errors.sardine;

public class SardineStreamingIOException extends SardineRuntimeException {

	private static final long serialVersionUID = -6010510572593268416L;

	public SardineStreamingIOException() {
		super();
	}

	public SardineStreamingIOException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SardineStreamingIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public SardineStreamingIOException(String message) {
		super(message);
	}

	public SardineStreamingIOException(Throwable cause) {
		super(cause);
	}
	
	

}
