package fr.viveris.s1pdgs.level0.wrapper.model.exception;

public abstract class CodedException extends Exception {

	private static final long serialVersionUID = -3674800585523293639L;

	private ErrorCode code;

	public CodedException(ErrorCode code, String message) {
		super(message);
		this.code = code;
	}

	public CodedException(ErrorCode code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public ErrorCode getCode() {
		return code;
	}

	public enum ErrorCode {

		UNKNOWN_FAMILY(2), 
		INTERNAL_ERROR(1), 
		OBS_UNKOWN_OBJ(50), 
		OBS_ERROR(51), 
		PROCESS_EXIT_ERROR(290), 
		PROCESS_TIMEOUT(291), 
		KAFKA_SEND_ERROR(70), 
		KAFKA_RESUMING_ERROR(71), 
		KAFKA_PAUSING_ERROR(72), 
		KAFKA_COMMIT_ERROR(73);

		private int code;

		ErrorCode(int code) {
			this.code = code;
		}

		public int getCode() {
			return this.code;
		}
	}
	
	public abstract String getLogMessage();

}
