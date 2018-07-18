package esa.s1pdgs.cpoc.mdcatalog.model.exception;

public class MetadataExtractionException extends AbstractCodedException {

	private static final long serialVersionUID = 2134771514034032034L;

	public MetadataExtractionException(String productName, Throwable cause) {
		super(ErrorCode.METADATA_EXTRACTION_ERROR, productName, cause.getMessage(), cause);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
