package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception concerning an EDRS session file management
 * @author Cyrielle Gailliard
 *
 */
public class InvalidFormatProduct extends AbstractCodedException {

	private static final long serialVersionUID = 3720211649870339168L;

	public InvalidFormatProduct(String message) {
		super(ErrorCode.INVALID_PRODUCT_FORMAT, message);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
