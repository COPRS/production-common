package esa.s1pdgs.cpoc.mdcatalog.model.exception;

/**
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class MetadataCreationException extends AbstractCodedException {

	private static final long serialVersionUID = 1086920560648012203L;

	private final String status;

	private final String result;

	private static final String MESSAGE = "Metadata not created";

	public MetadataCreationException(final String productName, final String result, final String status) {
		super(ErrorCode.ES_CREATION_ERROR, productName, MESSAGE);
		this.status = status;
		this.result = result;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[status %s] [result %s] [msg %s]", status, result, getMessage());
	}

}
