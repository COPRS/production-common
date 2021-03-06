package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * 
 */
public class IngestorIgnoredFileException extends AbstractCodedException {

	private static final long serialVersionUID = 6432844848252714971L;

	/**
	 * 
	 */
	private static final String MESSAGE = "File/folder %s will be ignored";

	/**
	 * 
	 * @param productName
	 * @param ignoredName
	 */
	public IngestorIgnoredFileException(final String ignoredName) {
		super(ErrorCode.INGESTOR_IGNORE_FILE, String.format(MESSAGE, ignoredName));
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
