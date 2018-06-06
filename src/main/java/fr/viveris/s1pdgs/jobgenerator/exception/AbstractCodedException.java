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

		INTERNAL_ERROR(1), 
		UNKNOWN_FAMILY(2), 
		INVALID_PRODUCT_FORMAT(3), 
		OBS_UNKOWN_OBJ(50), 
		OBS_ERROR(51), 
		MAX_NUMBER_CACHED_JOB_REACH(270), 
		MAX_NUMBER_TASKTABLE_REACH(271), 
		MAX_NUMBER_CACHED_SESSIONS_REACH(272), 
		MAX_AGE_CACHED_JOB_REACH(273), 
		MAX_AGE_CACHED_SESSIONS_REACH(274), 
		MISSING_ROUTING_ENTRY(275), 
		JOB_GENERATOR_INIT_FAILED(276), 
		MISSING_INPUT(277), 
		KAFKA_SEND_ERROR(70), 
		KAFKA_RESUMING_ERROR(71), 
		KAFKA_PAUSING_ERROR(72), 
		KAFKA_COMMIT_ERROR(73);

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
