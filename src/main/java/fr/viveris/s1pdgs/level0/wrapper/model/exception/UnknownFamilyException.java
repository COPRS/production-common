package fr.viveris.s1pdgs.level0.wrapper.model.exception;

public class UnknownFamilyException extends CodedException {
	
	private static final long serialVersionUID = 9140236445794096614L;
	
	private String family;

	public UnknownFamilyException(String message, String family) {
		super(ErrorCode.UNKNOWN_FAMILY, message);
		this.family = family;
	}

	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}

	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
