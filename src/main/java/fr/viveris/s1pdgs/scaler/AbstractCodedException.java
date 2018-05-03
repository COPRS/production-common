package fr.viveris.s1pdgs.scaler;

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
		KAFKA_SEND_ERROR(161), 
		KAFKA_RESUMING_ERROR(162), 
		KAFKA_PAUSING_ERROR(163), 
		KAFKA_COMMIT_ERROR(164), 
		SERVER_NOT_ACTIVE(180), 
		FLOATING_IP_NOT_ACTIVE(181), 
		SERVER_NOT_DELETED(182), 
		VOLUME_NOT_CREATED(183), 
		K8S_UNKNOWN_RESOURCE(190), 
		K8S_NO_TEMPLATE_POD(191),
		K8S_WRAPPER_STATUS_ERROR(192),
		K8S_WRAPPER_STOP_ERROR(193);

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
