package fr.viveris.s1pdgs.jobgenerator.exception;

import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;

/**
 * Exception concerning the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObjectStorageException extends AbstractCodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Key in object storage
	 */
	private final String key;

	/**
	 * Family
	 */
	private final ProductFamily family;

	/**
	 * 
	 * @param key
	 * @param bucket
	 * @param e
	 */
	public ObjectStorageException(final ProductFamily family, final String key, final Throwable e) {
		super(ErrorCode.OBS_ERROR, e.getMessage(), e);
		this.key = key;
		this.family = family;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the family
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[family %s] [key %s] [msg %s]", this.family, this.key, getMessage());
	}
}
