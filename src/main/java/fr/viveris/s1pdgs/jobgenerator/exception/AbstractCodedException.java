package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Abstract custom exception
 * 
 * @author Cyrielle Gailliard
 *
 */
public abstract class AbstractCodedException extends Exception {

	/**
	 * UUID
	 */
	private static final long serialVersionUID = -3674800585523293639L;

	/**
	 * Code identified the error
	 */
	private ErrorCode code;

	/**
	 * Constructor
	 * 
	 * @param code
	 * @param message
	 */
	public AbstractCodedException(ErrorCode code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * Constructor
	 * 
	 * @param code
	 * @param message
	 * @param e
	 */
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

	/**
	 * The available error codes
	 *
	 */
	public enum ErrorCode {

		UNKNOWN_FAMILY(128), INTERNAL_ERROR(129), INVALID_PRODUCT_FORMAT(130), OBS_UNKOWN_OBJ(140), OBS_ERROR(
				141), MAX_NUMBER_CACHED_JOB_REACH(151), MAX_NUMBER_TASKTABLE_REACH(
						152), MAX_NUMBER_CACHED_SESSIONS_REACH(153), MAX_AGE_CACHED_JOB_REACH(
								154), MAX_AGE_CACHED_SESSIONS_REACH(155), KAFKA_SEND_ERROR(161), KAFKA_RESUMING_ERROR(
										162), KAFKA_PAUSING_ERROR(163), KAFKA_COMMIT_ERROR(164), MISSING_ROUTING_ENTRY(
												170), JOB_GENERATOR_INIT_FAILED(171), MISSING_INPUT(172);

		/**
		 * code
		 */
		private final int code;

		/**
		 * constructor
		 * @param code
		 */
		private ErrorCode(final int code) {
			this.code = code;
		}

		/**
		 * 
		 * @return
		 */
		public int getCode() {
			return this.code;
		}
	}

	/**
	 * Display exception details (except code) in a specific format: [fieldname1
	 * fieldvalue1]... [fieldnamen fieldvaluen] [msg msg]
	 * 
	 * @return
	 */
	public abstract String getLogMessage();

}
