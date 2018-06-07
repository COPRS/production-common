package fr.viveris.s1pdgs.mdcatalog.model.exception;

public abstract class AbstractCodedException extends Exception {

	private static final long serialVersionUID = -3911928196431571871L;

	/**
	 * Product name
	 */
	private final String productName;

	/**
	 * Code identified the error
	 */
	private final ErrorCode code;

	public AbstractCodedException(final ErrorCode code, final String productName, final String msg) {
		super(msg);
		this.productName = productName;
		this.code = code;
	}

	public AbstractCodedException(final ErrorCode code, final String productName, final String msg,
			final Throwable cause) {
		super(msg, cause);
		this.productName = productName;
		this.code = code;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
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
		KAFKA_SEND_ERROR(70), 
		KAFKA_RESUMING_ERROR(71), 
		KAFKA_PAUSING_ERROR(72), 
		KAFKA_COMMIT_ERROR(73),
		ES_CREATION_ERROR(90),
		ES_NOT_PRESENT_ERROR(91),
		METADATA_IGNORE_FILE(310),
		METADATA_FILE_PATH(311),
		METADATA_FILE_EXTENSION(312),
		METADATA_MALFORMED_ERROR(313),
		METADATA_EXTRACTION_ERROR(313);

		/**
		 * code
		 */
		private final int code;

		/**
		 * constructor
		 * 
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
