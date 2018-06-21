package fr.viveris.s1pdgs.mdcatalog.model.exception;

import fr.viveris.s1pdgs.mdcatalog.model.ProductFamily;

/**
 * Exception raised when error occurs when accessing to the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObjectStorageException extends AbstractCodedException {
	
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Bucket name
	 */
	protected final ProductFamily family;

	/**
	 * Key in object storage
	 */
	protected final String key;

	public ObjectStorageException(ProductFamily family, String key, Throwable cause) {
		super(ErrorCode.OBS_ERROR, key, cause.getMessage(), cause);
		this.key = key;
		this.family = family;
	}

	/**
	 * @return the family
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

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[family %s] [key %s] [msg %s]", this.family.name(), this.key, getMessage());
	}
}
