package fr.viveris.s1pdgs.scaler;

public class InternalErrorException extends AbstractCodedException {

	private static final long serialVersionUID = 1694474051225086865L;

	public InternalErrorException(String message) {
		super(ErrorCode.INTERNAL_ERROR, message);
	}

	public InternalErrorException(String message, Throwable cause) {
		super(ErrorCode.INTERNAL_ERROR, message,cause);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
