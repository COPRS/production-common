package fr.viveris.s1pdgs.mdcatalog.model.exception;

public class MetadataNotPresentException extends AbstractCodedException {

	private static final long serialVersionUID = -616528427720024929L;

	private static final String MESSAGE = "Metadata not present";

	public MetadataNotPresentException(String productName) {
		super(ErrorCode.ES_NOT_PRESENT_ERROR, productName, MESSAGE);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
