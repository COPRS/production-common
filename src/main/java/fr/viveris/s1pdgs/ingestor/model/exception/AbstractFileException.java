package fr.viveris.s1pdgs.ingestor.model.exception;

public abstract class AbstractFileException extends Exception {

	private static final long serialVersionUID = -3911928196431571871L;
	
	private String productName;

	private ErrorCode code;

	public AbstractFileException(ErrorCode code, String productName, String message) {
		super(message);
		this.code = code;
		this.productName = productName;
	}

	public AbstractFileException(ErrorCode code, String productName, String message, Throwable e) {
		super(message, e);
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
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	public enum ErrorCode {

		UNKNOWN_FAMILY(128), 
		INTERNAL_ERROR(129), 
		OBS_UNKOWN_OBJ(140), 
		OBS_ERROR(141), 
		OBS_ALREADY_EXIST(142), 
		KAFKA_SEND_ERROR(161),
		INGESTOR_IGNORE_FILE(200),
		INGESTOR_INVALID_PATH(201),
		INGESTOR_CLEAN(202)
		;

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
