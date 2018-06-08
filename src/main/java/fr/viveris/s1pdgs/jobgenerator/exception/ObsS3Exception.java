package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception concerning the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObsS3Exception extends AbstractCodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Key in object storage
	 */
	private final String key;

	/**
	 * Bucket used in object storage
	 */
	private final String bucket;

	/**
	 * 
	 * @param key
	 * @param bucket
	 * @param e
	 */
	public ObsS3Exception(final String key, final String bucket, final Throwable e) {
		super(ErrorCode.OBS_ERROR, e.getMessage(), e);
		this.key = key;
		this.bucket = bucket;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the bucket
	 */
	public String getBucket() {
		return bucket;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[bucket %s] [key %s] [msg %s]", this.bucket, this.key, getMessage());
	}
}
