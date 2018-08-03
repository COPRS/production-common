package esa.s1pdgs.cpoc.scaler;

/**
 * Custom exception to give a error code to each exception
 * 
 * @author Cyrielle Gailliard
 *
 */
public abstract class AbstractCodedException extends Exception {

	private static final long serialVersionUID = -3674800585523293639L;

	/**
	 * The error code
	 */
	private final ErrorCode code;

	/**
	 * 
	 * @param code
	 * @param message
	 */
	public AbstractCodedException(final ErrorCode code, final String message) {
		super(message);
		this.code = code;
	}

	/**
	 * 
	 * @param code
	 * @param message
	 * @param e
	 */
	public AbstractCodedException(final ErrorCode code, final String message, final Throwable e) {
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
	 * Available error codes
	 * 
	 * @author Cyrielle Gailliard
	 *
	 */
	public enum ErrorCode {

		INTERNAL_ERROR(1), UNKNOWN_FAMILY(2), INVALID_PRODUCT_FORMAT(3), KAFKA_SEND_ERROR(70), KAFKA_RESUMING_ERROR(
				71), KAFKA_PAUSING_ERROR(72), KAFKA_COMMIT_ERROR(73), OS_SERVER_NOT_ACTIVE(
						110), OS_FLOATING_IP_NOT_ACTIVE(111), OS_SERVER_NOT_DELETED(112), OS_VOLUME_NOT_CREATED(
								113), K8S_UNKNOWN_RESOURCE(130), K8S_NO_TEMPLATE_POD(
										131), K8S_WRAPPER_STATUS_ERROR(132), K8S_WRAPPER_STOP_ERROR(133);

		/**
		 * code
		 */
		private final int code;

		/**
		 * 
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
	 * Used to display a formatted and custom message
	 * 
	 * @return
	 */
	public abstract String getLogMessage();

}
