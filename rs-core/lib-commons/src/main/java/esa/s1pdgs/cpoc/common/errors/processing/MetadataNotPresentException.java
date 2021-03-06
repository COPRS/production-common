package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class MetadataNotPresentException extends AbstractCodedException {

	private static final long serialVersionUID = -616528427720024929L;

	private static final String MESSAGE = "Metadata not present";

	public MetadataNotPresentException(String productName) {
		super(ErrorCode.ES_NOT_PRESENT_ERROR, MESSAGE);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
