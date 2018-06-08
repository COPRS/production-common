package fr.viveris.s1pdgs.ingestor.exceptions;

/**
 * Abstract custom exception
 *
 */
public abstract class AbstractFileException extends Exception {

	private static final long serialVersionUID = -3911928196431571871L;

	/**
	 * Product name concerned by the exception
	 */
	protected final String productName;

	/**
	 * code
	 */
	protected final ErrorCode code;

	/**
	 * Constructor
	 * @param code
	 * @param productName
	 * @param message
	 */
	public AbstractFileException(final ErrorCode code, final String productName, final String message) {
		super(message);
		this.code = code;
		this.productName = productName;
	}

	/**
	 * Constructor
	 * @param code
	 * @param productName
	 * @param message
	 * @param e
	 */
	public AbstractFileException(final ErrorCode code, final String productName, final String message,
			final Throwable exc) {
		super(message, exc);
		this.code = code;
		this.productName = productName;
	}

	/**
	 * @return the code
	 */
	public ErrorCode getCode() {
		return code;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * Available errors
	 *
	 */
	public enum ErrorCode {

		UNKNOWN_FAMILY(2), 
		INTERNAL_ERROR(1), 
		OBS_UNKOWN_OBJ(50), 
		OBS_ERROR(51), 
		OBS_ALREADY_EXIST(52), 
		KAFKA_SEND_ERROR(70), 
		INGESTOR_IGNORE_FILE(200), 
		INGESTOR_INVALID_PATH(201), 
		INGESTOR_CLEAN(202);

		/**
		 * Code
		 */
		private final int code;

		/**
		 * Constructor
		 * @param code
		 */
		ErrorCode(final int code) {
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
	 * Get message for logs
	 * @return
	 */
	public abstract String getLogMessage();

}
