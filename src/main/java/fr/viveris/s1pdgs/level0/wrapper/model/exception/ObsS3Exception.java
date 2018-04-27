package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception concerning the object storage
 * @author Cyrielle Gailliard
 *
 */
public class ObsS3Exception extends CodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;
	
	private String key;
	
	private String bucket;

	public ObsS3Exception(String key, String bucket, Throwable e) {
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

	public String getLogMessage() {
		return String.format("[bucket %s] [key %s] [msg %s]", this.bucket, this.key, getMessage());
	}
}
