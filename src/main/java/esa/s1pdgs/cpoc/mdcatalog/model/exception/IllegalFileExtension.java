package esa.s1pdgs.cpoc.mdcatalog.model.exception;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class IllegalFileExtension extends AbstractCodedException {

	private static final long serialVersionUID = 2663452897332948566L;

	private static final String MESSAGE = "Cannot retrieve ERDS session file type from extension %s";

	/**
	 * @param productName
	 */
	public IllegalFileExtension(String extension) {
		super(ErrorCode.METADATA_FILE_EXTENSION, "", String.format(MESSAGE, extension));
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
