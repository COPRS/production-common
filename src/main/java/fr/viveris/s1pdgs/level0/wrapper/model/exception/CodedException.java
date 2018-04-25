package fr.viveris.s1pdgs.level0.wrapper.model.exception;

public class CodedException extends Exception {

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

		UNKNOWN_FAMILY(128), INTERNAL_ERROR(129), OBS_UNKOWN_OBJ(140), OBS_ERROR(141), PROCESS_EXIT_ERROR(
				151), PROCESS_TIMEOUT(152), KAFKA_SEND_ERROR(161), KAFKA_RESUMING_ERROR(162), KAFKA_PAUSING_ERROR(163), KAFKA_COMMIT_ERROR(164);

		private int code;

		ErrorCode(int code) {
			this.code = code;
		}

		public int getCode() {
			return this.code;
		}
	}

}
