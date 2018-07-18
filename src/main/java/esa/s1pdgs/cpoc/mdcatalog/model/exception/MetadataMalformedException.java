package esa.s1pdgs.cpoc.mdcatalog.model.exception;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class MetadataMalformedException extends AbstractCodedException {

	private static final long serialVersionUID = 2939784030412076416L;

	private static final String MESSAGE = "Metadata malformed";

	private final String missingField;

	/**
	 * @param productName
	 */
	public MetadataMalformedException(final String productName, final String missingField) {
		super(ErrorCode.METADATA_MALFORMED_ERROR, productName, MESSAGE);
		this.missingField = missingField;
	}

	/**
	 * @return the missingField
	 */
	public String getMissingField() {
		return missingField;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[missingField %s] [msg %s]", missingField, getMessage());
	}

}
