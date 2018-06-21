package fr.viveris.s1pdgs.ingestor.exceptions;

import fr.viveris.s1pdgs.ingestor.files.model.ProductFamily;

/**
 * Exception raised when error occurs when accessing to the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObjectStorageException extends FileRuntimeException {

	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Bucket name
	 */
	protected final ProductFamily family;

	/**
	 * Key in object storage
	 */
	protected final String key;

	/**
	 * Constructor
	 * 
	 * @param productName
	 * @param key
	 * @param bucket
	 * @param cause
	 */
	public ObjectStorageException(final String productName, final String key, final ProductFamily family,
			final Throwable cause) {
		super(ErrorCode.OBS_ERROR, productName, cause.getMessage(), cause);
		this.key = key;
		this.family = family;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[family %s] [key %s] [msg %s]", this.family, this.key, getMessage());
	}

	/**
	 * @return the bucket
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

}
