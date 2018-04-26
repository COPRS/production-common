package fr.viveris.s1pdgs.level0.wrapper.model.exception;

public class InternalErrorException extends CodedException {

	private static final long serialVersionUID = 858134968662485565L;

	public InternalErrorException(String message) {
		super(ErrorCode.INTERNAL_ERROR, message);
	}

	public InternalErrorException(String message, Throwable e) {
		super(ErrorCode.INTERNAL_ERROR, message, e);
	}

	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
