package fr.viveris.s1pdgs.jobgenerator.exception;

public abstract class AbstractCodedException extends Exception {

	private static final long serialVersionUID = -3674800585523293639L;

	private ErrorCode code;

	public AbstractCodedException(ErrorCode code, String message) {
		super(message);
		this.code = code;
	}

	public AbstractCodedException(ErrorCode code, String message, Throwable e) {
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

		UNKNOWN_FAMILY(128), 
		INTERNAL_ERROR(129), 
		INVALID_PRODUCT_FORMAT(130),
		OBS_UNKOWN_OBJ(140), 
		OBS_ERROR(141), 
		MAX_NUMBER_CACHED_JOB_REACH(151), 
		MAX_NUMBER_TASKTABLE_REACH(152), 
		MAX_NUMBER_CACHED_SESSIONS_REACH(153), 
		MAX_AGE_CACHED_JOB_REACH(154), 
		MAX_AGE_CACHED_SESSIONS_REACH(155), 
		KAFKA_SEND_ERROR(161), 
		KAFKA_RESUMING_ERROR(162), 
		KAFKA_PAUSING_ERROR(163), 
		KAFKA_COMMIT_ERROR(164), 
		MISSING_ROUTING_ENTRY(170), 
		JOB_GENERATOR_INIT_FAILED(171), 
		MISSING_INPUT(172);

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
